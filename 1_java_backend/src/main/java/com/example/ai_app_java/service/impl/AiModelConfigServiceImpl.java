package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.mapper.AiModelConfigMapper;
import com.example.ai_app_java.service.AiModelConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiModelConfigServiceImpl implements AiModelConfigService {

    @Autowired
    private AiModelConfigMapper aiModelConfigMapper;

    @Override
    public List<AiModelConfig> listEnabled() {
        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelConfig::getEnabled, 1)
               .orderByAsc(AiModelConfig::getSortOrder);
        return aiModelConfigMapper.selectList(wrapper);
    }

    @Override
    public AiModelConfig getByCode(String code) {
        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelConfig::getCode, code)
               .eq(AiModelConfig::getEnabled, 1);
        return aiModelConfigMapper.selectOne(wrapper);
    }
}