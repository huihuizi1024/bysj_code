package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.GuardianResult;

/**
 * Guardian 服务接口（输入安全层）
 *
 * 执行顺序：
 * 1. PHQ-9 第9项语义检测（自伤意念）
 * 2. 语义相似度检测（调用 VectorSimilarityService，与 crisis_sample 库比对）
 * 3. 关系脉络门控（人称代词 + 高危情境词组合检测）
 * 4. 关键词硬匹配（扩充规模）
 */
public interface GuardianService {

    /**
     * 对用户输入进行危机检测
     * @param userInput 用户输入文本
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageId 消息ID
     * @return Guardian 检测结果
     */
    GuardianResult check(String userInput, Long userId, Long sessionId, Long messageId);

    /**
     * 批量检测（用于历史消息回溯）
     * @param inputs 文本列表
     * @return 结果列表
     */
    java.util.List<GuardianResult> batchCheck(java.util.List<String> inputs);
}
