package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * AI疗效评估记录实体
 *
 * 基于 MentalAlign 框架设计，用于评估不同 AI 模型在心理支持对话中的治疗质量
 * 核心指标：
 * - CSS (Cognitive Support Score): 认知支持得分，衡量 AI 回复在引导性、信息量、专业性、结构化方面的表现
 * - ARS (Affective Resonance Score): 情感共鸣得分，衡量 AI 回复在共情表达、情感验证、温暖感、安全感方面的表现
 *
 * @author MentalAlign Framework Integration
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("therapy_evaluation")
public class TherapyEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话ID */
    private Long sessionId;

    /** 被评估的AI消息ID */
    private Long messageId;

    /** 模型代码，如 DEEPSEEK/OPENAI/KIMI/LOCAL */
    private String modelCode;

    /**
     * 认知支持得分 (Cognitive Support Score)
     * 评估维度：
     * - 引导性：是否有效引导用户思考
     * - 信息量：是否提供有价值的心理知识
     * - 专业性：是否运用正确的心理疗法
     * - 结构化：是否清晰有层次
     * 范围：0.0 ~ 1.0
     */
    private Double cssScore;

    /**
     * 情感共鸣得分 (Affective Resonance Score)
     * 评估维度：
     * - 共情表达：是否准确表达理解和接纳
     * - 情感验证：是否验证用户的情绪体验
     * - 温暖感：回复是否温暖有爱心
     * - 安全感：是否让用户感到被保护
     * 范围：0.0 ~ 1.0
     */
    private Double arsScore;

    /** 临床意图代码，对应当前对话的意图分类 */
    private String clinicalIntent;

    /** 疗法模块，如 ACT_value_clarification, CBT_behavioral_activation */
    private String therapyModule;

    /**
     * 干预深度
     * - scaffolding: 强支架（PRS < 0.35）
     * - supportive: 中度支持（PRS 0.35-0.65）
     * - reflective: 反思性对话（PRS > 0.65）
     */
    private String interventionDepth;

    /** AI治疗角色：supportive/empathetic/socratic/guided/crisis_mode */
    private String aiRole;

    /**
     * 用户主观满意度评分
     * 由用户在对话后自愿提供
     * 范围：0.0 ~ 5.0
     */
    private Double userRating;

    /**
     * 用户主观认知支持评分（来自 TherapyRating 组件）
     * 范围：0.0 ~ 5.0
     */
    private Double userCss;

    /**
     * 用户主观情感共鸣评分（来自 TherapyRating 组件）
     * 范围：0.0 ~ 5.0
     */
    private Double userArs;

    /** 评估时间 */
    private LocalDateTime evaluatedAt;
}
