package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户满意度记录实体
 *
 * 基于 Google HEART 框架设计，用于收集用户体验指标
 * HEART 框架包含五个核心指标：
 * - Happiness（满意度）：用户对产品的满意程度
 * - Engagement（参与度）：用户与产品的互动深度
 * - Adoption（接受度）：新用户开始使用产品
 * - Retention（留存率）：用户持续使用产品
 * - Task Success（任务成功）：用户完成任务的效果
 *
 * @author HEART Framework Integration
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_satisfaction")
public class UserSatisfaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话ID */
    private Long sessionId;

    /** 本次对话使用的模型代码 */
    private String modelCode;

    /**
     * 满意度 (Happiness)
     * 衡量用户对本次对话的满意程度
     * 范围：0.0 ~ 5.0（对应1-5星）
     */
    private Double happiness;

    /**
     * 参与度 (Engagement)
     * 衡量用户在对话中的投入程度
     * 基于消息数量、对话时长、互动深度等计算
     * 范围：0.0 ~ 1.0
     */
    private Double engagement;

    /**
     * 接受度 (Adoption)
     * 衡量用户是否愿意继续使用该模型/功能
     * 范围：0.0 ~ 1.0
     */
    private Double adoption;

    /**
     * 留存意愿 (Retention)
     * 衡量用户是否愿意向他人推荐
     * 范围：0.0 ~ 1.0
     */
    private Double retention;

    /**
     * 任务成功度 (Task Success)
     * 衡量用户的问题是否在对话中得到有效解决
     * 范围：0.0 ~ 1.0
     */
    private Double taskSuccess;

    /**
     * 综合评分
     * 由 HEART 五项指标加权计算得出
     * 范围：0.0 ~ 5.0
     */
    private Double overallScore;

    /** 用户文字反馈（可选） */
    private String comment;

    /** 用户改进建议（可选） */
    private String improvementSuggestion;

    /** 提交时间 */
    private LocalDateTime submittedAt;
}
