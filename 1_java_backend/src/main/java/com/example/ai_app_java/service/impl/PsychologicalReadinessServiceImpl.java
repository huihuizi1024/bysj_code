package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.PsychologicalReadinessScore;
import com.example.ai_app_java.entity.EmotionRecord;
import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.mapper.PsychologicalReadinessScoreMapper;
import com.example.ai_app_java.service.PsychologicalReadinessService;
import com.example.ai_app_java.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PsychologicalReadinessServiceImpl implements PsychologicalReadinessService {

    // 权重配置（论文消融实验对象）
    private static final double W1_ENGAGEMENT = 0.40;
    private static final double W2_VALENCE = 0.35;
    private static final double W3_AROUSAL = 0.25;

    @Autowired
    private PsychologicalReadinessScoreMapper prsMapper;

    @Autowired
    private ChatMessageService chatMessageService;

    @Override
    public PsychologicalReadinessScore calculatePRS(EmotionRecord emotionRecord, Long userId,
                                                   Long sessionId, Long messageId) {
        PsychologicalReadinessScore prs = new PsychologicalReadinessScore();
        prs.setUserId(userId);
        prs.setSessionId(sessionId);
        prs.setMessageId(messageId);
        prs.setCalculatedAt(LocalDateTime.now());

        // 1. 参与度分量表：基于消息长度、会话历史活跃度
        double engagementScore = calculateEngagementScore(userId, sessionId);
        prs.setEngagementScore(engagementScore);

        // 2. 情感价态分量表：从情绪分析中获取（已是 -1~1 范围）
        double valenceScore = emotionRecord.getValence() != null ? emotionRecord.getValence() : 0.0;
        // 将 -1~1 归一化到 0~1，与其他分量表范围一致
        double valenceNormalized = (valenceScore + 1.0) / 2.0;
        prs.setValenceScore(valenceNormalized);

        // 3. 唤醒度分量表：直接从情绪分析中获取（0~1）
        double arousalScore = emotionRecord.getArousal() != null ? emotionRecord.getArousal() : 0.5;
        prs.setArousalScore(arousalScore);

        // 4. PRS 综合得分
        double totalScore = W1_ENGAGEMENT * engagementScore
                          + W2_VALENCE * valenceNormalized
                          + W3_AROUSAL * arousalScore;
        totalScore = Math.max(0.0, Math.min(1.0, totalScore));
        prs.setTotalScore(totalScore);

        // 5. 干预深度映射
        String depth = mapToInterventionDepth(totalScore);
        prs.setInterventionDepth(depth);

        prsMapper.insert(prs);
        return prs;
    }

    /**
     * 计算参与度分量表
     * 综合考量：
     *   - 当前消息长度（标准化到 0~0.4）
     *   - 会话历史活跃度（过去3轮是否有消息，0~0.3）
     *   - 是否为新会话（首次发言给 0.3 奖励分）
     */
    private double calculateEngagementScore(Long userId, Long sessionId) {
        double score = 0.0;

        // 查询最近3条用户消息
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
               .eq("role", "user")
               .orderByDesc("create_time")
               .last("LIMIT 3");
        List<ChatMessage> recentMsgs = chatMessageService.list(wrapper);

        // 历史活跃度
        if (!recentMsgs.isEmpty()) {
            score += 0.3; // 有历史消息
            if (recentMsgs.size() >= 2) {
                score += 0.2; // 持续参与
            }
        } else {
            score += 0.3; // 新会话首次发言奖励
        }

        // 消息长度（上限0.5）
        if (!recentMsgs.isEmpty()) {
            ChatMessage lastMsg = recentMsgs.get(0);
            int len = lastMsg.getContent() != null ? lastMsg.getContent().length() : 0;
            // 标准化：30字=0.1，300字=0.5
            double lenScore = Math.min(0.5, len / 600.0);
            score += lenScore;
        }

        return Math.min(1.0, score);
    }

    @Override
    public String getInterventionDepth(Long userId, Long sessionId) {
        // 查询当前会话最新PRS
        QueryWrapper<PsychologicalReadinessScore> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
               .orderByDesc("calculated_at")
               .last("LIMIT 1");
        PsychologicalReadinessScore prs = prsMapper.selectOne(wrapper);
        if (prs != null && prs.getInterventionDepth() != null) {
            return prs.getInterventionDepth();
        }
        return "supportive"; // 默认中度支持
    }

    @Override
    public List<PsychologicalReadinessScore> getRecentPRS(Long userId, int limit) {
        QueryWrapper<PsychologicalReadinessScore> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("calculated_at")
               .last("LIMIT " + limit);
        return prsMapper.selectList(wrapper);
    }

    @Override
    public String buildPrsContext(Double prsScore, String interventionDepth) {
        if (prsScore == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【本次对话策略配置】\n");
        sb.append(String.format("心理准备度(PRS)：%.2f\n", prsScore));

        String languageGuidance;
        String interventionStrategy;
        switch (interventionDepth) {
            case "scaffolding":
                languageGuidance = "使用简短、温暖、易于理解的语言，避免专业术语，每个回复控制在50字以内。";
                interventionStrategy = "提供强支架式支持：复述用户感受，给予充分同理心，提供具体可操作的步骤建议。";
                break;
            case "reflective":
                languageGuidance = "使用开放性、启发式的语言，提出有深度的反思性问题，鼓励用户自主探索内心。";
                interventionStrategy = "提供反思性对话：运用苏格拉底式提问，引导用户觉察负面自动思维，探索其他视角。";
                break;
            default: // supportive
                languageGuidance = "使用自然流畅的语言，平衡共情与引导，保持温暖而专业的对话风格。";
                interventionStrategy = "提供中度支持：在共情的基础上，适时引入CBT自助练习或正念资源。";
                break;
        }

        sb.append("语言风格：").append(languageGuidance).append("\n");
        sb.append("干预策略：").append(interventionStrategy).append("\n");
        return sb.toString();
    }

    @Override
    public Double getLatestPRSScore(Long sessionId) {
        QueryWrapper<PsychologicalReadinessScore> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
               .orderByDesc("calculated_at")
               .last("LIMIT 1");
        PsychologicalReadinessScore prs = prsMapper.selectOne(wrapper);
        return prs != null ? prs.getTotalScore() : null;
    }

    @Override
    public String getLatestInterventionDepth(Long sessionId) {
        QueryWrapper<PsychologicalReadinessScore> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
               .orderByDesc("calculated_at")
               .last("LIMIT 1");
        PsychologicalReadinessScore prs = prsMapper.selectOne(wrapper);
        return prs != null ? prs.getInterventionDepth() : "supportive";
    }

    private String mapToInterventionDepth(Double totalScore) {
        if (totalScore == null) return "supportive";
        if (totalScore < 0.35) {
            return "scaffolding";
        } else if (totalScore > 0.65) {
            return "reflective";
        } else {
            return "supportive";
        }
    }
}
