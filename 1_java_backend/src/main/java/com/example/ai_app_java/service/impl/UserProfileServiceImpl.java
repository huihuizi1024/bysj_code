package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.UserProfile;
import com.example.ai_app_java.entity.EmotionRecord;
import com.example.ai_app_java.mapper.ChatMessageMapper;
import com.example.ai_app_java.mapper.EmotionRecordMapper;
import com.example.ai_app_java.mapper.UserProfileMapper;
import com.example.ai_app_java.service.UserProfileService;
import com.example.ai_app_java.service.BehaviorCheckInService;
import com.example.ai_app_java.service.CognitiveVotingService;
import com.example.ai_app_java.service.EmotionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;

//=================================================
//          用户画像服务实现类（动态画像 + 评估-干预闭环）
//=================================================
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    //=================================================
    //          注入UserProfileMapper，用于操作用户画像
    //=================================================
    @Autowired
    private UserProfileMapper userProfileMapper;

    //=================================================
    //          注入ChatMessageMapper，用于获取用户历史对话
    //=================================================
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    //=================================================
    //          注入EmotionRecordMapper，用于获取用户情绪记录
    //=================================================
    @Autowired
    private EmotionRecordMapper emotionRecordMapper;

    // 行为打卡服务
    @Autowired(required = false)
    private BehaviorCheckInService checkInService;

    // 认知投票服务
    @Autowired(required = false)
    private CognitiveVotingService votingService;

    // 情绪分析服务
    @Autowired(required = false)
    private EmotionAnalysisService emotionAnalysisService;

    // 动态画像修正算法权重（可调）
    private static final double ALPHA = 0.7;  // 历史权重衰减
    private static final double BETA_EMOTION = 0.5;   // 情绪权重
    private static final double GAMMA_CHECKIN = 0.25; // 打卡权重
    private static final double DELTA_VOTING = 0.25;  // 投票权重

    //=================================================
    //          获取用户画像（如果没有则创建）
    //=================================================
    @Override
    public UserProfile getOrCreateProfile(Long userId){
        QueryWrapper<UserProfile> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);

        if (profile == null){
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setConversationCount(0);
            profile.setTotalMessages(0);
            profile.setStressLevel("medium");
            profile.setEmotionalTrend("stable");
            profile.setLastActiveTime(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            userProfileMapper.insert(profile);
        }
        return profile;
    }

    //=================================================
    //          更新用户画像
    //=================================================
    @Override
    public void updateUserProfile(UserProfile profile){
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.updateById(profile);
    }

    //=================================================
    //          分析并更新用户画像（基础版）
    //=================================================
    @Override
    public void analyzeAndUpdateProfile(Long userId){
        UserProfile profile = getOrCreateProfile(userId);

        QueryWrapper<ChatMessage> msgwrapper = new QueryWrapper<>();
        msgwrapper.eq("user_id", userId);
        List<ChatMessage> messages = chatMessageMapper.selectList(msgwrapper);

        Long sessionCount = messages.stream()
            .map(ChatMessage::getSessionId)
            .distinct()
            .count();

        profile.setConversationCount((int)(long)sessionCount);
        profile.setTotalMessages((int)messages.size());
        profile.setLastActiveTime(LocalDateTime.now());

        QueryWrapper<EmotionRecord> emotionwrapper = new QueryWrapper<>();
        emotionwrapper.eq("user_id", userId)
                    .orderByDesc("analysis_time")
                    .last("LIMIT 10");
        List<EmotionRecord> recentEmotions = emotionRecordMapper.selectList(emotionwrapper);

        if (!recentEmotions.isEmpty()){
            double avgScore = recentEmotions.stream()
                    .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                    .average()
                    .orElse(0.5);
            if (avgScore > 0.6){
                profile.setStressLevel("low");
            } else if (avgScore < 0.4){
                profile.setStressLevel("high");
            } else {
                profile.setStressLevel("medium");
            }
            if (recentEmotions.size() >= 4){
                double recent = recentEmotions.subList(0,3).stream()
                    .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                    .average().orElse(0.5);
                double older = recentEmotions.subList(3, recentEmotions.size()).stream()
                    .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                    .average().orElse(0.5);
                if (recent > older + 0.1){
                    profile.setEmotionalTrend("rising");
                } else if (recent < older - 0.1){
                    profile.setEmotionalTrend("falling");
                } else {
                    profile.setEmotionalTrend("stable");
                }
            }
        }
        updateUserProfile(profile);
    }

    //=================================================
    //          动态画像修正算法（评估-干预闭环）
    //
    //  PRS_t = ALPHA * PRS_{t-1} + (1-ALPHA) * (
    //              BETA_EMOTION * emotion_score
    //            + GAMMA_CHECKIN * checkin_score
    //            + DELTA_VOTING * voting_score)
    //
    //  其中 BETA_EMOTION + GAMMA_CHECKIN + DELTA_VOTING = 1.0
    //=================================================
    public void dynamicProfileCorrection(Long userId) {
        if (checkInService == null || votingService == null) {
            // 服务未初始化，降级到基础版
            analyzeAndUpdateProfile(userId);
            return;
        }

        UserProfile profile = getOrCreateProfile(userId);

        // 获取上次画像得分（若无则用 0.5）
        double lastScore = 0.5;
        if (profile.getStressLevel() != null) {
            lastScore = switch (profile.getStressLevel()) {
                case "low" -> 0.7;
                case "high" -> 0.3;
                default -> 0.5;
            };
        }

        // 1. 情绪得分（7天平均）
        double emotionScore = 0.5;
        if (emotionAnalysisService != null) {
            List<EmotionRecord> emotions = emotionAnalysisService.getUserEmotionTrend(userId, 7);
            if (!emotions.isEmpty()) {
                emotionScore = emotions.stream()
                    .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                    .average().orElse(0.5);
            }
        }

        // 2. 打卡活跃度得分（7天）
        double checkinScore = checkInService.calculateCheckinScore(userId, 7);

        // 3. 认知投票得分（最近10条）
        double votingScore = votingService.calculateVotingScore(userId, 10);

        // 4. 综合画像得分
        double combined = BETA_EMOTION * emotionScore
                        + GAMMA_CHECKIN * checkinScore
                        + DELTA_VOTING * votingScore;

        double currentScore = ALPHA * lastScore + (1 - ALPHA) * combined;
        currentScore = Math.max(0.0, Math.min(1.0, currentScore));

        // 5. 更新画像标签
        if (currentScore > 0.6) {
            profile.setStressLevel("low");
        } else if (currentScore < 0.4) {
            profile.setStressLevel("high");
        } else {
            profile.setStressLevel("medium");
        }

        // 连续打卡天数（额外记录到主要困扰字段）
        int streak = checkInService.getStreakDays(userId);
        if (streak > 0) {
            profile.setMainConcern("连续打卡" + streak + "天");
        }

        profile.setLastActiveTime(LocalDateTime.now());
        updateUserProfile(profile);
    }
}
