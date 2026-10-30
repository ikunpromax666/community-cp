/**
 * com.community.communitycp.user.entity
 */
package com.community.communitycp.user.entity;


import com.baomidou.mybatisplus.annotation.IdType;
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 软删除标志
     */
    @TableLogic
    private Integer isDelete;

}
