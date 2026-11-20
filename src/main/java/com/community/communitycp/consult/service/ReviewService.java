package com.community.communitycp.consult.service;

import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.consult.dto.ReviewDTO;
import com.community.communitycp.consult.dto.ReviewListDTO;
import com.community.communitycp.consult.vo.ReviewDetailVO;
import com.community.communitycp.consult.vo.ReviewVO;

/**
 * ClassName: ReviewService
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/16 17:25
 * Version:1.0
 */
public interface ReviewService {

    /**
     * 提交评价：仅订单患者本人、订单已完成、一单一评
     */
    void submitReview(ReviewDTO reviewDTO);

    /**
     * 获取医生评价列表
     *
     * @param queryDTO 查询参数（doctorId 为医生用户ID，即 user.id）
     * @return 分页评价列表（患者姓名已脱敏）
     */
    PageResult<ReviewVO> getList(ReviewListDTO queryDTO);

    ReviewDetailVO getDetail(Long reviewId);
}
