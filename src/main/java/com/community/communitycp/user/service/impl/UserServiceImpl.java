/**
 * com.community.communitycp.user.service.impl
 */
package com.community.communitycp.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.util.JwtUtil;
import com.community.communitycp.common.util.PasswordUtil;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.user.dto.LoginDTO;
import com.community.communitycp.user.dto.RegisterDTO;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import com.community.communitycp.user.service.UserService;
import com.community.communitycp.user.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil JwtUtil;
    private final RedisService redisService;

    /**
     * Token 有效期（毫秒），与 JWT 一致
     */
    @Value("${community.jwt.expire}")
    private Long jwtExpire;

    public UserServiceImpl(UserMapper userMapper, JwtUtil JwtUtil, RedisService redisService) {
        this.userMapper = userMapper;
        this.JwtUtil = JwtUtil;
        this.redisService = redisService;
    }

    @Override
    public Result register(RegisterDTO registerDTO) {

        //校验用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())
        );
        if (existUser != null) {
            return Result.fail(ResultCode.USERNAME_EXISTS);
        }
        //校验手机号是否已注册
        User existPhone = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, registerDTO.getPhone())
        );
        if (existPhone != null) {
            return Result.fail(ResultCode.PHONE_EXISTS);
        }

        //校验角色：只能是患者(1)或医生(2)，不能注册管理员(3)
        if (registerDTO.getRole() != UserRoleConstant.PATIENT && registerDTO.getRole() != UserRoleConstant.DOCTOR) {
            return Result.fail(ResultCode.AUTH_ERROR);
        }

        //组装用户对象，密码加密存储
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setIsAuth(0);

        userMapper.insert(user);
        return Result.success();
    }

    @Override
    public Result<LoginVO> login(LoginDTO loginDTO) {
        //校验用户名是否存在
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername()));
        if (user == null) {
            return Result.fail(ResultCode. USERNAME_OR_PASSWORD_ERROR);
        }
        //校验密码是否正确
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.fail(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        //生成JWT token
        String token = JwtUtil.generateToken(user.getId(), user.getRole());

        //单端登录：以 userId 为维度存 token，新登录直接覆盖旧值，旧设备 token 立即失效
        redisService.set(
                RedisKeyConstant.loginTokenKey(user.getId()),
                token,
                jwtExpire,
                TimeUnit.MILLISECONDS
        );

        LoginVO loginVO = new LoginVO();

        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setRole(user.getRole());

        return Result.success(loginVO);
    }

    @Override
    public Result logout() {
        // 从上下文取当前登录用户，删除其 Redis 中的有效 token
        Long userId = UserContext.getUserId();
        if (userId != null) {
            redisService.delete(RedisKeyConstant.loginTokenKey(userId));
        }
        return Result.success();
    }
}
