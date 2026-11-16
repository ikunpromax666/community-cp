package com.community.communitycp.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitycp.common.constant.OrderStatusConstant;
import com.community.communitycp.common.constant.PayStatusConstant;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.consult.entity.ConsultForm;
import com.community.communitycp.consult.entity.Prescription;
import com.community.communitycp.consult.mapper.ConsultFormMapper;
import com.community.communitycp.consult.mapper.PrescriptionMapper;
import com.community.communitycp.consult.vo.ConsultFormVO;
import com.community.communitycp.consult.vo.PrescriptionVO;
import com.community.communitycp.doctor.entity.Department;
import com.community.communitycp.doctor.entity.Doctor;
import com.community.communitycp.doctor.mapper.DepartmentMapper;
import com.community.communitycp.doctor.mapper.DoctorMapper;
import com.community.communitycp.order.dto.CreateOrderDto;
import com.community.communitycp.order.dto.OrderDTO;
import com.community.communitycp.order.dto.OrderListDTO;
import com.community.communitycp.order.entity.ConsultationOrder;
import com.community.communitycp.order.entity.PaymentRecord;
import com.community.communitycp.order.mapper.ConsultationOrderMapper;
import com.community.communitycp.order.mapper.PaymentRecordMapper;
import com.community.communitycp.order.service.OrderService;
import com.community.communitycp.order.vo.DetailOrderVO;
import com.community.communitycp.order.vo.OrderCreateVO;
import com.community.communitycp.order.vo.OrderListVO;
import com.community.communitycp.order.vo.PayVO;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 支付流水单号前缀
     */
    private static final String PAY_NO_PREFIX = "PAY";

    /**
     * 模拟支付渠道（无第三方接入，仅前端展示）
     */
    private static final String MOCK_PAY_CHANNEL = "MOCK";

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
    private final PaymentRecordMapper paymentRecordMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;
    private final RedisService redisService;

    public OrderServiceImpl(ConsultationOrderMapper orderMapper, ConsultFormMapper consultFormMapper,
                            DoctorMapper doctorMapper, PaymentRecordMapper paymentRecordMapper,
                            PrescriptionMapper prescriptionMapper, DepartmentMapper departmentMapper,
                            UserMapper userMapper, RedisService redisService) {
        this.orderMapper = orderMapper;
        this.consultFormMapper = consultFormMapper;
        this.doctorMapper = doctorMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayVO payOrder(OrderDTO dto) {
        Long patientId = UserContext.getUserId();
        Long orderId = dto.getOrderId();

        // 1. 查订单并校验归属（不匹配也按"不存在"处理，避免枚举他人订单）
        ConsultationOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getPatientId().equals(patientId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. CAS 推进状态：只有仍处于待支付才允许改成待接诊，防止并发重复支付
        LocalDateTime now = LocalDateTime.now();
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<ConsultationOrder>()
                .eq(ConsultationOrder::getId, orderId)
                .eq(ConsultationOrder::getOrderStatus, OrderStatusConstant.PENDING_PAY)
                .set(ConsultationOrder::getOrderStatus, OrderStatusConstant.PENDING_ACCEPT)
                .set(ConsultationOrder::getPayTime, now));
        if (rows == 0) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 3. 写支付流水（模拟支付直接成功，金额取订单下单时的快照）
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setOutTradeNo(generatePayNo());
        record.setAmount(order.getAmount());
        record.setPayChannel(MOCK_PAY_CHANNEL);
        record.setPayStatus(PayStatusConstant.SUCCESS);
        record.setPayTime(now);
        paymentRecordMapper.insert(record);

        // 4. 组装返回
        PayVO vo = new PayVO();
        vo.setOrderId(String.valueOf(orderId));
        vo.setOrderStatus(OrderStatusConstant.PENDING_ACCEPT);
        vo.setPayTime(now);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderDTO dto) {
        Long patientId = UserContext.getUserId();

        //判断订单是否存在且属于患者
        ConsultationOrder consultationOrder = orderMapper.selectById(dto.getOrderId());
        if (consultationOrder == null || !consultationOrder.getPatientId().equals(patientId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();

        if (consultationOrder.getOrderStatus() == OrderStatusConstant.PENDING_PAY) {
            // 未支付，不退款：0 → 5
            int rows = orderMapper.update(null, new LambdaUpdateWrapper<ConsultationOrder>()
                    .eq(ConsultationOrder::getId, consultationOrder.getId())
                    .eq(ConsultationOrder::getOrderStatus, OrderStatusConstant.PENDING_PAY)
                    .set(ConsultationOrder::getOrderStatus, OrderStatusConstant.USER_ABANDONED)
                    .set(ConsultationOrder::getCloseTime, now));
            if (rows == 0) {
                throw new BizException(ResultCode.ORDER_STATUS_ERROR);
            }
        } else if (consultationOrder.getOrderStatus() == OrderStatusConstant.PENDING_ACCEPT) {
            // 已支付未接诊，全额退款：1 → 7
            int rows = orderMapper.update(null, new LambdaUpdateWrapper<ConsultationOrder>()
                    .eq(ConsultationOrder::getId, consultationOrder.getId())
                    .eq(ConsultationOrder::getOrderStatus, OrderStatusConstant.PENDING_ACCEPT)
                    .set(ConsultationOrder::getOrderStatus, OrderStatusConstant.USER_CANCELLED)
                    .set(ConsultationOrder::getCloseTime, now));
            if (rows == 0) {
                throw new BizException(ResultCode.ORDER_STATUS_ERROR);
            }

            // 更新支付流水状态为已退款
            paymentRecordMapper.update(null, new LambdaUpdateWrapper<PaymentRecord>()
                    .eq(PaymentRecord::getPayStatus, PayStatusConstant.SUCCESS)
                    .eq(PaymentRecord::getOrderId, consultationOrder.getId())
                    .set(PaymentRecord::getPayStatus, PayStatusConstant.REFUNDED)
                    .set(PaymentRecord::getRefundTime, now));
        } else {
            // 2 问诊中、3 已完成、各种关闭态：不允许取消
            throw new BizException(ResultCode.ORDER_STATUS_ERROR);
        }
    }

    @Override
    public DetailOrderVO getOrderDetail(Long orderId) {
        Long patientId = UserContext.getUserId();

        // 1. 查订单并校验归属：仅患者本人可见，不匹配一律按「订单不存在」处理，避免枚举他人订单
        ConsultationOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getPatientId().equals(patientId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }

        // 2. 补患者、医生、科室名称
        User patient = userMapper.selectById(order.getPatientId());
        User doctorUser = userMapper.selectById(order.getDoctorId());
        Department dept = departmentMapper.selectById(order.getDeptId());

        // 3. 查问诊表单与处方（均为 1:1，未开处方时 prescription 为 null）
        ConsultForm form = consultFormMapper.selectOne(
                new LambdaQueryWrapper<ConsultForm>().eq(ConsultForm::getOrderId, orderId));
        Prescription prescription = prescriptionMapper.selectOne(
                new LambdaQueryWrapper<Prescription>().eq(Prescription::getOrderId, orderId));

        // 4. 组装 VO
        DetailOrderVO vo = new DetailOrderVO();
        vo.setOrderId(String.valueOf(order.getId()));
        vo.setOrderNo(order.getOrderNo());
        vo.setPatientId(String.valueOf(order.getPatientId()));
        vo.setPatientName(patient != null ? patient.getRealName() : null);
        vo.setDoctorId(String.valueOf(order.getDoctorId()));
        vo.setDoctorName(doctorUser != null ? doctorUser.getRealName() : null);
        vo.setDeptId(String.valueOf(order.getDeptId()));
        vo.setDeptName(dept != null ? dept.getName() : null);
        vo.setOrderStatus(order.getOrderStatus());
        vo.setOrderStatusName(OrderStatusConstant.getName(order.getOrderStatus()));
        vo.setAmount(order.getAmount());
        vo.setPayTime(order.getPayTime());
        vo.setAcceptTime(order.getAcceptTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCreateTime(order.getCreateTime());

        // 表单、处方只暴露业务字段，不透出 id、isDeleted、doctorId 等内部列
        if (form != null) {
            ConsultFormVO formVO = new ConsultFormVO();
            formVO.setOrderId(String.valueOf(order.getId()));
            formVO.setSymptom(form.getSymptom());
            formVO.setImgUrl(form.getImgUrl());
            vo.setConsultForm(formVO);
        }
        if (prescription != null) {
            PrescriptionVO prescriptionVO = new PrescriptionVO();
            prescriptionVO.setPrescriptionId(String.valueOf(prescription.getId()));
            prescriptionVO.setOrderId(String.valueOf(order.getId()));
            prescriptionVO.setDoctorName(vo.getDoctorName());
            prescriptionVO.setPrescriptionNo(prescription.getPrescriptionNo());
            prescriptionVO.setAdvice(prescription.getAdvice());
            prescriptionVO.setDrugInfo(prescription.getDrugInfo());
            prescriptionVO.setCreateTime(prescription.getCreateTime());
            vo.setPrescription(prescriptionVO);
        }
        return vo;
    }

    @Override
    public PageResult<OrderListVO> getMyOrderList(OrderListDTO dto) {
        Long patientId = UserContext.getUserId();

        // 1. 分页查自己的订单：status 传了才过滤；create_time 可能同秒，加主键兜底保证翻页稳定
        IPage<ConsultationOrder> page = orderMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()),
                new LambdaQueryWrapper<ConsultationOrder>()
                        .eq(ConsultationOrder::getPatientId, patientId)
                        .eq(dto.getStatus() != null, ConsultationOrder::getOrderStatus, dto.getStatus())
                        .orderByDesc(ConsultationOrder::getCreateTime)
                        .orderByAsc(ConsultationOrder::getId)
        );

        List<ConsultationOrder> orders = page.getRecords();

        // 2. 收集本页的医生ID、科室ID，各批量查一次，避免逐条查询的 N+1
        List<Long> doctorIds = new ArrayList<>(orders.size());
        List<Long> deptIds = new ArrayList<>(orders.size());
        for (ConsultationOrder order : orders) {
            doctorIds.add(order.getDoctorId());
            deptIds.add(order.getDeptId());
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(doctorIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        Map<Long, Department> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Department> depts = departmentMapper.selectBatchIds(deptIds);
            for (Department dept : depts) {
                deptMap.put(dept.getId(), dept);
            }
        }

        // 3. 组装 VO 列表
        List<OrderListVO> voList = new ArrayList<>(orders.size());
        for (ConsultationOrder order : orders) {
            User doctorUser = userMap.get(order.getDoctorId());
            Department dept = deptMap.get(order.getDeptId());

            OrderListVO vo = new OrderListVO();
            vo.setOrderId(String.valueOf(order.getId()));
            vo.setOrderNo(order.getOrderNo());
            vo.setDoctorName(doctorUser != null ? doctorUser.getRealName() : null);
            vo.setDeptName(dept != null ? dept.getName() : null);
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderStatusName(OrderStatusConstant.getName(order.getOrderStatus()));
            vo.setAmount(order.getAmount());
            vo.setCreateTime(order.getCreateTime());
            voList.add(vo);
        }

        // 4. 分页元信息从 IPage 拿，records 换成转好的 VO
        PageResult<OrderListVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
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

    /**
     * 生成支付流水单号：PAY + yyyyMMddHHmmss + 4位随机数
     * 表上有 uk_out_trade_no 唯一索引，用作支付幂等兜底
     */
    private String generatePayNo() {
        return PAY_NO_PREFIX
                + LocalDateTime.now().format(ORDER_NO_TIME)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
