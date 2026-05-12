package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("intent_classification")
public class IntentClassification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 意图代码: existential_crisis / value_clarification 等 */
    private String code;

    /** 意图名称 */
    private String name;

    /** 意图描述 */
    private String description;

    /** 关联疗法维度: CBT,ACT,DBT */
    private String therapyDimensions;

    /** 对应角色: empathetic / supportive / socratic / guided / crisis_mode */
    private String aiRole;

    /** 优先级 */
    private Integer priority;
}
