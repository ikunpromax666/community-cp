package com.community.communitycp.common.config;

import com.community.communitycp.common.web.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 配置：注册拦截器等
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")                 // 拦截所有请求
                .excludePathPatterns(                   // 放行以下路径
                        "/user/register",              // 用户注册
                        "/user/login",                 // 用户登录
                        "/doctor/list",                // 医生列表（公开）
                        "/doctor/detail/**",           // 医生详情（公开）
                        "/department/list",            // 科室列表（公开）
                        "/swagger-ui.html",            // Swagger UI 入口
                        "/swagger-ui/**",              // Swagger UI 静态资源
                        "/v3/api-docs/**",             // OpenAPI 文档数据
                        "/swagger-resources/**",       // Swagger 资源
                        "/webjars/**",                 // Swagger 依赖的 webjar
                        "/error",                      // Spring Boot 错误页
                        "/uploads/**"                  // 静态资源
                );
    }
}
