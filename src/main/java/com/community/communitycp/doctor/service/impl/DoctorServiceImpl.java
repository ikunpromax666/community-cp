package com.community.communitycp.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.core.BizException;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.core.ResultCode;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.common.web.UserContext;
import com.community.communitycp.doctor.dto.DoctorQueryDTO;
import com.community.communitycp.doctor.dto.OnlineStatusDTO;
import com.community.communitycp.doctor.entity.Department;
import com.community.communitycp.doctor.entity.Doctor;
import com.community.communitycp.doctor.mapper.DepartmentMapper;
import com.community.communitycp.doctor.mapper.DoctorMapper;
import com.community.communitycp.doctor.service.DoctorService;
import com.community.communitycp.doctor.vo.DoctorDetailVO;
import com.community.communitycp.doctor.vo.DoctorListVO;
import com.community.communitycp.doctor.vo.DoctorProfileVO;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: DoctorServiceImpl
 * Description:
 * Author: SuperXia
 * Datetime :2026/11/2 15:30
 * Version:1.0
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    /**
     * 缓存反序列化用的完整泛型类型，避免 readValue(json, List.class) 把元素转成 LinkedHashMap
     */
    private static final TypeReference<PageResult<DoctorListVO>> DOCTOR_PAGE_TYPE = new TypeReference<>() {
    };

    /**
     * 互斥锁持有时间（秒）：防止业务线程崩溃后锁永久不释放
     */
    private static final long LOCK_EXPIRE_SECONDS = 10L;

    /**
     * 空结果缓存时间（分钟）：防穿透，短过期
     */
    private static final long EMPTY_CACHE_MINUTES = 5L;

    /**
     * 正常结果基础缓存时间（分钟）
     */
    private static final long CACHE_MINUTES = 10L;

    /**
     * 等待其他线程重建缓存的最大重试次数
     */
    private static final int MAX_RETRY = 3;

    /**
     * 每次重试前等待的毫秒数
     */
    private static final long RETRY_SLEEP_MILLIS = 50L;

    /**
     * 医生不存在时缓存的空标记：反序列化后 userId 为 null，用来区分「缓存没命中」和「库里确实没有」
     */
    private static final String EMPTY_DOCTOR_MARK = "{}";

    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public DoctorServiceImpl(DoctorMapper doctorMapper, UserMapper userMapper,
                             DepartmentMapper departmentMapper, RedisService redisService,
                             ObjectMapper objectMapper) {
        this.doctorMapper = doctorMapper;
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<DoctorListVO> getDoctorList(DoctorQueryDTO queryDTO) {
        // 拼缓存 key：doctor:dept:{deptId}:p{pageNum}:s{pageSize}
        String cacheKey = RedisKeyConstant.doctorDeptPageKey(
                queryDTO.getDeptId(), queryDTO.getPageNum(), queryDTO.getPageSize());

        // 1. 先查缓存
        PageResult<DoctorListVO> cached = readCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，尝试获取互斥锁（防止热点 key 失效瞬间大量请求打到 DB）
        String lockKey = RedisKeyConstant.doctorDeptLockKey(queryDTO.getDeptId());
        String lockToken = redisService.tryLock(lockKey, LOCK_EXPIRE_SECONDS);

        // 3. 没抢到锁：说明其他线程正在重建缓存，稍等后重试读缓存，超过次数直接查 DB 兜底
        if (lockToken == null) {
            for (int i = 0; i < MAX_RETRY; i++) {
                sleep(RETRY_SLEEP_MILLIS);
                PageResult<DoctorListVO> retried = readCache(cacheKey);
                if (retried != null) {
                    return retried;
                }
            }
            return queryFromDb(queryDTO);
        }

        try {
            // 4. Double Check：从"未命中"到"拿到锁"之间，其他线程可能已经写完缓存
            PageResult<DoctorListVO> doubleChecked = readCache(cacheKey);
            if (doubleChecked != null) {
                return doubleChecked;
            }

            // 5. 查 DB 并组装 VO
            PageResult<DoctorListVO> result = queryFromDb(queryDTO);

            // 6. 写缓存：空结果短 TTL 防穿透；正常结果加随机偏移防雪崩
            long ttlMinutes = result.getRecords().isEmpty()
                    ? EMPTY_CACHE_MINUTES
                    : CACHE_MINUTES + ThreadLocalRandom.current().nextLong(3);
            redisService.set(cacheKey, objectMapper.writeValueAsString(result), ttlMinutes, TimeUnit.MINUTES);

            return result;
        } finally {
            // 7. 必须在 finally 释放，否则业务异常时会等到锁自然过期
            redisService.unlock(lockKey, lockToken);
        }
    }

    @Override
    public DoctorDetailVO getDoctorDetail(Long userId) {
        String key = RedisKeyConstant.doctorDetailKey(userId);

        // 1. 先查缓存
        String json = redisService.get(key);
        if (json != null) {
            // 空标记 "{}" 反序列化后 userId 为 null，说明这个医生不存在
            DoctorDetailVO cached = objectMapper.readValue(json, DoctorDetailVO.class);
            if (cached == null || cached.getUserId() == null) {
                throw new BizException(ResultCode.DOCTOR_NOT_FOUND);
            }
            return cached;
        }

        // 2. 缓存未命中，按 user_id 查 doctor（入参是 user.id，不是 doctor 表主键）
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            // 空值防穿透：缓存空标记 5 分钟
            redisService.set(key, EMPTY_DOCTOR_MARK, EMPTY_CACHE_MINUTES, TimeUnit.MINUTES);
            throw new BizException(ResultCode.DOCTOR_NOT_FOUND);
        }

        // 3. 补 user（姓名、头像）和 department（科室名称）
        User user = userMapper.selectById(userId);
        Department dept = departmentMapper.selectById(doctor.getDeptId());

        // 4. 组装详情 VO（只暴露详情页需要的字段）
        DoctorDetailVO vo = new DoctorDetailVO();
        vo.setUserId(userId);
        vo.setRealName(user != null ? user.getRealName() : null);
        vo.setAvatar(user != null ? user.getAvatar() : null);
        vo.setDeptId(doctor.getDeptId());
        vo.setDeptName(dept != null ? dept.getName() : null);
        vo.setTitle(doctor.getTitle());
        vo.setSpecialty(doctor.getSpecialty());
        vo.setConsultPrice(doctor.getConsultPrice());
        vo.setOnlineStatus(doctor.getOnlineStatus());
        vo.setConsultStatus(doctor.getConsultStatus());
        vo.setRatingAvg(doctor.getRatingAvg());
        vo.setRatingCount(doctor.getRatingCount());

        // 5. 写缓存（加随机偏移，防缓存雪崩）
        long ttl = CACHE_MINUTES + ThreadLocalRandom.current().nextLong(3);
        redisService.set(key, objectMapper.writeValueAsString(vo), ttl, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public DoctorProfileVO getDoctorProfile(Long userId) {
        // 医生看自己的资料，调用频率低且在线/问诊状态会变，不做缓存，直接查库
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            throw new BizException(ResultCode.DOCTOR_NOT_FOUND);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }

        Department dept = departmentMapper.selectById(doctor.getDeptId());

        DoctorProfileVO vo = new DoctorProfileVO();
        vo.setUserId(userId);
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setDeptId(doctor.getDeptId());
        vo.setDeptName(dept != null ? dept.getName() : null);
        vo.setTitle(doctor.getTitle());
        vo.setSpecialty(doctor.getSpecialty());
        vo.setConsultPrice(doctor.getConsultPrice());
        vo.setOnlineStatus(doctor.getOnlineStatus());
        vo.setConsultStatus(doctor.getConsultStatus());
        vo.setRatingAvg(doctor.getRatingAvg());
        vo.setRatingCount(doctor.getRatingCount());
        return vo;
    }

    @Override
    public void updateOnlineStatus(OnlineStatusDTO onlineStatusDTO) {
        // 当前登录医生的 userId，不接受前端传参，避免改别人的状态
        Long userId = UserContext.getUserId();

        // 1. 按 user_id 查 doctor 记录（doctor 表主键不是 user.id，不能用 selectById）
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            throw new BizException(ResultCode.DOCTOR_NOT_FOUND);
        }

        // 2. 状态没变直接返回，省一次写库和缓存清理
        if (Objects.equals(doctor.getOnlineStatus(), onlineStatusDTO.getOnlineStatus())) {
            return;
        }

        // 3. 先更新 DB，再删缓存（避免并发读把旧值回填进缓存）
        doctor.setOnlineStatus(onlineStatusDTO.getOnlineStatus());
        doctorMapper.updateById(doctor);

        // 4. 详情缓存失效
        redisService.delete(RedisKeyConstant.doctorDetailKey(userId));

        // 5. 该科室的列表缓存失效（下线后不应再出现在列表里）
        redisService.deleteByPrefix(RedisKeyConstant.doctorDeptPrefix(doctor.getDeptId()));
    }

    /**
     * 手机号脱敏：13812341234 -> 138****1234
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }


    /**
     * 读缓存：未命中返回 null；命中（包括空列表）返回反序列化后的分页结果
     */
    private PageResult<DoctorListVO> readCache(String cacheKey) {
        String json = redisService.get(cacheKey);
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, DOCTOR_PAGE_TYPE);
    }

    /**
     * 查 DB：分页查 doctor + 批量查 user，避免 N+1
     */
    private PageResult<DoctorListVO> queryFromDb(DoctorQueryDTO queryDTO) {
        // 1. 分页查 doctor 实体（条件：科室 + 在线 + 空闲，兜底排序保证分页稳定）
        IPage<Doctor> page = doctorMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getDeptId, queryDTO.getDeptId())
                        .eq(Doctor::getOnlineStatus, 1)
                        .eq(Doctor::getConsultStatus, 0)
                        .orderByDesc(Doctor::getRatingAvg)
                        .orderByAsc(Doctor::getId)
        );

        List<Doctor> doctors = page.getRecords();

        // 2. 收集 userId，批量查 user（1 条 SQL，避免 N+1）
        List<Long> userIds = new ArrayList<>(doctors.size());
        for (Doctor d : doctors) {
            userIds.add(d.getUserId());
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        // 3. 把每个 Doctor 转成 DoctorListVO，塞进新 PageResult
        List<DoctorListVO> voList = new ArrayList<>(doctors.size());
        for (Doctor d : doctors) {
            User u = userMap.get(d.getUserId());
            DoctorListVO vo = new DoctorListVO();
            vo.setDoctorId(d.getUserId());
            vo.setRealName(u != null ? u.getRealName() : null);
            vo.setAvatar(u != null ? u.getAvatar() : null);
            vo.setTitle(d.getTitle());
            vo.setSpecialty(d.getSpecialty());
            vo.setConsultPrice(d.getConsultPrice());
            vo.setRatingAvg(d.getRatingAvg());
            vo.setRatingCount(d.getRatingCount());
            voList.add(vo);
        }

        // 4. 组装返回：分页元信息从 IPage 拿，records 是刚转好的 VO 列表
        PageResult<DoctorListVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
