/**
 * com.community.communitycp.consult.service
 */
package com.community.communitycp.consult.service;

import com.community.communitycp.consult.dto.ConsultFormDTO;
import com.community.communitycp.consult.vo.ConsultFormVO;

public interface ConsultFormService {

    ConsultFormVO getConsultForm(Long orderId);

    void updateConsultForm(Long orderId, ConsultFormDTO dto);
}
