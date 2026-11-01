/**
 * com.community.communitycp.user.controller
 */
package com.community.communitycp.user.controller;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.user.dto.*;
import com.community.communitycp.user.service.UserService;
import com.community.communitycp.user.vo.LoginVO;
import com.community.communitycp.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "用户模块")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@Valid  @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result logout() {
        return userService.logout();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<UserVO> info() {
        return userService.getUserInfo();
    }
    @Operation(summary = "实名认证")
     @PostMapping("/auth")
    public Result auth(@Valid @RequestBody AuthDTO authDTO) {
        return userService.auth(authDTO);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户信息")
    public Result updateProfile(@Valid @RequestBody UpProfileDTO upProfileDTO) {
        return userService.updateProfile(upProfileDTO);
    }

    @PutMapping("/password")
    @Operation(summary = "修改用户密码")
    public Result updatePassword(@Valid @RequestBody UpPasswordDTO upPasswordDTO) {
        return userService.updatePassword(upPasswordDTO);
    }
}
