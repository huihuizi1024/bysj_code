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
@TableName("crisis_alert")
public class CrisisAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sessionId;
    private Long messageId;

    //预警等级：high/medium/low
    private String alertLevel;
    //预警类型：自杀/自残/暴力倾向/绝望
    private String alertType;
    //匹配的关键词
    private String keywords;
    //处理状态：pending（待处理）/handled（已处理）/resolved（已解决）
    private String status;
    //创建时间
    private LocalDateTime createdAt;
    //处理时间（处理后才会有值）
    private LocalDateTime handledAt;
    //处理备注
    private String handlerNotes;
}