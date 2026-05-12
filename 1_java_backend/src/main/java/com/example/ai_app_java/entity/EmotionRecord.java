package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data              // Lombok 插件：自动生成 getter/setter/toString
@AllArgsConstructor // 全参构造器
@NoArgsConstructor // 无参构造器
@TableName("emotion_record")  // 对应数据库表名
public class EmotionRecord {
    
    @TableId(type = IdType.AUTO)  // 主键自增
    private Long id;
    
    private Long userId;           // 用户ID
    private Long sessionId;        // 会话ID
    private Long messageId;        // 消息ID
    
    // 情绪类型：positive/negative/neutral/anxiety/depression/anger
    private String emotionType;
    
    // 情绪得分：0.0（非常负面）~ 1.0（非常积极）
    private Double emotionScore;

    // 情感价态 -1.0（极度消极）~ 1.0（极度积极）
    private Double valence;

    // 唤醒度 0.0（极度平静）~ 1.0（极度激动）
    private Double arousal;

    // 心理准备度得分（PRS）0.0~1.0
    private Double prsScore;

    // 识别出的情绪关键词
    private String keywords;
    
    // 分析时间
    private LocalDateTime analysisTime;
}