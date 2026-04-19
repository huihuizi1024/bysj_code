package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_model_preference")
public class UserModelPreference {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;           // 用户ID
    private String modelCode;      // 关联 ai_model_config.code
    private LocalDateTime updatedAt; // 更新时间
}