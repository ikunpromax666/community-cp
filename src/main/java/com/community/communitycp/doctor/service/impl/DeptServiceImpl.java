/**
 * com.community.communitycp.doctor.service.impl
 */
package com.community.communitycp.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.communitycp.common.constant.RedisKeyConstant;
import com.community.communitycp.common.redis.RedisService;
import com.community.communitycp.doctor.entity.Department;
import com.community.communitycp.doctor.mapper.DepartmentMapper;
import com.community.communitycp.doctor.service.DeptService;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class DeptServiceImpl implements DeptService {

    /**
     * 缓存反序列化用的完整泛型类型，避免 readValue(json, List.class) 把元素转成 LinkedHashMap
     */
    private static final TypeReference<List<Department>> DEPT_LIST_TYPE = new TypeReference<>() {
    };

    private final DepartmentMapper departmentMapper;

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public DeptServiceImpl(DepartmentMapper departmentMapper, RedisService redisService, ObjectMapper objectMapper) {
        this.departmentMapper = departmentMapper;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Department> list() {
        String key = RedisKeyConstant.deptListKey();
        //先查缓存
        String json = redisService.get(key);

        if (json != null) {
            // 空值防穿透：缓存了 "[]" 说明 DB 就是空的
            if ("[]".equals(json)) {
                return Collections.emptyList();
            }
            // 缓存了非空值，直接返回
            return objectMapper.readValue(json, DEPT_LIST_TYPE);
        }

        // 缓存未命中，从 DB 查询
        List<Department> list = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSort)
                );

        // 3. 写缓存：空值 5min，正常值 30min + 随机偏移
        long ttl = list.isEmpty()
                ? 5L // 空值 5min 空值防穿透，短过期
                : 30L + ThreadLocalRandom.current().nextLong(5); // 正常值 30min + 随机偏移，防雪崩
        redisService.set(key,objectMapper.writeValueAsString(list),ttl, TimeUnit.MINUTES);

        return list;
    }
}
