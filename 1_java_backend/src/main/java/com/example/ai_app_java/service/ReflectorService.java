package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.ReflectorResult;

/**
 * Reflector 服务接口（输出安全层）
 *
 * 对 AI 生成内容进行合规性审计，检测：
 * 1. 自我伤害建议
 * 2. 医疗处方/诊断
 * 3. 歧视性/偏见性语言
 * 4. 违反伦理准则的内容
 */
public interface ReflectorService {

    /**
     * 审计 AI 回复内容
     * @param aiResponse AI 生成的完整回复
     * @return Reflector 审计结果
     */
    ReflectorResult audit(String aiResponse);

    /**
     * 实时流式审计（对每个 token 片段进行检查）
     * @param partialContent 当前累积的流式内容
     * @return 若发现违规内容，返回修正后的安全回复；否则返回 null
     */
    String auditStream(String partialContent);
}
