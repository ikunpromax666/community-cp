package com.community.communitycp.common.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作封装
 */
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 写入并设置过期时间
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 读取
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 批量删除（缓存按前缀失效时用）
     */
    public void delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    /**
     * 按前缀删除（项目数据量小可用 keys 命令；生产建议改用 SCAN 增量删除）
     */
    public void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 释放锁脚本：先比对 value 与 token 是否一致，一致才删除。
     * 用 Lua 保证“比对 + 删除”在 Redis 端原子执行，避免删除别人持有的锁。
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    /**
     * 尝试获取分布式锁（SET key token NX EX expireSec，原子加锁）
     *
     * @param key       锁 key，如 "lock:doctor:dept:{deptId}"
     * @param expireSec 锁持有时间（秒），防业务线程崩溃后永久锁住
     * @return 加锁成功返回锁凭证 token（释放锁时要传回来），失败返回 null
     */
    public String tryLock(String key, long expireSec) {
        String token = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, token, expireSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success) ? token : null;
    }

    /**
     * 释放分布式锁：只有 token 与锁内 value 一致才会删除
     *
     * @param key   锁 key
     * @param token tryLock 返回的锁凭证
     * @return true=释放成功，false=锁已过期或已被他人持有
     */
    public boolean unlock(String key, String token) {
        if (token == null) {
            return false;
        }
        Long result = redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
        return Long.valueOf(1L).equals(result);
    }
}
