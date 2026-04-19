-- 添加管理员账号脚本
-- 用户名: admin
-- 密码: 123456

USE `mental_health_db`;

-- 如果已存在则更新，否则插入
INSERT INTO `user` (`username`, `password`, `role`) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN')
ON DUPLICATE KEY UPDATE `password` = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', `role` = 'ADMIN';

-- 验证插入结果
SELECT id, username, role, create_time FROM `user` WHERE username = 'admin';
