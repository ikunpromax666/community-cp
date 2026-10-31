-- ============================================================
-- 社区问诊平台 数据库建表脚本（最终版）
-- 技术栈：Spring Boot + MyBatis-Plus + MySQL + Redis
-- 约定：无物理外键，逻辑关联；雪花 BIGINT 主键；软删除用 is_deleted
-- ============================================================

-- 幂等：先删除已存在的表，保证脚本可重复执行（开发环境用，会清空表数据）
DROP TABLE IF EXISTS `review`;
DROP TABLE IF EXISTS `payment_record`;
DROP TABLE IF EXISTS `prescription`;
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `consult_form`;
DROP TABLE IF EXISTS `consultation_order`;
DROP TABLE IF EXISTS `doctor`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `department`;

-- 1. 科室表 department
CREATE TABLE `department` (
  `id`          BIGINT       NOT NULL                      COMMENT '雪花主键ID，全局唯一',
  `name`        VARCHAR(50)  NOT NULL                      COMMENT '科室名称，如内科、皮肤科，前端选科室展示用',
  `intro`       VARCHAR(500) DEFAULT NULL                  COMMENT '科室简介，介绍诊疗范围，非必填',
  `sort`        INT          NOT NULL DEFAULT 0            COMMENT '展示排序，越小越靠前',
  `status`      TINYINT      NOT NULL DEFAULT 1            COMMENT '启用状态：0=禁用(临时下线)，1=启用(前端正常展示)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后修改时间',
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0            COMMENT '软删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';


-- 2. 用户表 user（仅存公共字段，医生扩展信息放 doctor 表）
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL                      COMMENT '雪花主键ID，全局唯一',
  `username`    VARCHAR(50)  NOT NULL                      COMMENT '登录账号，唯一，用于登录校验',
  `password`    VARCHAR(100) NOT NULL                      COMMENT '登录密码，BCrypt加密存储',
  `phone`       VARCHAR(20)  NOT NULL                      COMMENT '手机号，唯一，用于注册登录和接收通知',
  `avatar`      VARCHAR(255) DEFAULT NULL                  COMMENT '头像URL，用户上传后存储访问地址',
  `role`        TINYINT      NOT NULL DEFAULT 1            COMMENT '用户角色：1=患者，2=医生，3=管理员',
  `real_name`   VARCHAR(20)  DEFAULT NULL                  COMMENT '真实姓名，实名展示',
  `id_card`     VARCHAR(18)  DEFAULT NULL                  COMMENT '身份证号，用于实名校验，返回时脱敏',
  `is_auth`     TINYINT      NOT NULL DEFAULT 0            COMMENT '实名状态：0=未实名，1=已实名',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账号信息最后修改时间',
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0            COMMENT '软删除：0=正常，1=删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 3. 医生扩展表 doctor（与 user 1:1，仅 role=2 的用户有对应记录）
CREATE TABLE `doctor` (
  `id`            BIGINT        NOT NULL                    COMMENT '雪花主键ID',
  `user_id`       BIGINT        NOT NULL                    COMMENT '关联user.id，1:1',
  `dept_id`       BIGINT        NOT NULL                    COMMENT '归属科室ID，关联department.id',
  `title`         VARCHAR(20)   DEFAULT NULL                COMMENT '医生职称，如主治医师、主任医师',
  `specialty`     VARCHAR(200)  DEFAULT NULL                COMMENT '医生擅长领域，医生详情页展示',
  `consult_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00       COMMENT '图文问诊价格，下单时取此值快照写入订单',
  `online_status` TINYINT       NOT NULL DEFAULT 1          COMMENT '在线/接诊开关：0=离线不接单，1=在线可接单，医生手动切换',
  `consult_status` TINYINT      NOT NULL DEFAULT 0          COMMENT '问诊状态：0=空闲可接诊，1=问诊中不可再接新单，系统自动维护',
  `rating_avg`    DECIMAL(3,2)  NOT NULL DEFAULT 5.00       COMMENT '评分均值，冗余字段避免每次聚合',
  `rating_count`  INT           NOT NULL DEFAULT 0          COMMENT '评价数，冗余字段',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `is_deleted`    TINYINT       NOT NULL DEFAULT 0          COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_dept_online` (`dept_id`, `online_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生扩展表';


-- 4. 问诊订单表 consultation_order（核心主表）
CREATE TABLE `consultation_order` (
  `id`           BIGINT        NOT NULL                    COMMENT '雪花主键ID，订单唯一标识',
  `order_no`     VARCHAR(32)   NOT NULL                    COMMENT '业务订单号，给用户看的单号，和主键分离',
  `patient_id`   BIGINT        NOT NULL                    COMMENT '患者ID，关联user.id',
  `doctor_id`    BIGINT        NOT NULL                    COMMENT '医生ID，关联user.id',
  `dept_id`      BIGINT        NOT NULL                    COMMENT '科室ID，关联department.id',
  `order_status` TINYINT       NOT NULL DEFAULT 0          COMMENT '订单状态：0待支付,1待接诊,2问诊中,3已完成,4超时关闭,5用户放弃关闭,6拒诊/接诊超时退款关闭,7用户取消退款关闭',
  `amount`       DECIMAL(10,2) NOT NULL                    COMMENT '问诊费用，下单时取医生价格快照写入',
  `pay_time`     DATETIME      DEFAULT NULL                COMMENT '支付成功时间，仅支付成功时赋值一次',
  `accept_time`  DATETIME      DEFAULT NULL                COMMENT '医生接诊时间，医生接单时赋值一次',
  `finish_time`  DATETIME      DEFAULT NULL                COMMENT '问诊完成时间，问诊结束时赋值一次',
  `close_time`   DATETIME      DEFAULT NULL                COMMENT '订单关闭时间，超时/拒诊/放弃关闭时赋值一次',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单最后修改时间',
  `is_deleted`   TINYINT       NOT NULL DEFAULT 0          COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_patient_status` (`patient_id`, `order_status`),
  KEY `idx_doctor_status` (`doctor_id`, `order_status`),
  KEY `idx_status_create` (`order_status`, `create_time`),
  KEY `idx_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊订单表';


-- 5. 问诊表单表 consult_form（与订单 1:1）
CREATE TABLE `consult_form` (
  `id`          BIGINT        NOT NULL                      COMMENT '雪花主键ID',
  `order_id`    BIGINT        NOT NULL                      COMMENT '问诊订单ID，关联consultation_order.id',
  `symptom`     VARCHAR(1000) NOT NULL                      COMMENT '症状描述，含症状、患病时长、既往病史，用户自由输入',
  `img_url`     VARCHAR(255)  DEFAULT NULL                  COMMENT '症状图片地址，可选，上传检查单/患处照片',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '表单提交时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '表单修改时间',
  `is_deleted`  TINYINT       NOT NULL DEFAULT 0            COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊表单表';


-- 6. 聊天消息表 message
CREATE TABLE `message` (
  `id`            BIGINT        NOT NULL                    COMMENT '全局雪花主键ID',
  `order_id`      BIGINT        NOT NULL                    COMMENT '问诊订单ID，消息归属哪一场问诊会话',
  `seq`           BIGINT        NOT NULL                    COMMENT '会话内局部消息序号，Redis INCR生成，用于顺序/断点续拉',
  `client_msg_id` VARCHAR(64)   NOT NULL                    COMMENT '客户端消息ID，前端生成，幂等防重，防止网络重试重复发送',
  `sender_id`     BIGINT        NOT NULL                    COMMENT '发送人ID，关联user.id',
  `sender_role`   TINYINT       NOT NULL                    COMMENT '发送者角色：1=患者，2=医生，减少联表查询',
  `msg_type`      TINYINT       NOT NULL DEFAULT 1          COMMENT '消息类型：1=文本，2=图片',
  `content`       VARCHAR(2000) DEFAULT NULL                COMMENT '消息内容，文本存文字，图片存图片地址',
  `extra`         JSON          DEFAULT NULL                COMMENT '扩展字段，JSON格式，预留图片宽高、缩略图等',
  `msg_status`    TINYINT       NOT NULL DEFAULT 1          COMMENT '消息状态：1=正常，2=已撤回',
  `create_time`   DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '消息发送时间，毫秒级，聊天时序更精准',
  `is_deleted`    TINYINT       NOT NULL DEFAULT 0          COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_client` (`order_id`, `client_msg_id`),
  UNIQUE KEY `uk_order_seq` (`order_id`, `seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医患聊天消息表';


-- 7. 电子处方表 prescription（与订单 1:1，一单一处方，不可修改，无status字段）
CREATE TABLE `prescription` (
  `id`             BIGINT       NOT NULL                   COMMENT '雪花主键ID',
  `order_id`       BIGINT       NOT NULL                   COMMENT '问诊订单ID，关联consultation_order.id',
  `doctor_id`      BIGINT       NOT NULL                   COMMENT '开处方医生ID，关联user.id',
  `prescription_no` VARCHAR(32) NOT NULL                   COMMENT '处方编号，业务展示用，全局唯一',
  `advice`         TEXT         DEFAULT NULL               COMMENT '医嘱，医生文字叮嘱',
  `drug_info`      TEXT         DEFAULT NULL               COMMENT '药品信息，药品名称/规格/用量，文本存储',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处方开具时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处方修改时间',
  `is_deleted`     TINYINT      NOT NULL DEFAULT 0         COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  UNIQUE KEY `uk_prescription_no` (`prescription_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子处方表';


-- 8. 支付流水表 payment_record（模拟支付，无第三方回调，不做软删除）
CREATE TABLE `payment_record` (
  `id`           BIGINT        NOT NULL                     COMMENT '雪花主键ID',
  `order_id`     BIGINT        NOT NULL                     COMMENT '问诊订单ID，关联consultation_order.id',
  `out_trade_no` VARCHAR(64)   NOT NULL                     COMMENT '本地业务支付单号，平台生成，用于幂等控制',
  `amount`       DECIMAL(10,2) NOT NULL                     COMMENT '本次支付金额',
  `pay_channel`  VARCHAR(20)   DEFAULT NULL                 COMMENT '模拟支付渠道：微信/支付宝，仅前端展示',
  `pay_status`   TINYINT       NOT NULL DEFAULT 0           COMMENT '支付状态：0=待支付，1=支付成功，2=支付失败，3=已退款',
  `pay_time`     DATETIME      DEFAULT NULL                 COMMENT '支付成功时间',
  `refund_time`  DATETIME      DEFAULT NULL                 COMMENT '退款完成时间，全额退款时赋值',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '支付流水创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '流水状态修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表（模拟支付，无第三方回调）';


-- 9. 评价表 review（与订单 1:1）
CREATE TABLE `review` (
  `id`          BIGINT        NOT NULL                      COMMENT '雪花主键ID',
  `order_id`    BIGINT        NOT NULL                      COMMENT '问诊订单ID，关联consultation_order.id',
  `patient_id`  BIGINT        NOT NULL                      COMMENT '评价人（患者）ID，关联user.id',
  `doctor_id`   BIGINT        NOT NULL                      COMMENT '被评价医生ID，关联user.id',
  `score`       TINYINT       NOT NULL                      COMMENT '评分，1~5分',
  `content`     VARCHAR(500)  DEFAULT NULL                  COMMENT '评价文字内容，可选',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价提交时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评价修改时间',
  `is_deleted`  TINYINT       NOT NULL DEFAULT 0            COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_doctor_time` (`doctor_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊评价表';
