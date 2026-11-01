/**
 * com.community.communitycp.user.service
 */
package com.community.communitycp.user.service;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.user.dto.LoginDTO;
import com.community.communitycp.user.dto.RegisterDTO;
import com.community.communitycp.user.vo.LoginVO;
import jakarta.validation.Valid;


public interface UserService {

    Result register(RegisterDTO registerDTO);

    Result<LoginVO> login( LoginDTO loginDTO);

    Result logout();
}
