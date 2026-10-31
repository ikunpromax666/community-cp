/**
 * com.community.communitycp.user.entity
 */
package com.community.communitycp.user.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("`user`")
@Data
public class User {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 头像URL
     */
    private String avatar;
     /**
      * 角色
      */
    private Integer role;
    /**
     * 真实姓名
     */
    private String realName;
     /**
      * 身份证号
      */
    private String idCard;
     /**
      * 是否实名认证
      */
    private Integer isAuth;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 软删除标志：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDeleted;

}
