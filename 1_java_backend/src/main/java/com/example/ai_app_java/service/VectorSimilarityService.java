package com.example.ai_app_java.service;

import java.util.List;

/**
 * 向量相似度服务接口
 *
 * 使用 BGE-base-zh 向量模型计算文本语义相似度
 * 用于危机样本库的语义匹配
 */
public interface VectorSimilarityService {

    /**
     * 将文本向量化（调用 BGE embedding 接口）
     * @param text 输入文本
     * @return float 数组表示的向量
     */
    float[] embed(String text);

    /**
     * 计算两个向量的余弦相似度
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 余弦相似度（0~1）
     */
    double cosineSimilarity(float[] vec1, float[] vec2);

    /**
     * 计算两个文本的语义相似度
     * @param text1 文本1
     * @param text2 文本2
     * @return 余弦相似度（0~1）
     */
    double textSimilarity(String text1, String text2);

    /**
     * 检查用户输入是否与危机样本库匹配
     * @param userInput 用户输入
     * @param threshold 相似度阈值（默认 0.65）
     * @return 匹配的样本ID列表（按相似度降序）
     */
    List<Long> findMatchingSamples(String userInput, double threshold);

    /**
     * 获取最高相似度得分
     * @param userInput 用户输入
     * @param threshold 阈值
     * @return 最高相似度（无匹配返回 -1）
     */
    double getMaxSimilarity(String userInput, double threshold);
}
