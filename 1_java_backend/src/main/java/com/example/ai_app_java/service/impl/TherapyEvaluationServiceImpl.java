package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.TherapyEvaluation;
import com.example.ai_app_java.mapper.TherapyEvaluationMapper;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.TherapyEvaluationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI疗效评估服务实现
 *
 * 基于 MentalAlign 框架，通过 AI 模型对对话进行疗效评估
 *
 * 评估维度：
 * - CSS (Cognitive Support Score): 认知支持得分
 *   评估 AI 回复在引导性、信息量、专业性、结构化方面的表现
 * - ARS (Affective Resonance Score): 情感共鸣得分
 *   评估 AI 回复在共情表达、情感验证、温暖感、安全感方面的表现
 *
 * @author MentalAlign Framework Integration
 */
@Service
public class TherapyEvaluationServiceImpl implements TherapyEvaluationService {

    @Autowired
    private TherapyEvaluationMapper therapyEvaluationMapper;

    @Autowired
    private AiModelConfigService aiModelConfigService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private org.springframework.core.env.Environment environment;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * MentalAlign 评估系统提示词
     * 指导 AI 从认知支持和情感共鸣两个维度评估回复质量
     */
    private static final String MENTAL_ALIGN_PROMPT = """
        你是一个专业的心理治疗质量评估专家。请评估以下AI心理支持回复的治疗质量。

        【用户输入】
        %s

        【AI回复】
        %s

        请严格按照以下JSON格式返回评估结果，不要有任何额外内容：

        {
            "css": 0.0到1.0之间的数值（认知支持得分）,
            "ars": 0.0到1.0之间的数值（情感共鸣得分）,
            "reasoning": {
                "guidance": "对引导性的简要评价",
                "information": "对信息量的简要评价",
                "professionalism": "对专业性的简要评价",
                "structure": "对结构化的简要评价",
                "empathy": "对共情表达的简要评价",
                "validation": "对情感验证的简要评价",
                "warmth": "对温暖感的简要评价",
                "safety": "对安全感的简要评价"
            }
        }

        评分标准说明：
        - CSS（认知支持得分）评估维度：
          * 引导性(25%): 是否有效引导用户思考，促进自我觉察
          * 信息量(25%): 是否提供有价值的心理知识或实用建议
          * 专业性(25%): 是否正确运用心理学原理和疗法技术
          * 结构化(25%): 回复是否清晰有层次，易于理解

        - ARS（情感共鸣得分）评估维度：
          * 共情表达(25%): 是否准确表达对用户情绪的理解
          * 情感验证(25%): 是否验证用户的情绪体验是合理的
          * 温暖感(25%): 回复是否温暖、有爱心、让人感到被关心
          * 安全感(25%): 是否让用户感到被接纳、被保护
        """;

    /**
     * 调用 AI 模型进行疗效评估
     *
     * @param userMessage 用户消息内容
     * @param aiMessage AI 回复内容
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageId AI 消息ID
     * @param modelCode 模型代码
     * @param clinicalIntent 临床意图
     * @param therapyModule 疗法模块
     * @param interventionDepth 干预深度
     * @param aiRole AI 角色
     * @return 评估记录
     */
    @Override
    public TherapyEvaluation evaluateResponse(
            String userMessage,
            String aiMessage,
            Long userId,
            Long sessionId,
            Long messageId,
            String modelCode,
            String clinicalIntent,
            String therapyModule,
            String interventionDepth,
            String aiRole) {

        try {
            // 调用 AI 模型获取 CSS/ARS 评分
            Map<String, Double> scores = calculateMentalAlignScores(userMessage, aiMessage, modelCode);

            // 构建评估记录
            TherapyEvaluation evaluation = new TherapyEvaluation();
            evaluation.setUserId(userId);
            evaluation.setSessionId(sessionId);
            evaluation.setMessageId(messageId);
            evaluation.setModelCode(modelCode);
            evaluation.setCssScore(scores.get("css"));
            evaluation.setArsScore(scores.get("ars"));
            evaluation.setClinicalIntent(clinicalIntent);
            evaluation.setTherapyModule(therapyModule);
            evaluation.setInterventionDepth(interventionDepth);
            evaluation.setAiRole(aiRole);
            evaluation.setEvaluatedAt(LocalDateTime.now());

            // 保存评估记录
            therapyEvaluationMapper.insert(evaluation);

            return evaluation;
        } catch (Exception e) {
            // 评估失败时返回默认值
            System.out.println("【疗效评估】评估失败：" + e.getMessage());
            TherapyEvaluation evaluation = new TherapyEvaluation();
            evaluation.setUserId(userId);
            evaluation.setSessionId(sessionId);
            evaluation.setMessageId(messageId);
            evaluation.setModelCode(modelCode);
            evaluation.setCssScore(0.5); // 默认中性分数
            evaluation.setArsScore(0.5);
            evaluation.setClinicalIntent(clinicalIntent);
            evaluation.setTherapyModule(therapyModule);
            evaluation.setInterventionDepth(interventionDepth);
            evaluation.setAiRole(aiRole);
            evaluation.setEvaluatedAt(LocalDateTime.now());
            return evaluation;
        }
    }

    /**
     * 调用 AI 模型计算 MentalAlign 评分
     *
     * 使用 AI 模型作为评判者，从认知支持和情感共鸣两个维度评估回复质量
     *
     * @param userMessage 用户消息
     * @param aiMessage AI 回复
     * @param modelCode 评估使用的模型代码
     * @return 包含 css 和 ars 的 Map
     */
    private Map<String, Double> calculateMentalAlignScores(
            String userMessage, String aiMessage, String modelCode) {

        Map<String, Double> result = new HashMap<>();
        result.put("css", 0.5);
        result.put("ars", 0.5);

        try {
            AiModelConfig config = aiModelConfigService.getByCode(modelCode);
            if (config == null) {
                System.out.println("【疗效评估】未找到模型配置，使用默认评分");
                return result;
            }

            String apiKey = environment.getProperty(config.getApiKeyAlias(), "");
            String prompt = String.format(MENTAL_ALIGN_PROMPT, userMessage, aiMessage);

            // 构建请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system",
                "content", "你是一个专业的心理治疗质量评估专家。"));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            org.springframework.http.ResponseEntity<String> response =
                restTemplate.postForEntity(config.getApiUrl(), entity, String.class);

            // 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 解析 JSON 评分
            JsonNode scoreNode = objectMapper.readTree(content);
            double css = scoreNode.path("css").asDouble(0.5);
            double ars = scoreNode.path("ars").asDouble(0.5);

            // 确保分数在有效范围内
            result.put("css", Math.max(0.0, Math.min(1.0, css)));
            result.put("ars", Math.max(0.0, Math.min(1.0, ars)));

        } catch (Exception e) {
            System.out.println("【疗效评估】AI 评分计算失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取指定模型的评估统计数据
     */
    @Override
    public Map<String, Object> getModelStats(String modelCode, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<TherapyEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TherapyEvaluation::getModelCode, modelCode)
               .ge(TherapyEvaluation::getEvaluatedAt, startTime);

        List<TherapyEvaluation> evaluations = therapyEvaluationMapper.selectList(wrapper);

        Map<String, Object> stats = new HashMap<>();
        if (evaluations.isEmpty()) {
            stats.put("avgCss", 0.0);
            stats.put("avgArs", 0.0);
            stats.put("avgUserRating", 0.0);
            stats.put("count", 0);
            return stats;
        }

        // 计算平均值
        double avgCss = evaluations.stream()
            .filter(e -> e.getCssScore() != null)
            .mapToDouble(TherapyEvaluation::getCssScore)
            .average().orElse(0.0);

        double avgArs = evaluations.stream()
            .filter(e -> e.getArsScore() != null)
            .mapToDouble(TherapyEvaluation::getArsScore)
            .average().orElse(0.0);

        double avgUserRating = evaluations.stream()
            .filter(e -> e.getUserRating() != null)
            .mapToDouble(TherapyEvaluation::getUserRating)
            .average().orElse(0.0);

        stats.put("avgCss", Math.round(avgCss * 100) / 100.0);
        stats.put("avgArs", Math.round(avgArs * 100) / 100.0);
        stats.put("avgUserRating", Math.round(avgUserRating * 100) / 100.0);
        stats.put("count", evaluations.size());

        return stats;
    }

    /**
     * 获取所有模型的最新评估对比
     */
    @Override
    public List<Map<String, Object>> getAllModelStats(int days) {
        // 获取所有启用的模型配置
        List<AiModelConfig> models = aiModelConfigService.listEnabled();
        List<Map<String, Object>> result = new ArrayList<>();

        for (AiModelConfig model : models) {
            Map<String, Object> stats = getModelStats(model.getCode(), days);
            stats.put("modelCode", model.getCode());
            stats.put("modelName", model.getName());
            result.add(stats);
        }

        return result;
    }

    /**
     * 获取指定用户的评估历史
     */
    @Override
    public List<TherapyEvaluation> getUserHistory(Long userId, int limit) {
        LambdaQueryWrapper<TherapyEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TherapyEvaluation::getUserId, userId)
               .orderByDesc(TherapyEvaluation::getEvaluatedAt)
               .last("LIMIT " + limit);
        return therapyEvaluationMapper.selectList(wrapper);
    }

    /**
     * 获取指定模型的评估趋势数据
     */
    @Override
    public List<Map<String, Object>> getModelTrend(String modelCode, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<TherapyEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TherapyEvaluation::getModelCode, modelCode)
               .ge(TherapyEvaluation::getEvaluatedAt, startTime)
               .orderByAsc(TherapyEvaluation::getEvaluatedAt);

        List<TherapyEvaluation> evaluations = therapyEvaluationMapper.selectList(wrapper);

        // 按日期分组计算每日平均值
        Map<String, List<TherapyEvaluation>> byDate = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TherapyEvaluation eval : evaluations) {
            String dateKey = eval.getEvaluatedAt().format(formatter);
            byDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(eval);
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<String, List<TherapyEvaluation>> entry : byDate.entrySet()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", entry.getKey());

            double avgCss = entry.getValue().stream()
                .filter(e -> e.getCssScore() != null)
                .mapToDouble(TherapyEvaluation::getCssScore)
                .average().orElse(0.0);

            double avgArs = entry.getValue().stream()
                .filter(e -> e.getArsScore() != null)
                .mapToDouble(TherapyEvaluation::getArsScore)
                .average().orElse(0.0);

            dayData.put("avgCss", Math.round(avgCss * 100) / 100.0);
            dayData.put("avgArs", Math.round(avgArs * 100) / 100.0);
            trend.add(dayData);
        }

        return trend;
    }

    /**
     * 提交用户对 AI 回复的主观评分
     */
    @Override
    public boolean submitUserRating(Long userId, Long sessionId, Long messageId, Double rating, Double userCss, Double userArs) {
        LambdaQueryWrapper<TherapyEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TherapyEvaluation::getSessionId, sessionId)
               .eq(TherapyEvaluation::getMessageId, messageId)
               .last("LIMIT 1");

        TherapyEvaluation evaluation = therapyEvaluationMapper.selectOne(wrapper);
        if (evaluation != null) {
            evaluation.setUserRating(rating);
            evaluation.setUserCss(userCss);
            evaluation.setUserArs(userArs);
            therapyEvaluationMapper.updateById(evaluation);
            return true;
        }
        return false;
    }

    /**
     * 保存评估记录
     */
    @Override
    public void saveEvaluation(TherapyEvaluation evaluation) {
        therapyEvaluationMapper.insert(evaluation);
    }

    /**
     * 获取指定时间范围内的评估记录
     */
    @Override
    public List<TherapyEvaluation> getEvaluationsByTimeRange(
            String modelCode, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<TherapyEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TherapyEvaluation::getModelCode, modelCode)
               .between(TherapyEvaluation::getEvaluatedAt, startTime, endTime)
               .orderByDesc(TherapyEvaluation::getEvaluatedAt);
        return therapyEvaluationMapper.selectList(wrapper);
    }
}
