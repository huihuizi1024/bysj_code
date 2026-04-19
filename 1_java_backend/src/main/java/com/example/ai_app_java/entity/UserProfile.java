package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_profile")
public class UserProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    //人格类型：introvert（内向）/extrovert（外向）/ambivert（双重性格）
    private String personalityType;
    //主要困扰：学业压力/人际关系/情感问题/职业规划/其他
    private String mainConcern;
    //压力等级：high（高）/medium（中）/low（低）
    private String stressLevel;
    //情绪趋势：rising（上升）/falling（下降）/stable（稳定）
    private String emotionalTrend;
    //对话次数
    private Integer conversationCount;
    //总消息数
    private Integer totalMessages;
    //最后活跃时间
    private LocalDateTime lastActiveTime;
    //更新时间
    private LocalDateTime updatedAt;
}