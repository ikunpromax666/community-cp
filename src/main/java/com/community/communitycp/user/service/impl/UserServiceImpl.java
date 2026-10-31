/**
 * com.community.communitycp.user.service.impl
 */
package com.community.communitycp.user.service.impl;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.util.JwtUtil;
import com.community.communitycp.common.util.PasswordUtil;
import com.community.communitycp.user.dto.RegisterDTO;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import com.community.communitycp.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Result register(RegisterDTO registerDTO) {
        // 密码加密
        JwtUtil jwtUtil = new JwtUtil();

        registerDTO.setPassword(PasswordUtil.encode(registerDTO.getPassword()));

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        userMapper.insert(user);
        return Result.success();
    }
}
