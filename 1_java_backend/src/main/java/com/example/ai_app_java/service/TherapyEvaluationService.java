package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.TherapyEvaluation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI疗效评估服务接口
 *
 * 基于 MentalAlign 框架，提供对 AI 回复的认知支持和情感共鸣评估
 *
 * MentalAlign 核心指标：
 * - CSS (Cognitive Support Score): 认知支持得分，衡量 AI 在引导性、信息量、专业性、结构化方面的表现
 * - ARS (Affective Resonance Score): 情感共鸣得分，衡量 AI 在共情表达、情感验证、温暖感、安全感方面的表现
 *
 * @author MentalAlign Framework Integration
 */
public interface TherapyEvaluationService {

    /**
     * 评估单条 AI 回复的疗效
     *
     * 调用 AI 模型对指定的对话进行 CSS/ARS 评分
     *
     * @param userMessage 用户消息内容
     * @param aiMessage AI 回复内容
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageId AI 消息ID
     * @param modelCode 模型代码
     * @param clinicalIntent 临床意图
     * @param therapyModule 疗法模块
     * @param interventionDepth 干预深度
     * @param aiRole AI 角色
     * @return 评估记录
     */
    TherapyEvaluation evaluateResponse(
            String userMessage,
            String aiMessage,
            Long userId,
            Long sessionId,
            Long messageId,
            String modelCode,
            String clinicalIntent,
            String therapyModule,
            String interventionDepth,
            String aiRole);

    /**
     * 获取指定模型的评估统计数据
     *
     * @param modelCode 模型代码
     * @param days 时间范围（天数）
     * @return 包含平均 CSS/ARS/用户评分的统计数据
     */
    Map<String, Object> getModelStats(String modelCode, int days);

    /**
     * 获取所有模型的最新评估对比
     *
     * @param days 时间范围（天数）
     * @return 各模型的统计数据列表
     */
    List<Map<String, Object>> getAllModelStats(int days);

    /**
     * 获取指定用户的评估历史
     *
     * @param userId 用户ID
     * @param limit 返回记录数限制
     * @return 评估记录列表
     */
    List<TherapyEvaluation> getUserHistory(Long userId, int limit);

    /**
     * 获取指定模型的评估趋势数据
     *
     * @param modelCode 模型代码
     * @param days 时间范围（天数）
     * @return 每日的 CSS/ARS 平均值
     */
    List<Map<String, Object>> getModelTrend(String modelCode, int days);

    /**
     * 提交用户对 AI 回复的主观评分
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageId AI 消息ID
     * @param rating 用户评分（0-5）
     * @param userCss 用户认知支持评分（0-5）
     * @param userArs 用户情感共鸣评分（0-5）
     * @return 是否成功
     */
    boolean submitUserRating(Long userId, Long sessionId, Long messageId, Double rating, Double userCss, Double userArs);

    /**
     * 记录评估数据（内部使用，由 AI 服务在对话结束后调用）
     *
     * @param evaluation 评估记录
     */
    void saveEvaluation(TherapyEvaluation evaluation);

    /**
     * 获取指定时间范围内的评估记录
     *
     * @param modelCode 模型代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 评估记录列表
     */
    List<TherapyEvaluation> getEvaluationsByTimeRange(
            String modelCode, LocalDateTime startTime, LocalDateTime endTime);
}
