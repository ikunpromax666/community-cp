/**
 * com.community.communitycp.doctor.service
 */
package com.community.communitycp.doctor.service;

import com.community.communitycp.doctor.vo.DeptVO;

import java.util.List;

public interface DeptService {

    /**
     * 科室列表（仅启用的科室，按 sort 升序）
     */
    List<DeptVO> list();
}
