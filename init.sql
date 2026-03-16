-- 1. 如果不存在则创建数据库
CREATE DATABASE IF NOT EXISTS `mental_health_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mental_health_db`;

-- ----------------------------
-- 2. 匿名用户表 (存储账号密码及加密信息)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `username` varchar(50) NOT NULL COMMENT '匿名用户名/账号',
    `password` varchar(100) NOT NULL COMMENT 'BCrypt加密后的密码密文',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='匿名用户表';

-- ----------------------------
-- 3. AI聊天记录表 (存储多轮对话的上下文)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                              `user_id` bigint NOT NULL COMMENT '所属用户ID(关联user表id)',
                                              `role` varchar(20) NOT NULL COMMENT '角色: user(用户) 或 assistant(AI助手)',
    `content` text NOT NULL COMMENT '对话的具体文本内容',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) -- 索引：大幅提升查询特定用户聊天记录的速度
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天记录表';