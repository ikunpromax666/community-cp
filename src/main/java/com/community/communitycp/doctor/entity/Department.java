package com.community.communitycp.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科室表
 */
@Data
@TableName("department")
public class Department {

    /**
     * 雪花主键ID，全局唯一
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 科室名称，如内科、皮肤科
     */
    private String name;

    /**
     * 科室简介，介绍诊疗范围
     */
    private String intro;

    /**
     * 展示排序，越小越靠前
     */
    private Integer sort;

    /**
     * 启用状态：0=禁用，1=启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 软删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;
}
