package com.community.communitycp.consult.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitycp.common.constant.OrderStatusConstant;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.consult.dto.ReviewDTO;
import com.community.communitycp.consult.dto.ReviewListDTO;
import com.community.communitycp.consult.entity.Review;
import com.community.communitycp.consult.mapper.ReviewMapper;
import com.community.communitycp.consult.service.ReviewService;
import com.community.communitycp.consult.vo.ReviewDetailVO;
import com.community.communitycp.consult.vo.ReviewVO;
import com.community.communitycp.doctor.entity.Doctor;
import com.community.communitycp.doctor.mapper.DoctorMapper;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ReviewServiceImpl
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/16 17:26
 * Version:1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final ConsultationOrderMapper consultationOrderMapper;
    private final DoctorMapper doctorMapper;
    private final RedisService redisService;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(ReviewDTO reviewDTO) {
        Long patientId = UserContext.getUserId();

        // 1. 归属校验：只能评价自己的订单（他人的订单一律当作不存在，防止越权探测）
        ConsultationOrder order = consultationOrderMapper.selectById(reviewDTO.getOrderId());
        if (order == null || !patientId.equals(order.getPatientId())) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 只有已完成的订单才能评价
        Integer orderStatus = order.getOrderStatus();
        if (orderStatus == null || orderStatus != OrderStatusConstant.FINISHED) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 3. 一单一评：先查一次给出友好提示，深层靠 uk_order_id 唯一索引兜底
        Long reviewed = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, order.getId()));
        if (reviewed > 0) {
            throw new BizException(ResultCode.REVIEW_SUBMITTED);
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setPatientId(patientId);
        review.setDoctorId(order.getDoctorId());
        review.setScore(reviewDTO.getScore());
        String content = reviewDTO.getContent();
        review.setContent(content == null || content.trim().isEmpty() ? null : content.trim());
        try {
            reviewMapper.insert(review);
        } catch (DuplicateKeyException e) {
            // 并发下两个请求同时通过第 3 步，唯一索引拦住后转成业务码
            log.warn("重复评价被唯一索引拦截 orderId={}", order.getId());
            throw new BizException(ResultCode.REVIEW_SUBMITTED);
        }

        // 4. 原子更新医生评分，与插入评价同事务
        doctorMapper.updateRating(order.getDoctorId(), reviewDTO.getScore());

        // 5. 评分变了，医生详情和科室列表缓存要失效
        clearDoctorCache(order.getDoctorId());
    }

    @Override
    public PageResult<ReviewVO> getList(ReviewListDTO queryDTO) {
        // doctor_id 存的是 user.id；排序用 create_time DESC, id DESC 才能命中 idx_doctor_time
        Page<Review> page = reviewMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getDoctorId, queryDTO.getDoctorId())
                        .orderByDesc(Review::getCreateTime)
                        .orderByDesc(Review::getId));

        List<Review> reviews = page.getRecords();

        // 一次性批量查患者，避免 N+1；同一患者多条评价只查一次
        List<Long> patientIds = new ArrayList<>();
        for (Review review : reviews) {
            if (!patientIds.contains(review.getPatientId())) {
                patientIds.add(review.getPatientId());
            }
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!patientIds.isEmpty()) {
            for (User user : userMapper.selectBatchIds(patientIds)) {
                userMap.put(user.getId(), user);
            }
        }

        List<ReviewVO> voList = new ArrayList<>();
        for (Review review : reviews) {
            User user = userMap.get(review.getPatientId());
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId().toString());
            // 患者可能已注销（查不到）或未实名，统一降级为匿名
            vo.setPatientName(user == null ? "匿名用户" : maskName(user.getRealName()));
            vo.setScore(review.getScore());
            vo.setContent(review.getContent());
            vo.setCreateTime(review.getCreateTime());
            voList.add(vo);
        }

        PageResult<ReviewVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public ReviewDetailVO getDetail(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BizException(ResultCode.REVIEW_NOT_FOUND);
        }

        // review.doctor_id 存的是 user.id，直接查 user 表
        User doctorUser = userMapper.selectById(review.getDoctorId());
        if (doctorUser == null) {
            throw new BizException(ResultCode.DOCTOR_NOT_FOUND);
        }
        // 患者可能已注销（查不到）或未实名，统一降级为匿名
        User patient = userMapper.selectById(review.getPatientId());
        ReviewDetailVO vo = new ReviewDetailVO();
        vo.setReviewId(review.getId().toString());
        vo.setOrderId(review.getOrderId().toString());
        vo.setDoctorId(review.getDoctorId().toString());
        vo.setDoctorName(doctorUser.getRealName());
        vo.setPatientName(patient == null ? "匿名用户" : maskName(patient.getRealName()));
        vo.setScore(review.getScore());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }

    /**
     * 姓名脱敏：张三 -> 张*，李建国 -> 李**
     */
    private String maskName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "匿名用户";
        }
        if (name.length() == 1) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name.substring(0, 1));
        for (int i = 1; i < name.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }

    private void clearDoctorCache(Long doctorUserId) {
        redisService.delete(RedisKeyConstant.doctorDetailKey(doctorUserId));
        Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getUserId, doctorUserId));
        if (doctor != null && doctor.getDeptId() != null) {
            redisService.deleteByPrefix(RedisKeyConstant.doctorDeptPrefix(doctor.getDeptId()));
        }
    }
}
