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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;

//=================================================
//          用户画像服务实现类
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

    //=================================================
    //          获取用户画像（如果没有则创建）
    //=================================================
    @Override
    public UserProfile getOrCreateProfile(Long userId){
        //1、先查询是否存在用户画像
        QueryWrapper<UserProfile> wrapper = new QueryWrapper<> ();
        wrapper.eq("user_id",userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);

        if(profile == null){
            //2、如果不存在，则创建新的用户画像
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
        //3、返回用户画像
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
    //          分析并更新用户画像
    //=================================================
    @Override
    public void analyzeAndUpdateProfile(Long userId){
        //1、获取或创建画像
        UserProfile profile = getOrCreateProfile(userId);

        //2、统计消息数和会话数
        QueryWrapper<ChatMessage> msgwrapper = new QueryWrapper<>();
        msgwrapper.eq("user_id",userId);
        List<ChatMessage> messages = chatMessageMapper.selectList(msgwrapper);

        Long sessionCount = messages.stream()
        .map(ChatMessage::getSessionId)
        .distinct()
        .count();

        profile.setConversationCount((int)(long)sessionCount);
        profile.setTotalMessages((int)messages.size());
        profile.setLastActiveTime(LocalDateTime.now());

        //3、分析情绪趋势
        QueryWrapper<EmotionRecord> emotionwrapper = new QueryWrapper<>();
        emotionwrapper.eq("user_id",userId)
                    .orderByDesc("analysis_time")
                    .last("LIMIT 10");
        List<EmotionRecord> recentEmotions = emotionRecordMapper.selectList(emotionwrapper);

        if(!recentEmotions.isEmpty()){
            //计算平均情绪得分
            double avgScore = recentEmotions.stream()
                    .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                    .average()
                    .orElse(0.5);
            //根据平均情绪得分更新压力等级
            if(avgScore > 0.6){
                profile.setStressLevel("low");
            }else if(avgScore < 0.4){
                profile.setStressLevel("high");
            }else{
                profile.setStressLevel("medium");
            }
            //分析情绪趋势：至少需要4条记录（3条最新+至少1条较旧）
            if(recentEmotions.size() >= 4){
                double recent = recentEmotions.subList(0,3).stream()
                .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                .average()
                .orElse(0.5);
                double older = recentEmotions.subList(3,recentEmotions.size()).stream()
                .mapToDouble(e -> e.getEmotionScore() != null ? e.getEmotionScore() : 0.5)
                .average()
                .orElse(0.5);
                if(recent > older + 0.1){
                    profile.setEmotionalTrend("rising");    //情绪好转
                }else if(recent < older -0.1){
                    profile.setEmotionalTrend("falling");   //情绪变差
                }else{
                    profile.setEmotionalTrend("stable");    //情绪稳定
                }
            }
        }
        //4、保存更新
        updateUserProfile(profile);
    }
}
