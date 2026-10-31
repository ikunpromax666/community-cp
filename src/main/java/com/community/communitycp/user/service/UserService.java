/**
 * com.community.communitycp.user.service
 */
package com.community.communitycp.user.service;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.user.dto.RegisterDTO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Configuration;

@Configuration
public interface UserService {

    Result register(@Valid RegisterDTO registerDTO);
}
