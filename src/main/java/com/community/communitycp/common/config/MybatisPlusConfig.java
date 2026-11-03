package com.community.communitycp.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：分页插件
 * 3.4.0+ 新版用 MybatisPlusInterceptor + PaginationInnerInterceptor
 * 旧版 PaginationInterceptor 已废弃，别用错
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // MySQL 分页插件
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最多 500 条，防止有人传 pageSize=99999 把库拖死
        pageInterceptor.setMaxLimit(500L);
        // 溢出总页数处理：请求 page > 总页数时，false 返回空数据（前端更容易判断到底了）
        pageInterceptor.setOverflow(false);

        interceptor.addInnerInterceptor(pageInterceptor);
        return interceptor;
    }
}
