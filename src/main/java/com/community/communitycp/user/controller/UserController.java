/**
 * com.community.communitycp.user.controller
 */
package com.community.communitycp.user.controller;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.user.dto.LoginDTO;
import com.community.communitycp.user.dto.RegisterDTO;
import com.community.communitycp.user.service.UserService;
import com.community.communitycp.user.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result register(@Valid  @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/logout")
    public Result logout() {
        return userService.logout();
    }
}
