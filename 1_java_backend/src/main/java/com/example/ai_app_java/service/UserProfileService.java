package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.UserProfile;
import java.util.List;

//用户画像服务接口

public interface UserProfileService {
    /**
     *  获取用户画像（如果没有则创建）
     */
    UserProfile getOrCreateProfile(Long userId);

    /**
     * 更新用户画像
     * @param userProfile 用户画像对象
     */
    void updateUserProfile(UserProfile userProfile);

    /**
     * 根据历史对话分析并更新用户画像
     * @param userId 用户ID
     */
    void analyzeAndUpdateProfile(Long userId);

}