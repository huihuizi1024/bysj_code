package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.AiModelConfig;
import java.util.List;

public interface AiModelConfigService {
    // 获取所有启用的模型列表
    List<AiModelConfig> listEnabled();
    // 根据code获取模型配置
    AiModelConfig getByCode(String code);
}