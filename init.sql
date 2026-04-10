-- ==========================================================
-- 心理支持对话系统 (Mental Health AI App) 数据库初始化脚本
-- 字符集: utf8mb4 (支持各种复杂的表情符号)
-- ==========================================================

-- 1. 如果不存在则创建数据库
CREATE DATABASE IF NOT EXISTS `mental_health_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mental_health_db`;

-- ==========================================================
-- ⚠️ 警告：以下三行会在重新运行脚本时清空旧表，确保结构为最新。
-- 如果将来部署到生产环境或已有真实数据，请务必注释掉这三行！
-- ==========================================================
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `chat_session`;
DROP TABLE IF EXISTS `user`;


-- ----------------------------------------------------------
-- 2. 匿名用户表 (存储账号密码及加密信息)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
                                      `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `username`    varchar(50)  NOT NULL COMMENT '匿名用户名/账号',
                                      `password`    varchar(100) NOT NULL COMMENT 'BCrypt加密后的密码密文',
                                      `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='匿名用户表';


-- ----------------------------------------------------------
-- 3. 聊天会话表 (用于左侧边栏显示多个历史对话)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_session` (
                                              `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
                                              `user_id`     bigint       NOT NULL COMMENT '所属用户ID(关联user表id)',
                                              `title`       varchar(100) NOT NULL COMMENT '会话标题(如：考研压力咨询)',
                                              `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              PRIMARY KEY (`id`),
                                              KEY `idx_user_id` (`user_id`) -- 索引：大幅提升查询某用户所有会话的速度
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';


-- ----------------------------------------------------------
-- 4. AI聊天记录表 (存储多轮对话的具体内容)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
                                              `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                              `session_id`  bigint       NOT NULL COMMENT '所属会话ID(关联chat_session表)',
                                              `user_id`     bigint       NOT NULL COMMENT '所属用户ID(冗余字段，方便统计)',
                                              `role`        varchar(20)  NOT NULL COMMENT '角色: user(用户) 或 assistant(AI助手)',
                                              `content`     text         NOT NULL COMMENT '对话的具体文本内容',
                                              `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                              PRIMARY KEY (`id`),
                                              KEY `idx_session_id` (`session_id`), -- 索引：大幅提升查询某会话所有记录的速度
                                              KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天记录表';