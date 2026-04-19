package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.UserModelPreference;
import com.example.ai_app_java.mapper.UserModelPreferenceMapper;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.UserModelPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserModelPreferenceServiceImpl implements UserModelPreferenceService {

    @Autowired
    private UserModelPreferenceMapper userModelPreferenceMapper;

    @Autowired
    @Lazy
    private AiModelConfigService aiModelConfigService;

    @Override
    public String getUserModelCode(Long userId) {
        LambdaQueryWrapper<UserModelPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserModelPreference::getUserId, userId);
        UserModelPreference pref = userModelPreferenceMapper.selectOne(wrapper);

        if (pref != null) {
            return pref.getModelCode();
        }

        // 没有偏好，返回默认模型
        AiModelConfig defaultModel = aiModelConfigService.listEnabled().stream()
                .filter(m -> m.getIsDefault() != null && m.getIsDefault() == 1)
                .findFirst()
                .orElse(null);

        return defaultModel != null ? defaultModel.getCode() : "DEEPSEEK";
    }

    @Override
    public void saveOrUpdate(Long userId, String modelCode) {
        LambdaQueryWrapper<UserModelPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserModelPreference::getUserId, userId);
        UserModelPreference existing = userModelPreferenceMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setModelCode(modelCode);
            existing.setUpdatedAt(LocalDateTime.now());
            userModelPreferenceMapper.updateById(existing);
        } else {
            UserModelPreference newPref = new UserModelPreference();
            newPref.setUserId(userId);
            newPref.setModelCode(modelCode);
            newPref.setUpdatedAt(LocalDateTime.now());
            userModelPreferenceMapper.insert(newPref);
        }
    }
}