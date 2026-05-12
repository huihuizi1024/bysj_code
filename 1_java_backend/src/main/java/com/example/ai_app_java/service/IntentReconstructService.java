package com.example.ai_app_java.service;

import java.util.List;

/**
 * 意图重构服务接口
 *
 * 将用户非结构化输入重构为临床可操作的意图分类
 */
public interface IntentReconstructService {

    /**
     * 重构用户输入的隐性意图
     * @param userInput 用户输入文本
     * @param emotionType 情绪类型
     * @param emotionScore 情绪得分
     * @return 意图分析结果
     */
    IntentResult reconstruct(String userInput, String emotionType, Double emotionScore);

    /**
     * 获取所有支持的意图分类
     * @return 意图列表
     */
    List<IntentInfo> getAllIntents();

    /**
     * 根据意图代码获取推荐的治疗模块
     * @param intentCode 意图代码
     * @return 疗法维度（如 "CBT,ACT"）
     */
    String getTherapyDimensions(String intentCode);

    /**
     * 意图分析结果
     */
    record IntentResult(
        String latentNeed,      // 隐性需求描述
        String clinicalIntent,  // 临床意图代码
        String therapyModule,   // 疗法模块（如 "ACT_value_clarification"）
        double confidence       // 置信度 0~1
    ) {}

    /**
     * 意图信息
     */
    record IntentInfo(
        String code,
        String name,
        String description,
        String therapyDimensions,
        String aiRole
    ) {}
}
