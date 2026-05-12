package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.UserSatisfaction;
import com.example.ai_app_java.mapper.UserSatisfactionMapper;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.UserSatisfactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户满意度服务实现
 *
 * 基于 Google HEART 框架，收集和管理用户体验指标
 *
 * HEART 框架指标：
 * - Happiness（满意度）：用户对产品的满意程度（0-5分）
 * - Engagement（参与度）：用户与产品的互动深度（0-1）
 * - Adoption（接受度）：新用户开始使用产品（0-1）
 * - Retention（留存率）：用户持续使用产品（0-1）
 * - Task Success（任务成功）：用户完成任务的效果（0-1）
 *
 * @author HEART Framework Integration
 */
@Service
public class UserSatisfactionServiceImpl implements UserSatisfactionService {

    @Autowired
    private UserSatisfactionMapper userSatisfactionMapper;

    @Autowired
    private AiModelConfigService aiModelConfigService;

    /**
     * HEART 指标权重配置
     * 用于计算综合评分
     */
    private static final double WEIGHT_HAPPINESS = 0.30;    // 满意度权重
    private static final double WEIGHT_ENGAGEMENT = 0.15;  // 参与度权重
    private static final double WEIGHT_ADOPTION = 0.15;    // 接受度权重
    private static final double WEIGHT_RETENTION = 0.20;   // 留存意愿权重
    private static final double WEIGHT_TASK_SUCCESS = 0.20; // 任务成功权重

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
     * @param comment 用户反馈
     * @param improvementSuggestion 改进建议
     * @return 满意度记录
     */
    @Override
    public UserSatisfaction submitSatisfaction(
            Long userId,
            Long sessionId,
            String modelCode,
            Double happiness,
            Double engagement,
            Double adoption,
            Double retention,
            Double taskSuccess,
            String comment,
            String improvementSuggestion) {

        // 计算综合评分
        double overallScore = calculateOverallScore(
            happiness, engagement, adoption, retention, taskSuccess);

        // 构建记录
        UserSatisfaction satisfaction = new UserSatisfaction();
        satisfaction.setUserId(userId);
        satisfaction.setSessionId(sessionId);
        satisfaction.setModelCode(modelCode);
        satisfaction.setHappiness(normalizeToRange(happiness, 0, 5));
        satisfaction.setEngagement(normalizeToRange(engagement, 0, 1));
        satisfaction.setAdoption(normalizeToRange(adoption, 0, 1));
        satisfaction.setRetention(normalizeToRange(retention, 0, 1));
        satisfaction.setTaskSuccess(normalizeToRange(taskSuccess, 0, 1));
        satisfaction.setOverallScore(normalizeToRange(overallScore, 0, 5));
        satisfaction.setComment(comment);
        satisfaction.setImprovementSuggestion(improvementSuggestion);
        satisfaction.setSubmittedAt(LocalDateTime.now());

        // 保存
        userSatisfactionMapper.insert(satisfaction);
        return satisfaction;
    }

    /**
     * 提交简化版满意度评价（用于快速评价）
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param modelCode 模型代码
     * @param overallScore 综合评分（0-5）
     * @return 满意度记录
     */
    @Override
    public UserSatisfaction submitQuickSatisfaction(
            Long userId,
            Long sessionId,
            String modelCode,
            Double overallScore) {

        // 快速评价模式下，其他指标使用综合评分的归一化值
        double normalizedScore = normalizeToRange(overallScore, 0, 5);

        UserSatisfaction satisfaction = new UserSatisfaction();
        satisfaction.setUserId(userId);
        satisfaction.setSessionId(sessionId);
        satisfaction.setModelCode(modelCode);
        // 快速模式下，所有 HEART 指标使用相同的归一化分数
        satisfaction.setHappiness(normalizedScore);
        satisfaction.setEngagement(normalizedScore / 5.0); // 归一化到 0-1
        satisfaction.setAdoption(normalizedScore / 5.0);
        satisfaction.setRetention(normalizedScore / 5.0);
        satisfaction.setTaskSuccess(normalizedScore / 5.0);
        satisfaction.setOverallScore(normalizedScore);
        satisfaction.setSubmittedAt(LocalDateTime.now());

        userSatisfactionMapper.insert(satisfaction);
        return satisfaction;
    }

    /**
     * 计算 HEART 综合评分
     *
     * 根据 HEART 五个指标加权计算综合得分
     *
     * @param happiness 满意度（0-5）
     * @param engagement 参与度（0-1）
     * @param adoption 接受度（0-1）
     * @param retention 留存意愿（0-1）
     * @param taskSuccess 任务成功度（0-1）
     * @return 综合评分（0-5）
     */
    private double calculateOverallScore(
            Double happiness,
            Double engagement,
            Double adoption,
            Double retention,
            Double taskSuccess) {

        // 将所有指标归一化到 0-1 范围
        double h = normalizeToRange(happiness, 0, 5) / 5.0;
        double e = normalizeToRange(engagement, 0, 1);
        double a = normalizeToRange(adoption, 0, 1);
        double r = normalizeToRange(retention, 0, 1);
        double t = normalizeToRange(taskSuccess, 0, 1);

        // 加权计算
        double score = h * WEIGHT_HAPPINESS +
                      e * WEIGHT_ENGAGEMENT +
                      a * WEIGHT_ADOPTION +
                      r * WEIGHT_RETENTION +
                      t * WEIGHT_TASK_SUCCESS;

        // 将结果转换回 0-5 范围
        return score * 5.0;
    }

    /**
     * 将值归一化到指定范围
     */
    private double normalizeToRange(Double value, double min, double max) {
        if (value == null) {
            return (max - min) / 2.0 + min; // 返回中值
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 获取指定用户的满意度历史
     */
    @Override
    public List<UserSatisfaction> getUserHistory(Long userId, int limit) {
        LambdaQueryWrapper<UserSatisfaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSatisfaction::getUserId, userId)
               .orderByDesc(UserSatisfaction::getSubmittedAt)
               .last("LIMIT " + limit);
        return userSatisfactionMapper.selectList(wrapper);
    }

    /**
     * 获取用户维度统计
     */
    @Override
    public Map<String, Object> getUserStats(Long userId) {
        LambdaQueryWrapper<UserSatisfaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSatisfaction::getUserId, userId);

        List<UserSatisfaction> records = userSatisfactionMapper.selectList(wrapper);

        Map<String, Object> stats = new HashMap<>();
        if (records.isEmpty()) {
            // 返回默认统计
            stats.put("avgHappiness", 0.0);
            stats.put("avgEngagement", 0.0);
            stats.put("avgAdoption", 0.0);
            stats.put("avgRetention", 0.0);
            stats.put("avgTaskSuccess", 0.0);
            stats.put("avgOverall", 0.0);
            stats.put("count", 0);
            return stats;
        }

        // 计算各项平均值
        stats.put("avgHappiness", calculateAvg(records, UserSatisfaction::getHappiness));
        stats.put("avgEngagement", calculateAvg(records, UserSatisfaction::getEngagement));
        stats.put("avgAdoption", calculateAvg(records, UserSatisfaction::getAdoption));
        stats.put("avgRetention", calculateAvg(records, UserSatisfaction::getRetention));
        stats.put("avgTaskSuccess", calculateAvg(records, UserSatisfaction::getTaskSuccess));
        stats.put("avgOverall", calculateAvg(records, UserSatisfaction::getOverallScore));
        stats.put("count", records.size());

        // 模型偏好统计
        Map<String, Long> modelPreference = new HashMap<>();
        for (UserSatisfaction s : records) {
            String model = s.getModelCode();
            modelPreference.merge(model, 1L, Long::sum);
        }
        stats.put("modelPreference", modelPreference);

        return stats;
    }

    /**
     * 获取平台整体 HEART 指标
     */
    @Override
    public Map<String, Object> getPlatformStats(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<UserSatisfaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(UserSatisfaction::getSubmittedAt, startTime);

        List<UserSatisfaction> records = userSatisfactionMapper.selectList(wrapper);

        return buildStatsMap(records);
    }

    /**
     * 获取指定模型的 HEART 指标
     */
    @Override
    public Map<String, Object> getModelHeartStats(String modelCode, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<UserSatisfaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSatisfaction::getModelCode, modelCode)
               .ge(UserSatisfaction::getSubmittedAt, startTime);

        List<UserSatisfaction> records = userSatisfactionMapper.selectList(wrapper);

        Map<String, Object> stats = buildStatsMap(records);
        stats.put("modelCode", modelCode);

        // 获取模型名称
        AiModelConfig config = aiModelConfigService.getByCode(modelCode);
        if (config != null) {
            stats.put("modelName", config.getName());
        }

        return stats;
    }

    /**
     * 获取所有模型的 HEART 对比数据
     */
    @Override
    public List<Map<String, Object>> getModelComparison(int days) {
        // 使用 listEnabled() 获取所有启用的模型
        List<AiModelConfig> models = aiModelConfigService.listEnabled();
        List<Map<String, Object>> result = new ArrayList<>();

        for (AiModelConfig model : models) {
            Map<String, Object> stats = getModelHeartStats(model.getCode(), days);
            result.add(stats);
        }

        // 按综合评分排序
        result.sort((a, b) -> {
            Double scoreA = (Double) a.getOrDefault("avgOverall", 0.0);
            Double scoreB = (Double) b.getOrDefault("avgOverall", 0.0);
            return scoreB.compareTo(scoreA);
        });

        return result;
    }

    /**
     * 获取用户满意度趋势
     */
    @Override
    public List<Map<String, Object>> getSatisfactionTrend(String modelCode, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<UserSatisfaction> wrapper = new LambdaQueryWrapper<>();
        if (modelCode != null && !modelCode.isEmpty()) {
            wrapper.eq(UserSatisfaction::getModelCode, modelCode);
        }
        wrapper.ge(UserSatisfaction::getSubmittedAt, startTime)
               .orderByAsc(UserSatisfaction::getSubmittedAt);

        List<UserSatisfaction> records = userSatisfactionMapper.selectList(wrapper);

        // 按日期分组
        Map<String, List<UserSatisfaction>> byDate = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (UserSatisfaction record : records) {
            String dateKey = record.getSubmittedAt().format(formatter);
            byDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(record);
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<String, List<UserSatisfaction>> entry : byDate.entrySet()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", entry.getKey());
            dayData.put("stats", buildStatsMap(entry.getValue()));
            trend.add(dayData);
        }

        return trend;
    }

    /**
     * 检查用户是否已对某会话提交过满意度
     */
    @Override
    public boolean hasSubmitted(Long sessionId) {
        LambdaQueryWrapper<UserSatisfaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSatisfaction::getSessionId, sessionId);
        return userSatisfactionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 构建统计数据 Map
     */
    private Map<String, Object> buildStatsMap(List<UserSatisfaction> records) {
        Map<String, Object> stats = new HashMap<>();

        if (records.isEmpty()) {
            stats.put("avgHappiness", 0.0);
            stats.put("avgEngagement", 0.0);
            stats.put("avgAdoption", 0.0);
            stats.put("avgRetention", 0.0);
            stats.put("avgTaskSuccess", 0.0);
            stats.put("avgOverall", 0.0);
            stats.put("count", 0);
            return stats;
        }

        stats.put("avgHappiness", Math.round(calculateAvg(records, UserSatisfaction::getHappiness) * 100) / 100.0);
        stats.put("avgEngagement", Math.round(calculateAvg(records, UserSatisfaction::getEngagement) * 100) / 100.0);
        stats.put("avgAdoption", Math.round(calculateAvg(records, UserSatisfaction::getAdoption) * 100) / 100.0);
        stats.put("avgRetention", Math.round(calculateAvg(records, UserSatisfaction::getRetention) * 100) / 100.0);
        stats.put("avgTaskSuccess", Math.round(calculateAvg(records, UserSatisfaction::getTaskSuccess) * 100) / 100.0);
        stats.put("avgOverall", Math.round(calculateAvg(records, UserSatisfaction::getOverallScore) * 100) / 100.0);
        stats.put("count", records.size());

        return stats;
    }

    /**
     * 计算平均值
     */
    private double calculateAvg(List<UserSatisfaction> records,
            java.util.function.Function<UserSatisfaction, Double> getter) {
        return records.stream()
            .map(getter)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
}
