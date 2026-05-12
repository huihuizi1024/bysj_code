package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.PsychologicalReadinessScore;
import com.example.ai_app_java.entity.EmotionRecord;
import java.util.List;

/**
 * 心理准备度得分（Psychological Readiness Score, PRS）服务接口
 *
 * PRS = w1 * engagement + w2 * valence_normalized + w3 * arousal
 * 权重：w1=0.4（参与度）, w2=0.35（情感价态）, w3=0.25（唤醒度）
 *
 * 干预深度映射：
 *   PRS < 0.35  → scaffolding（强支架）
 *   PRS 0.35-0.65 → supportive（中度支持）
 *   PRS > 0.65  → reflective（反思性对话）
 */
public interface PsychologicalReadinessService {

    /**
     * 根据情绪记录计算PRS得分
     * @param emotionRecord 当前情绪分析结果
     * @param userId 用户ID（用于查询历史参与度）
     * @param sessionId 会话ID
     * @param messageId 消息ID
     * @return PRS记录
     */
    PsychologicalReadinessScore calculatePRS(EmotionRecord emotionRecord, Long userId,
                                              Long sessionId, Long messageId);

    /**
     * 获取当前用户的干预深度
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return scaffolding / supportive / reflective
     */
    String getInterventionDepth(Long userId, Long sessionId);

    /**
     * 获取最近N条PRS记录
     * @param userId 用户ID
     * @param limit 数量限制
     * @return PRS记录列表
     */
    List<PsychologicalReadinessScore> getRecentPRS(Long userId, int limit);

    /**
     * 构建PRS上下文文本，注入到AI System Prompt
     * @param prsScore PRS总分
     * @param interventionDepth 干预深度
     * @return 格式化的上下文字符串
     */
    String buildPrsContext(Double prsScore, String interventionDepth);

    /**
     * 获取当前会话的最新PRS得分
     * @param sessionId 会话ID
     * @return PRS得分（无记录返回null）
     */
    Double getLatestPRSScore(Long sessionId);

    /**
     * 获取当前会话的最新干预深度
     * @param sessionId 会话ID
     * @return scaffolding / supportive / reflective（无记录返回 supportive）
     */
    String getLatestInterventionDepth(Long sessionId);
}
