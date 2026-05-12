package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.UserSatisfaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户满意度服务接口
 *
 * 基于 Google HEART 框架，收集和管理用户体验指标
 *
 * HEART 框架指标：
 * - Happiness（满意度）：用户对产品的满意程度
 * - Engagement（参与度）：用户与产品的互动深度
 * - Adoption（接受度）：新用户开始使用产品
 * - Retention（留存率）：用户持续使用产品
 * - Task Success（任务成功）：用户完成任务的效果
 *
 * @author HEART Framework Integration
 */
public interface UserSatisfactionService {

    /**
     * 提交用户满意度评价
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param modelCode 模型代码
     * @param happiness 满意度（0-5）
     * @param engagement 参与度（0-1）
     * @param adoption 接受度（0-1）
     * @param retention 留存意愿（0-1）
     * @param taskSuccess 任务成功度（0-1）
     * @param comment 用户反馈（可选）
     * @param improvementSuggestion 改进建议（可选）
     * @return 满意度记录
     */
    UserSatisfaction submitSatisfaction(
            Long userId,
            Long sessionId,
            String modelCode,
            Double happiness,
            Double engagement,
            Double adoption,
            Double retention,
            Double taskSuccess,
            String comment,
            String improvementSuggestion);

    /**
     * 提交简化版满意度评价（用于快速评价）
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param modelCode 模型代码
     * @param overallScore 综合评分（0-5）
     * @return 满意度记录
     */
    UserSatisfaction submitQuickSatisfaction(
            Long userId,
            Long sessionId,
            String modelCode,
            Double overallScore);

    /**
     * 获取指定用户的满意度历史
     *
     * @param userId 用户ID
     * @param limit 返回记录数限制
     * @return 满意度记录列表
     */
    List<UserSatisfaction> getUserHistory(Long userId, int limit);

    /**
     * 获取用户维度统计
     *
     * @param userId 用户ID
     * @return 用户在各维度的平均得分
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 获取平台整体 HEART 指标
     *
     * @param days 时间范围（天数）
     * @return 平台整体 HEART 指标数据
     */
    Map<String, Object> getPlatformStats(int days);

    /**
     * 获取指定模型的 HEART 指标
     *
     * @param modelCode 模型代码
     * @param days 时间范围（天数）
     * @return 模型 HEART 指标数据
     */
    Map<String, Object> getModelHeartStats(String modelCode, int days);

    /**
     * 获取所有模型的 HEART 对比数据
     *
     * @param days 时间范围（天数）
     * @return 各模型 HEART 指标对比
     */
    List<Map<String, Object>> getModelComparison(int days);

    /**
     * 获取用户满意度趋势
     *
     * @param modelCode 模型代码（可选，为空则获取所有模型）
     * @param days 时间范围（天数）
     * @return 每日的满意度数据
     */
    List<Map<String, Object>> getSatisfactionTrend(String modelCode, int days);

    /**
     * 检查用户是否已对某会话提交过满意度
     *
     * @param sessionId 会话ID
     * @return 是否已提交
     */
    boolean hasSubmitted(Long sessionId);
}
