package com.community.communitycp.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitycp.common.constant.OrderStatusConstant;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.consult.entity.ConsultForm;
import com.community.communitycp.consult.mapper.ConsultFormMapper;
import com.community.communitycp.doctor.entity.Doctor;
import com.community.communitycp.doctor.mapper.DoctorMapper;
import com.community.communitycp.order.dto.CreateOrderDto;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import com.community.communitycp.order.service.OrderService;
import com.community.communitycp.order.vo.OrderCreateVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 订单号前缀
     */
    private static final String ORDER_NO_PREFIX = "ORD";

    /**
     * 订单号中的时间部分：yyyyMMddHHmmss
     */
    private static final DateTimeFormatter ORDER_NO_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 下单幂等 key 有效期（分钟）
     */
    private static final long IDEMPOTENT_MINUTES = 5L;

    private final ConsultationOrderMapper orderMapper;
    private final ConsultFormMapper consultFormMapper;
    private final DoctorMapper doctorMapper;
    private final RedisService redisService;

    public OrderServiceImpl(ConsultationOrderMapper orderMapper,ConsultFormMapper consultFormMapper, DoctorMapper doctorMapper, RedisService redisService) {
        this.orderMapper = orderMapper;
        this.consultFormMapper = consultFormMapper;
        this.doctorMapper = doctorMapper;
        this.redisService = redisService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(CreateOrderDto dto) {
        Long patientId = UserContext.getUserId();

        // 1. 幂等：同一 requestId 5 分钟内只允许提交一次
        String idempotentKey = RedisKeyConstant.idempotentOrderKey(patientId, dto.getRequestId());
        boolean firstTime = redisService.setIfAbsent(
                idempotentKey, "1", IDEMPOTENT_MINUTES, TimeUnit.MINUTES);
        if (!firstTime) {
            throw new BizException("请勿重复提交");
        }

        try {
            return doCreateOrder(dto, patientId);
        } catch (Exception e) {
            // 业务失败释放幂等 key，允许用户修正后重试
            redisService.delete(idempotentKey);
            throw e;
        }
    }

    /**
     * 真正的下单逻辑：校验医生 + 同事务写订单和问诊表单
     */
    private OrderCreateVO doCreateOrder(CreateOrderDto dto, Long patientId) {
        // 1. 校验医生（入参 doctorId 是 user.id，按 user_id 查）
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, dto.getDoctorId()));
        if (doctor == null) {
            throw new BizException(ResultCode.DOCTOR_NOT_FOUND);
        }
        if (doctor.getOnlineStatus() == null || doctor.getOnlineStatus() != 1) {
            throw new BizException(ResultCode.DOCTOR_OFFLINE);
        }
        if (doctor.getConsultStatus() != null && doctor.getConsultStatus() == 1) {
            throw new BizException(ResultCode.DOCTOR_BUSY);
        }

        // 2. 创建订单：金额取医生当前价格快照，后续调价不影响已下单
        ConsultationOrder order = new ConsultationOrder();
        order.setOrderNo(generateOrderNo());
        order.setPatientId(patientId);
        order.setDoctorId(dto.getDoctorId());
        order.setDeptId(doctor.getDeptId());
        order.setOrderStatus(OrderStatusConstant.PENDING_PAY);
        order.setAmount(doctor.getConsultPrice());
        orderMapper.insert(order);

        // 3. 同事务创建问诊表单（orderId 由雪花算法在 insert 前生成，插入后可用）
        ConsultForm form = new ConsultForm();
        form.setOrderId(order.getId());
        form.setSymptom(dto.getSymptom());
        form.setImgUrl(dto.getImgUrl());
        consultFormMapper.insert(form);

        // 4. 组装返回
        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderId(String.valueOf(order.getId()));
        vo.setOrderNo(order.getOrderNo());
        vo.setAmount(order.getAmount());
        vo.setOrderStatus(order.getOrderStatus());
        return vo;
    }

    /**
     * 生成订单号：ORD + yyyyMMddHHmmss + 4位随机数
     */
    private String generateOrderNo() {
        return ORDER_NO_PREFIX
                + LocalDateTime.now().format(ORDER_NO_TIME)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
