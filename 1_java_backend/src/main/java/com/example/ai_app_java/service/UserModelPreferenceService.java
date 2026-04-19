package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.UserModelPreference;

public interface UserModelPreferenceService {
    // 获取用户当前选中的模型code，没有则返回默认模型
    String getUserModelCode(Long userId);
    // 保存用户的模型选择
    void saveOrUpdate(Long userId, String modelCode);
}