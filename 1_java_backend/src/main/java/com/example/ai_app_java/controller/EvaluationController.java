package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.TherapyEvaluation;
import com.example.ai_app_java.entity.UserSatisfaction;
import com.example.ai_app_java.service.EvalService;
import com.example.ai_app_java.service.TherapyEvaluationService;
import com.example.ai_app_java.service.UserSatisfactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评估控制器
 *
 * 提供 MentalAlign 和 HEART 框架的评估接口
 *
 * 主要功能：
 * 1. MentalAlign 疗效评估：CSS/ARS 评分
 * 2. HEART 用户满意度：Happiness/Engagement/Adoption/Retention/Task Success
 * 3. 模型效果对比：多模型横向对比分析
 *
 * @author MentalAlign + HEART Framework Integration
 */
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    @Autowired
    private TherapyEvaluationService therapyEvaluationService;

    @Autowired
    private UserSatisfactionService userSatisfactionService;

    @Autowired
    private EvalService evalService;

    // ==================== MentalAlign 疗效评估接口 ====================

    /**
     * 获取指定模型的评估统计数据
     *
     * GET /evaluation/therapy/stats?modelCode=DEEPSEEK&days=7
     */
    @GetMapping("/therapy/stats")
    public Result getTherapyStats(
            @RequestParam(required = false) String modelCode,
            @RequestParam(defaultValue = "7") int days) {
        if (modelCode != null && !modelCode.isEmpty()) {
            Map<String, Object> stats = therapyEvaluationService.getModelStats(modelCode, days);
            return Result.success("获取成功", stats);
        } else {
            // 返回所有模型的统计数据
            List<Map<String, Object>> allStats = therapyEvaluationService.getAllModelStats(days);
            return Result.success("获取成功", allStats);
        }
    }

    /**
     * 获取所有模型的疗效对比（从 eval_run 批量评测结果读取）
     *
     * GET /evaluation/therapy/compare?days=7
     */
    @GetMapping("/therapy/compare")
    public Result compareModels(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> comparison = evalService.getModelStatsFromRuns(days);
        return Result.success("获取成功", comparison);
    }

    /**
     * 获取模型的评估趋势
     *
     * GET /evaluation/therapy/trend?modelCode=DEEPSEEK&days=30
     */
    @GetMapping("/therapy/trend")
    public Result getTherapyTrend(
            @RequestParam String modelCode,
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = therapyEvaluationService.getModelTrend(modelCode, days);
        return Result.success("获取成功", trend);
    }

    /**
     * 获取用户的疗效评估历史
     *
     * GET /evaluation/therapy/history?limit=20
     */
    @GetMapping("/therapy/history")
    public Result getTherapyHistory(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        List<TherapyEvaluation> history = therapyEvaluationService.getUserHistory(userId, limit);
        return Result.success("获取成功", history);
    }

    /**
     * 提交用户对 AI 回复的主观评分
     *
     * POST /evaluation/therapy/rating
     * Body: { sessionId, messageId, rating, userCss, userArs }
     */
    @PostMapping("/therapy/rating")
    public Result submitUserRating(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody Map<String, Object> body) {
        Long sessionId = ((Number) body.get("sessionId")).longValue();
        Long messageId = ((Number) body.get("messageId")).longValue();
        Double rating = body.get("rating") != null ? ((Number) body.get("rating")).doubleValue() : null;
        Double userCss = body.get("userCss") != null ? ((Number) body.get("userCss")).doubleValue() : null;
        Double userArs = body.get("userArs") != null ? ((Number) body.get("userArs")).doubleValue() : null;

        boolean success = therapyEvaluationService.submitUserRating(userId, sessionId, messageId, rating, userCss, userArs);
        if (success) {
            return Result.success("评分提交成功", null);
        } else {
            return Result.fail(400, "评分提交失败，找不到对应的评估记录");
        }
    }

    // ==================== HEART 用户满意度接口 ====================

    /**
     * 提交用户满意度评价
     *
     * POST /evaluation/satisfaction
     * Body: { sessionId, modelCode, happiness, engagement, adoption, retention, taskSuccess, comment, improvementSuggestion }
     */
    @PostMapping("/satisfaction")
    public Result submitSatisfaction(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody Map<String, Object> body) {
        Long sessionId = ((Number) body.get("sessionId")).longValue();
        String modelCode = (String) body.get("modelCode");

        // 提取 HEART 指标
        Double happiness = body.get("happiness") != null
            ? ((Number) body.get("happiness")).doubleValue() : null;
        Double engagement = body.get("engagement") != null
            ? ((Number) body.get("engagement")).doubleValue() : null;
        Double adoption = body.get("adoption") != null
            ? ((Number) body.get("adoption")).doubleValue() : null;
        Double retention = body.get("retention") != null
            ? ((Number) body.get("retention")).doubleValue() : null;
        Double taskSuccess = body.get("taskSuccess") != null
            ? ((Number) body.get("taskSuccess")).doubleValue() : null;
        String comment = (String) body.get("comment");
        String improvementSuggestion = (String) body.get("improvementSuggestion");

        UserSatisfaction satisfaction = userSatisfactionService.submitSatisfaction(
            userId, sessionId, modelCode,
            happiness, engagement, adoption, retention, taskSuccess,
            comment, improvementSuggestion);

        return Result.success("满意度提交成功", satisfaction);
    }

    /**
     * 提交快速满意度评价
     *
     * POST /evaluation/satisfaction/quick
     * Body: { sessionId, modelCode, overallScore }
     */
    @PostMapping("/satisfaction/quick")
    public Result submitQuickSatisfaction(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody Map<String, Object> body) {
        Long sessionId = ((Number) body.get("sessionId")).longValue();
        String modelCode = (String) body.get("modelCode");
        Double overallScore = ((Number) body.get("overallScore")).doubleValue();

        UserSatisfaction satisfaction = userSatisfactionService.submitQuickSatisfaction(
            userId, sessionId, modelCode, overallScore);

        return Result.success("评价提交成功", satisfaction);
    }

    /**
     * 获取用户满意度历史
     *
     * GET /evaluation/satisfaction/history?limit=20
     */
    @GetMapping("/satisfaction/history")
    public Result getSatisfactionHistory(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        List<UserSatisfaction> history = userSatisfactionService.getUserHistory(userId, limit);
        return Result.success("获取成功", history);
    }

    /**
     * 获取用户维度统计
     *
     * GET /evaluation/satisfaction/user-stats
     */
    @GetMapping("/satisfaction/user-stats")
    public Result getUserStats(
            @RequestAttribute("currentUserId") Long userId) {
        Map<String, Object> stats = userSatisfactionService.getUserStats(userId);
        return Result.success("获取成功", stats);
    }

    /**
     * 获取指定模型的 HEART 指标
     *
     * GET /evaluation/satisfaction/model?modelCode=DEEPSEEK&days=7
     */
    @GetMapping("/satisfaction/model")
    public Result getModelHeartStats(
            @RequestParam String modelCode,
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> stats = userSatisfactionService.getModelHeartStats(modelCode, days);
        return Result.success("获取成功", stats);
    }

    /**
     * 获取所有模型的 HEART 对比（从 eval_run 批量评测结果读取）
     *
     * GET /evaluation/satisfaction/compare?days=7
     */
    @GetMapping("/satisfaction/compare")
    public Result compareHeartModels(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> comparison = evalService.getHeartComparisonFromRuns(days);
        return Result.success("获取成功", comparison);
    }

    /**
     * 获取用户满意度趋势
     *
     * GET /evaluation/satisfaction/trend?modelCode=&days=30
     */
    @GetMapping("/satisfaction/trend")
    public Result getSatisfactionTrend(
            @RequestParam(required = false) String modelCode,
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = userSatisfactionService.getSatisfactionTrend(modelCode, days);
        return Result.success("获取成功", trend);
    }

    /**
     * 获取平台整体 HEART 指标（从 eval_run 批量评测结果读取）
     *
     * GET /evaluation/satisfaction/platform?days=7
     */
    @GetMapping("/satisfaction/platform")
    public Result getPlatformStats(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> stats = evalService.getPlatformStatsFromRuns(days);
        return Result.success("获取成功", stats);
    }

    /**
     * 检查用户是否已对某会话提交过满意度
     *
     * GET /evaluation/satisfaction/check?sessionId=1
     */
    @GetMapping("/satisfaction/check")
    public Result checkSubmitted(
            @RequestParam Long sessionId) {
        boolean hasSubmitted = userSatisfactionService.hasSubmitted(sessionId);
        return Result.success("查询成功", Map.of("hasSubmitted", hasSubmitted));
    }

    // ==================== 综合评估接口 ====================

    /**
     * 获取综合评估报告
     *
     * GET /evaluation/report?days=7
     */
    @GetMapping("/report")
    public Result getComprehensiveReport(
            @RequestParam(defaultValue = "7") int days) {
        // 获取 MentalAlign 数据
        List<Map<String, Object>> therapyComparison = evalService.getModelStatsFromRuns(days);

        // 获取 HEART 数据
        List<Map<String, Object>> heartComparison = userSatisfactionService.getModelComparison(days);

        // 获取平台整体数据
        Map<String, Object> platformStats = userSatisfactionService.getPlatformStats(days);

        return Result.success("获取成功", Map.of(
            "therapyComparison", therapyComparison,
            "heartComparison", heartComparison,
            "platformStats", platformStats,
            "reportPeriod", days + "天"
        ));
    }
}
