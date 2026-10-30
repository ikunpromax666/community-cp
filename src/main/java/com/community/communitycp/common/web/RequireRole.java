package com.community.communitycp.common.web;

import com.community.communitycp.common.constant.UserRoleConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 标注在 Controller 方法上，指定该接口允许的角色
 * 例：@RequireRole({UserRoleConstant.DOCTOR, UserRoleConstant.ADMIN})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 允许的角色列表，默认允许所有已登录用户
     */
    int[] value() default {UserRoleConstant.PATIENT, UserRoleConstant.DOCTOR, UserRoleConstant.ADMIN};
}
