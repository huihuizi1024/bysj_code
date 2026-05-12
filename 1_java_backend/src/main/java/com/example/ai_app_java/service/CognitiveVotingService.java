package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.CognitiveVoting;
import java.util.List;

/**
 * 认知投票服务接口
 *
 * 基于 CBT 认知扭曲理论，在对话中适时插入认知投票：
 * - thought_distortion：识别认知扭曲类型
 * - self_efficacy：自我效能评估
 * - coping_strategy：应对策略选择
 */
public interface CognitiveVotingService {

    /**
     * 提交认知投票
     * @param userId 用户ID
     * @param votingType 投票类型
     * @param question 投票问题
     * @param selectedOption 用户选择的选项
     * @param sessionId 会话ID（可选）
     * @return 投票记录
     */
    CognitiveVoting submitVote(Long userId, String votingType, String question,
                              String selectedOption, Long sessionId);

    /**
     * 获取用户最近的投票记录
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 投票记录列表
     */
    List<CognitiveVoting> getRecentVotings(Long userId, int limit);

    /**
     * 根据会话获取投票记录
     * @param sessionId 会话ID
     * @return 投票记录列表
     */
    List<CognitiveVoting> getVotingsBySession(Long sessionId);

    /**
     * 计算认知投票得分（0~1）
     * 基于投票参与度和选择质量
     * @param userId 用户ID
     * @param limit 最近N条
     * @return 投票得分
     */
    double calculateVotingScore(Long userId, int limit);

    /**
     * 根据情绪状态，判断是否应触发认知投票
     * 当 depression 类型 + score < 0.4 时触发
     * @param emotionType 情绪类型
     * @param emotionScore 情绪得分
     * @return 是否应触发
     */
    boolean shouldTriggerVoting(String emotionType, double emotionScore);

    /**
     * 获取当前应展示的投票问题
     * @param emotionType 情绪类型
     * @param recentVotingType 最近投票类型（避免重复）
     * @return 投票问题配置
     */
    VotingQuestion getNextQuestion(String emotionType, String recentVotingType);

    /** 投票问题配置 */
    record VotingQuestion(String type, String question, String[] options) {}
}
