package com.community.communitycp.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.core.PageResult;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.doctor.dto.DoctorQueryDTO;
import com.community.communitycp.doctor.entity.Doctor;
import com.community.communitycp.doctor.mapper.DoctorMapper;
import com.community.communitycp.doctor.service.DoctorService;
import com.community.communitycp.doctor.vo.DoctorListVO;
import com.community.communitycp.user.entity.User;
import com.community.communitycp.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public DoctorServiceImpl(DoctorMapper doctorMapper, UserMapper userMapper,
                             RedisService redisService, ObjectMapper objectMapper) {
        this.doctorMapper = doctorMapper;
        this.userMapper = userMapper;
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
