package com.community.communitycp.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 接口文档配置
 * 访问地址：http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    /**
     * 安全方案名称（JWT Bearer Token）
     */
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI communityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("社区问诊平台 API")
                        .description("社区在线问诊平台后端接口文档，包含用户、医生、订单、问诊、评价、管理后台等模块。")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("社区问诊平台")
                                .email("support@community.com")))
                // 全局安全方案：所有接口都可以在 Swagger UI 右上角填入 Token
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("输入登录接口返回的 Token（无需加 Bearer 前缀，Swagger 会自动添加）")));
    }
}
