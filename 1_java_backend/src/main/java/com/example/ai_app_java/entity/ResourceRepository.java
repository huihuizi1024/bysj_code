package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource_repository")
public class ResourceRepository {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;             // 资源库代码: crisis / mindfulness / cbt / safety_plan / sleep / stress_mgmt / counseling
    private String name;             // 资源库名称，如 "危机干预资源库"
    private String category;         // 资源大类: crisis / self_help / meditation / exercise / reading
    private String description;      // 资源库描述
    private String strategy;         // AI 策略提示词片段
    @TableField("trigger_emotion")
    private String triggerEmotion;   // 触发情绪类型: anxiety / depression / anger / all
    @TableField("trigger_score_min")
    private Double triggerScoreMin;   // 触发情绪得分下限 (0~1)
    @TableField("trigger_score_max")
    private Double triggerScoreMax;   // 触发情绪得分上限 (0~1)
    private Integer priority;        // 优先级（越小越高）
    private Integer enabled;          // 1 启用 / 0 禁用
    @TableField("created_time")
    private LocalDateTime createdTime;
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
