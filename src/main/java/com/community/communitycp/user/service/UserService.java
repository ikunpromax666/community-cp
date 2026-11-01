/**
 * com.community.communitycp.user.service
 */
package com.community.communitycp.user.service;

import com.community.communitycp.common.core.Result;
import com.community.communitycp.user.dto.*;
import com.community.communitycp.user.vo.LoginVO;
import com.community.communitycp.user.vo.UserVO;
import jakarta.validation.Valid;


public interface UserService {

    Result register(RegisterDTO registerDTO);

    Result<LoginVO> login( LoginDTO loginDTO);

    Result logout();

    Result<UserVO> getUserInfo();

    Result auth( AuthDTO authDTO);

    Result updateProfile( UpProfileDTO upProfileDTO);

    Result updatePassword(UpPasswordDTO upPasswordDTO);
}
