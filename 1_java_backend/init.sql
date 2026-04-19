-- ==========================================================
-- 心理支持对话系统 (Mental Health AI App) 数据库初始化脚本
-- 字符集: utf8mb4 (支持各种复杂的表情符号)
-- ==========================================================

-- 1. 如果不存在则创建数据库
CREATE DATABASE IF NOT EXISTS `mental_health_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `mental_health_db`;

-- ==========================================================
-- ⚠️ 警告：以下几行会在重新运行脚本时清空旧表，确保结构为最新。
-- 如果将来部署到生产环境或已有真实数据，请务必注释掉这三行！
-- ==========================================================
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `chat_session`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `emotion_record`;
DROP TABLE IF EXISTS `crisis_alert`;
DROP TABLE IF EXISTS `user_profile`;
DROP TABLE IF EXISTS `supportive_resource`;
DROP TABLE IF EXISTS `resource_recommendation`;
DROP TABLE IF EXISTS `ai_model_config`;
DROP TABLE IF EXISTS `user_model_preference`;


-- ----------------------------------------------------------
-- 2. 匿名用户表 (存储账号密码及加密信息)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
                                      `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `username`    varchar(50)  NOT NULL COMMENT '匿名用户名/账号',
                                      `password`    varchar(100) NOT NULL COMMENT 'BCrypt加密后的密码密文',
                                      `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                                      `role`        varchar(20)  NOT NULL COMMENT '角色: USER普通用户/ADMIN管理员',
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


-- ----------------------------------------------------------
-- 5. 情绪分析记录表 (存储每条用户消息的情绪分析结果)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `emotion_record` (
                                      `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `user_id`        bigint       NOT NULL COMMENT '用户ID',
                                      `session_id`     bigint       NOT NULL COMMENT '会话ID',
                                      `message_id`     bigint       NOT NULL COMMENT '消息ID',
                                      `emotion_type`   varchar(20)           COMMENT '情绪类型: positive/negative/neutral/anxiety/depression/anger',
                                      `emotion_score`  double                 COMMENT '情绪得分 0-1',
                                      `keywords`       text                   COMMENT '情绪关键词',
                                      `analysis_time`  datetime              COMMENT '分析时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_user_id` (`user_id`),
                                      KEY `idx_session_id` (`session_id`),
                                      KEY `idx_analysis_time` (`analysis_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情绪分析记录表';


-- ----------------------------------------------------------
-- 6. 危机预警记录表 (存储检测到的危机事件)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `crisis_alert` (
                                      `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `user_id`        bigint       NOT NULL COMMENT '用户ID',
                                      `session_id`     bigint       NOT NULL COMMENT '会话ID',
                                      `message_id`     bigint                COMMENT '消息ID',
                                      `alert_level`    varchar(10)           COMMENT '预警等级: high/medium/low',
                                      `alert_type`      varchar(50)           COMMENT '预警类型: 自杀/自残/暴力倾向/绝望',
                                      `keywords`        text                  COMMENT '匹配的关键词',
                                      `status`          varchar(20)           COMMENT '状态: pending/handled/resolved',
                                      `created_at`      datetime              COMMENT '创建时间',
                                      `handled_at`      datetime              COMMENT '处理时间',
                                      `handler_notes`   text                  COMMENT '处理备注',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_user_id` (`user_id`),
                                      KEY `idx_status` (`status`),
                                      KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='危机预警记录表';


-- ----------------------------------------------------------
-- 7. 用户画像表 (存储用户的心理状态画像)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_profile` (
                                      `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `user_id`            bigint       NOT NULL COMMENT '用户ID(唯一)',
                                      `personality_type`   varchar(20)           COMMENT '人格类型',
                                      `main_concern`       varchar(100)          COMMENT '主要困扰',
                                      `stress_level`       varchar(10)           COMMENT '压力等级: high/medium/low',
                                      `emotional_trend`    varchar(10)           COMMENT '情绪趋势: rising/falling/stable',
                                      `conversation_count` int        DEFAULT 0 COMMENT '对话次数',
                                      `total_messages`     int        DEFAULT 0 COMMENT '总消息数',
                                      `last_active_time`   datetime              COMMENT '最后活跃时间',
                                      `updated_at`         datetime              COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';
-- ----------------------------------------------------------
-- 8. 支持性资源表（存储心理支持资源内容）
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `supportive_resource` (
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category`           varchar(30)           COMMENT '资源大类: crisis/counseling/selfhelp/mindfulness/tips',
    `resource_type`      varchar(20)           COMMENT '资源小类: hotline/center/exercise/tips/article',
    `title`              varchar(100)  NOT NULL COMMENT '资源名称',
    `content`            text                  COMMENT '资源详情/内容',
    `trigger_emotion`    varchar(50)           COMMENT '推荐触发的情绪类型: depression/anxiety/anger/all',
    `trigger_score_min`  double                COMMENT '触发情绪得分下限(0~1)',
    `trigger_score_max`  double                COMMENT '触发情绪得分上限(0~1)',
    `applicable_scene`  varchar(200)          COMMENT '适用场景描述',
    `priority`           int         DEFAULT 99 COMMENT '推荐优先级(越小越高)',
    `enabled`            int         DEFAULT 1  COMMENT '是否启用: 1启用 0禁用',
    `create_time`        datetime              COMMENT '创建时间',
    `update_time`        datetime              COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支持性资源表';

-- ----------------------------------------------------------
-- 9. 资源推荐记录表（统计资源使用情况）
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `resource_recommendation` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         bigint       NOT NULL COMMENT '用户ID',
    `session_id`      bigint                COMMENT '会话ID',
    `resource_id`     bigint       NOT NULL COMMENT '被推荐的资源ID',
    `emotion_type`    varchar(20)           COMMENT '推荐时的情绪类型',
    `emotion_score`   double                COMMENT '推荐时的情绪得分',
    `recommended_at`  datetime              COMMENT '推荐时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_recommended_at` (`recommended_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源推荐记录表';

-- ----------------------------------------------------------
-- 10. 初始化支持性资源数据
-- ----------------------------------------------------------
INSERT INTO `supportive_resource` (`category`, `resource_type`, `title`, `content`, `trigger_emotion`, `trigger_score_min`, `trigger_score_max`, `applicable_scene`, `priority`, `enabled`, `create_time`) VALUES
-- 危机热线（所有低分情绪都可能被触发）
('crisis', 'hotline', '全国希望24小时心理危机干预热线', '📞 全国统一心理援助热线：400-161-9995\n⏰ 服务时间：24小时全年无休\n👤 接线人员：专业心理咨询师\n📍 适用情况：当你感到绝望、无助，或有自我伤害的念头时，请第一时间拨打，我们会一直陪着你。', 'all', 0.0, 0.3, '当用户表达绝望、无助、有自我伤害念头时', 1, 1, NOW()),
('crisis', 'hotline', '北京心理危机研究与干预中心', '📞 热线电话：010-82951332\n⏰ 服务时间：24小时\n📍 机构：北京市心理危机研究与干预中心', 'all', 0.0, 0.3, '当用户情绪极度低落或有危机风险时', 2, 1, NOW()),
('crisis', 'hotline', '生命热线', '📞 热线电话：400-821-1215\n⏰ 服务时间：每天 8:00-22:00', 'all', 0.0, 0.3, '当用户感到极度绝望时', 3, 1, NOW()),

-- 正念冥想（焦虑情绪适用）
('mindfulness', 'exercise', '3分钟呼吸空间（经典正念练习）', '找一个舒适的姿势坐着，背部挺直但不要僵硬。\n\n🌿 第1分钟：闭上眼睛或轻轻垂下眼帘，将注意力轻轻放在呼吸上。感受空气从鼻孔进入，感受胸腔的起伏。不需要改变呼吸，只是单纯地觉察它。\n\n🌿 第2分钟：继续专注呼吸。如果注意力飘走了，这是正常的，只需要温柔地把注意力带回来，就像轻轻把迷路的小鸟放回枝头。\n\n🌿 第3分钟：缓缓睁开眼，给自己一个微笑。你刚刚给自己3分钟的安宁，这就是在照顾自己。\n\n💡 小提示：每天固定时间练习（比如起床后或睡前），坚持21天会看到明显效果。', 'anxiety', 0.0, 0.5, '当用户感到紧张、焦虑、心慌、不安时', 10, 1, NOW()),
('mindfulness', 'exercise', '478呼吸法（快速缓解焦虑）', '这是一种经过科学验证的呼吸技巧，可以在几分钟内显著降低焦虑水平。\n\n📋 步骤：\n1. 用鼻子慢慢吸气，数 4 秒（1...2...3...4）\n2. 屏住呼吸，数 4 秒（1...2...3...4）\n3. 用嘴巴慢慢呼气，数 8 秒（1...2...3...4...5...6...7...8）\n4. 重复上述步骤 3-4 次\n\n💡 小提示：呼气时间比吸气长一倍是关键，这能激活副交感神经，帮助身体从"紧张模式"切换到"放松模式"。', 'anxiety', 0.1, 0.5, '当用户感到紧张、心跳加速、焦虑发作时', 11, 1, NOW()),
('mindfulness', 'exercise', '身体扫描放松练习（睡前版）', '身体扫描能帮助我们把注意力从"头脑中的担忧"转移到"身体的感受"上，特别适合睡前使用。\n\n🧘 练习方式：\n找一个舒服的姿势躺好，闭上眼睛。\n\n从脚趾开始，慢慢把注意力移动到脚底→脚踝→小腿→膝盖→大腿→臀部。感受每个部位的感觉，不需要刻意放松，只是觉察。\n\n继续向上：腹部→胸口→背部→双手→手臂→肩膀→脖子→脸部。\n\n如果哪里感到紧绷，可以试着在那里停留一下，温柔地对它说"我知道你很紧张，你可以放松下来了"。\n\n⏰ 时长：约10-15分钟\n💡 建议：可以配合轻音乐使用。', 'anxiety', 0.0, 0.4, '当用户表达睡眠问题、失眠、头脑停不下来时', 12, 1, NOW()),

-- CBT自助技巧（抑郁情绪适用）
('selfhelp', 'tips', '情绪记录表（三栏式CBT练习）', '这是一个帮助你觉察"触发事件→自动思维→情绪反应"之间关系的工具。\n\n📋 三栏记录法：\n┌──────────────┬──────────────────────┬────────────────┐\n│  触发事件     │  自动思维（脑海中闪过） │  情绪和强度(0-100) │\n├──────────────┼──────────────────────┼────────────────┤\n│ 例：期末考试成绩 │ "我真没用，什么都做不好" │  难过(80)、沮丧(70) │\n│              │ 也许这次题目真的很难？  │                 │\n│              │ 我平时努力了哪些？       │                 │\n└──────────────┴──────────────────────┴────────────────┘\n\n💡 练习要点：\n1. 先只记录，不要急着评判自己的思维\n2. 问自己：这个想法有证据支持吗？有没有反例？\n3. 换一个更平衡的想法，写在下方\n\n📝 推荐每天记录1-2件让你情绪波动的事，坚持一周会有新的发现。', 'depression', 0.0, 0.5, '当用户持续情绪低落、自我否定、找不到意义感时', 20, 1, NOW()),
('selfhelp', 'tips', '安全计划模板（危机时刻的行动清单）', '当你感到非常痛苦、快要撑不住的时候，这个安全计划可以帮你度过最难熬的时刻。\n\n📋 请认真填写以下内容，并保存好：\n\n【我的预警信号】\n当我有自我伤害的念头时，通常会有这些信号：\n1. _______________\n2. _______________\n\n【我可以使用的内部策略】（分散注意力/自我安抚）\n1. 听音乐：播放我的歌单「______________」\n2. 出门走走：去 ______________\n3. 给 ______________ 打电话\n4. 写日记/画画\n5. 做一件小事：_______________\n\n【我可以联系的人】（可以倾诉的人）\n1. _____________ 电话：_____________\n2. _____________ 电话：_____________\n\n【专业帮助】（危机时刻一定要打）\n📞 全国心理援助热线：400-161-9995（24小时）\n📞 北京心理危机干预中心：010-82951332\n\n【让环境更安全】\n我承诺在这个时候会：\n□ 把锐利物品交给 _____________ 保管\n□ 不独自待在 _____________\n\n💡 请把这个计划保存好，在需要时使用它。你不是一个人。', 'depression', 0.0, 0.35, '当用户表达绝望、觉得自己是负担、暗示自我伤害时', 5, 1, NOW()),
('selfhelp', 'tips', '积极自我对话练习表', '很多时候，让我们痛苦的其实不是事件本身，而是我们对事件的自责性解读。下面是一个转换练习：\n\n📋 负面自动思维 → 平衡性思维的转换练习：\n\n❌ "我真失败，什么都做不好"  \n✅ → "这件事没做好，但不代表我整个人是失败的。我有做得好的事情，比如______。"\n\n❌ "没人关心我"  \n✅ → "我现在感到孤独，但_________其实一直关心我，只是我可能忽略了。"\n\n❌ "我必须表现完美才能被认可"  \n✅ → "真实的我已经值得被尊重，我可以不完美。"\n\n❌ "事情永远不会变好了"  \n✅ → "现在很痛苦，但情绪是会流动的。________的时候我也曾感觉好过。"\n\n❌ "我是一个负担"  \n✅ → "我在经历困难，这不代表我是负担。_________需要我，我也有自己的价值。"\n\n💡 练习方法：\n当发现自己在用❌的思维方式时，停下来，写下来，然后试着用✅的方式重新说一句。刚开始可能觉得"不真实"，没关系，慢慢练习，神经通路会逐渐改变。', 'depression', 0.1, 0.5, '当用户自我否定、觉得不被爱、是负担时', 21, 1, NOW()),

-- 日常调节（通用）
('tips', 'article', '改善睡眠的十条建议', '睡眠对情绪的调节至关重要，以下是经过心理学研究验证的睡眠建议：\n\n🌙 1. 固定作息时间：每天同一时间睡觉、同一时间起床（包括周末）\n🌙 2. 睡前1小时关闭电子设备：手机蓝光会抑制褪黑素分泌\n🌙 3. 下午2点后不喝咖啡/浓茶\n🌙 4. 睡前避免大量进食或空腹\n🌙 5. 营造舒适的睡眠环境：黑暗、安静、适宜温度（18-22℃）\n🌙 6. 床只用来睡觉，不在床上刷手机、看剧\n🌙 7. 睡不着时不要硬躺，离开床做点放松的事，有困意再回来\n🌙 8. 白天适量运动，但避免睡前剧烈运动\n🌙 9. 尝试478呼吸法或身体扫描帮助入睡\n🌙 10. 如果持续失眠超过2周，请寻求专业帮助\n\n💡 小提醒：如果躺在床上超过20分钟还睡不着，不要强迫自己，起来坐一会儿，等困了再躺回去。', 'all', 0.0, 0.5, '当用户提到睡眠问题、失眠、早醒、疲惫时', 30, 1, NOW()),
('tips', 'article', '压力管理：把大压力拆成小任务', '当压力让你觉得喘不过气时，试着这样做：\n\n📋 第一步：把压力源写下来\n不要只放在脑子里想，写出来会让它变得"具体"，而不是一团模糊的焦虑。\n\n📋 第二步：区分"能改变"和"不能改变"\n- 能改变的：制定具体的下一步行动\n- 不能改变的：接受它，把精力放在能控制的部分\n\n📋 第三步：大任务拆成小步骤\n比如"准备期末考试"可以拆成：\n① 今天整理第一章笔记（30分钟）\n② 明天做第一章练习题\n③ 后天复习错题\n\n💡 关键心态：专注于"今天能做的第一步"，而不是"整个大山"。完成小任务带来的成就感，会帮你积累信心。', 'all', 0.1, 0.5, '当用户感到压力山大、无从下手、事情太多时', 31, 1, NOW());

INSERT INTO `supportive_resource` (`category`, `resource_type`, `title`, `content`, `trigger_emotion`, `trigger_score_min`, `trigger_score_max`, `applicable_scene`, `priority`, `enabled`, `create_time`) VALUES
-- 专业咨询资源（持续低落推荐）
('counseling', 'center', '校内心理咨询中心预约方式', '📍 校内心理咨询中心\n📞 预约电话：__________（请填写你的学校心理中心电话）\n🕐 开放时间：周一至周五 8:00-12:00，14:00-17:00\n📍 咨询地点：__________（请填写咨询室位置）\n\n💡 小提示：\n· 首次预约可能需要排队，请尽早预约\n· 咨询是完全保密的，你的隐私会被保护\n· 如果不知道是否需要咨询，可以先打电话说明情况，老师会给你建议\n· 大学心理咨询中心通常提供免费或低价的咨询服务\n\n✨ 你愿意迈出这一步去预约咨询，这本身就是一个很有力量的行动。', 'all', 0.0, 0.45, '当用户表达持续情绪低落、长期困扰、反复出现类似问题时', 40, 1, NOW()),
('counseling', 'center', '什么时候需要寻求专业心理咨询？', '心理咨询不是"有病"才去的。以下情况，你都可以考虑寻求专业帮助：\n\n✅ 情绪问题持续超过2周没有明显改善\n✅ 情绪问题开始影响日常生活（学习/吃饭/睡眠/社交）\n✅ 遇到重大创伤事件（失恋、亲人离世、霸凌等）难以走出来\n✅ 反复出现相同的"负面思维模式"\n✅ 感到迷茫，不知道自己怎么了，但就是不舒服\n\n📋 心理咨询和心理治疗的区别：\n· 心理咨询：处理一般心理困扰，如压力、人际关系、自我成长\n· 心理治疗：处理较严重的心理障碍，需要更系统的干预\n\n💡 勇敢求助是心理健康的表现，不是软弱。', 'all', 0.0, 0.5, '当用户不确定自己是否需要专业帮助时', 41, 1, NOW());
-- ----------------------------------------------------------
-- 11. AI模型配置表（存储所有可选的AI模型）
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ai_model_config` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code`            varchar(30)  NOT NULL COMMENT '模型代码标识：DEEPSEEK/OPENAI/KIMI/LOCAL',
    `name`            varchar(50)  NOT NULL COMMENT '前端显示名称',
    `description`     varchar(200)          COMMENT '模型描述',
    `api_url`         varchar(255)          COMMENT 'API请求地址',
    `api_key_alias`   varchar(50)           COMMENT '对应secrets.properties中的key名',
    `model_name`      varchar(100)          COMMENT '具体模型名',
    `temperature`     double       DEFAULT 0.7 COMMENT '温度参数',
    `max_tokens`      int          DEFAULT 2000 COMMENT '最大token数',
    `enabled`         int          DEFAULT 1  COMMENT '1启用 0禁用',
    `is_default`     int          DEFAULT 0  COMMENT '1默认选中',
    `sort_order`      int          DEFAULT 99 COMMENT '排序顺序(越小越靠前)',
    `create_time`     datetime              COMMENT '创建时间',
    `update_time`     datetime              COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';

-- ----------------------------------------------------------
-- 12. 用户模型偏好表（记录每个用户当前选用的AI模型）
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_model_preference` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint       NOT NULL COMMENT '用户ID',
    `model_code`  varchar(30)  NOT NULL COMMENT '关联ai_model_config.code',
    `updated_at`  datetime              COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户模型偏好表';

-- ----------------------------------------------------------
-- 13. 初始化AI模型配置数据
-- ----------------------------------------------------------
INSERT INTO `ai_model_config` (`code`, `name`, `description`, `api_url`, `api_key_alias`, `model_name`, `temperature`, `max_tokens`, `enabled`, `is_default`, `sort_order`, `create_time`) VALUES
('DEEPSEEK', 'DeepSeek', '深度求索大模型，性价比高，适合日常心理陪伴', 'https://api.deepseek.com/v1/chat/completions', 'ai.deepseek.key', 'deepseek-chat', 0.7, 2000, 1, 1, 1, NOW()),
('OPENAI', 'GPT-4o Mini', 'OpenAI最新小模型，响应快，适合复杂对话', 'https://api.openai.com/v1/chat/completions', 'ai.openai.key', 'gpt-4o-mini', 0.7, 2000, 1, 0, 2, NOW()),
('KIMI', 'Kimi', '月之暗面Kimi，长上下文支持，适合长文分析', 'https://api.moonshot.cn/v1/chat/completions', 'ai.kimi.key', 'moonshot-v1-8k', 0.7, 2000, 1, 0, 3, NOW()),
('LOCAL', '本地模型', '本地部署的大模型，数据不出本地', 'http://localhost:8000/v1/chat/completions', 'ai.local.key', 'your-local-model', 0.7, 2000, 1, 0, 4, NOW());

-- ----------------------------------------------------------
-- 14. 初始化管理员账号（密码：123456）
-- ----------------------------------------------------------
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', '$2a$10$jPydTMKSPXSt1LhWVX9xZuiglPWsHNEXd14Yol7owFwqEsUjOl04.', 'ADMIN');