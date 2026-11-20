package com.community.communitycp.consult.controller;

import com.community.communitycp.common.constant.UserRoleConstant;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.Result;
import com.community.communitycp.common.web.RequireRole;
import com.community.communitycp.consult.dto.ReviewDTO;
import com.community.communitycp.consult.dto.ReviewListDTO;
import com.community.communitycp.consult.service.ReviewService;
import com.community.communitycp.consult.vo.ReviewDetailVO;
import com.community.communitycp.consult.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ReviewController
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/16 17:25
 * Version:1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "评价接口")
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/submit")
    @RequireRole(UserRoleConstant.PATIENT)
    @Operation(summary = "提交评价")
    public Result<Void> submitReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        reviewService.submitReview(reviewDTO);
        return Result.success();
    }

    /**
     * 公开接口（已加入 AuthInterceptor 白名单），游客也可查看医生评价
     */
    @GetMapping("/list")
    @Operation(summary = "获取医生评价列表")
    public Result<PageResult<ReviewVO>> getReviewList(@Valid ReviewListDTO queryDTO) {
        return Result.success(reviewService.getList(queryDTO));
    }

    /**
     * 公开接口（已加入 AuthInterceptor 白名单，仅放行数字ID），游客也可查看评价详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public Result<ReviewDetailVO> getReview(
            @Parameter(description = "评价ID") @PathVariable("id") Long reviewId) {
        return Result.success(reviewService.getDetail(reviewId));
    }
}
