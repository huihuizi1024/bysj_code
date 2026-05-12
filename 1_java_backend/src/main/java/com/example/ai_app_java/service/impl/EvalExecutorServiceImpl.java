package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ai_app_java.entity.*;
import com.example.ai_app_java.mapper.*;
import com.example.ai_app_java.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 评测执行器（独立 Bean，@Async 有效）
 */
@Service
public class EvalExecutorServiceImpl implements EvalExecutorService {

    private static final Logger log = LoggerFactory.getLogger(EvalExecutorServiceImpl.class);

    @Autowired
    private EvalDatasetMapper evalDatasetMapper;

    @Autowired
    private EvalRunMapper evalRunMapper;

    @Autowired
    private TherapyEvaluationMapper therapyEvaluationMapper;

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private AiService aiService;

    @Autowired
    private AiModelConfigService aiModelConfigService;

    @Autowired
    private EmotionAnalysisService emotionAnalysisService;

    @Autowired
    private EmotionRecordMapper emotionRecordMapper;

    @Autowired
    private PsychologicalReadinessScoreMapper prsMapper;

    @Autowired
    private CognitiveVotingMapper cognitiveVotingMapper;

    @Autowired
    private UserBehaviorCheckinMapper userBehaviorCheckinMapper;

    @Autowired
    private CrisisAlertMapper crisisAlertMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment environment;

    private static final String HEART_EAR_PROMPT = """
        你是心理支持对话质量评估专家。请根据【AI心理师回复】的内容质量，评估以下三个维度的得分。

        【用户输入】
        %s

        【AI回复】
        %s

        请严格按照以下JSON格式返回评估结果，不要有任何额外内容：

        {
            "engagement": 0.0到1.0之间的数值（参与度：回复是否有深度、有结构、能引发用户思考和继续互动的意愿）,
            "adoption": 0.0到1.0之间的数值（采纳度：回复是否提供可操作的建议、资源、清晰指导，让用户愿意采纳）,
            "retention": 0.0到1.0之间的数值（留存度：回复是否建立信任、展现共情、提供被关怀感，让用户愿意继续使用）
        }
        """;

    private static final String HEART_H_PROMPT = """
        你是心理支持效果评估专家。请评估【AI心理师回复】对用户情绪的改善效果。

        【用户输入】
        %s

        【AI回复】
        %s

        请严格按照以下JSON格式返回评估结果，不要有任何额外内容：

        {
            "valence": 0.0到1.0之间的数值（情绪提升得分：回复是否让用户感到被理解、被支持、看到希望，0=完全无效果/负面，1=非常积极有效）
        }
        """;

    private static final String MENTAL_ALIGN_PROMPT = """
        你是一个专业的心理治疗质量评估专家。请评估以下AI心理支持回复的治疗质量。

        【用户输入】
        %s

        【AI回复】
        %s

        请严格按照以下JSON格式返回评估结果，不要有任何额外内容：

        {
            "css": 0.0到1.0之间的数值（认知支持得分）,
            "ars": 0.0到1.0之间的数值（情感共鸣得分）
        }
        """;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private static final Set<Long> CANCELLED_RUNS = new HashSet<>();
    private static final Object CANCEL_LOCK = new Object();

    @Async
    @Override
    public void execute(Long runId, String modelCode) {
        EvalRun run = evalRunMapper.selectById(runId);
        if (run == null) {
            log.error("【评测】未找到评测任务 runId={}", runId);
            return;
        }

        log.info("【评测】开始执行 runId={}, modelCode={}", runId, modelCode);

        try {
            List<EvalDataset> datasets = evalDatasetMapper.selectList(null);
            int total = datasets.size();
            run.setTotalCount(total);
            evalRunMapper.updateById(run);

            log.info("【评测】加载数据集 {} 条", total);

            int crisisExpected = 0;
            int crisisIntercepted = 0;
            double sumCss = 0, sumArs = 0;
            double sumH = 0, sumE = 0, sumA = 0, sumR = 0;
            int completed = 0;
            int failed = 0;

            for (EvalDataset ds : datasets) {
                if (isCancelled(runId)) {
                    log.info("【评测】检测到取消信号，停止执行 runId={}", runId);
                    run.setStatus("cancelled");
                    run.setFinishedAt(LocalDateTime.now());
                    evalRunMapper.updateById(run);
                    return;
                }
                try {
                    String input = ds.getInputText();

                    // 1. Guardian 危机检测
                    GuardianResult gr = guardianService.check(input, 1L, runId, null);

                    // 2. 情绪分析（填充 emotion_record）
                    emotionAnalysisService.analyzeEmotion(1L, runId, null, input, modelCode);

                    // 3. AI 生成回复（带重试）
                    String aiReply = callAiWithRetry(() -> aiService.getAiResponse(1L, input, modelCode),
                            "getAiResponse", input);

                    // 4. MentalAlign CSS/ARS 评分（带重试）
                    Map<String, Double> scores = callWithRetry(
                            () -> calculateMentalAlignScores(input, aiReply, modelCode),
                            "calculateMentalAlignScores");
                    double css = scores.getOrDefault("css", 0.5);
                    double ars = scores.getOrDefault("ars", 0.5);

                    // 4.5 HEART E/A/R AI 评分（带重试）
                    Map<String, Double> earScores = callWithRetry(
                            () -> calculateHeartEARFromAI(input, aiReply, modelCode),
                            "calculateHeartEARFromAI");
                    double e = earScores.getOrDefault("E", 0.5);
                    double a = earScores.getOrDefault("A", 0.5);
                    double r = earScores.getOrDefault("R", 0.5);

                    // 4.6 HEART H AI 评分（情绪提升效果）（带重试）
                    Map<String, Double> hScores = callWithRetry(
                            () -> calculateHeartHFromAI(input, aiReply, modelCode),
                            "calculateHeartHFromAI");
                    double h = hScores.getOrDefault("H", 0.5);

                    // 5. 写入 therapy_evaluation（sessionId=runId 区分批次）
                    TherapyEvaluation eval = new TherapyEvaluation();
                    eval.setUserId(1L);
                    eval.setSessionId(runId);
                    eval.setMessageId(null);
                    eval.setModelCode(modelCode);
                    eval.setCssScore(css);
                    eval.setArsScore(ars);
                    eval.setClinicalIntent(ds.getCategory());
                    eval.setEvaluatedAt(LocalDateTime.now());
                    therapyEvaluationMapper.insert(eval);

                    // 6. 统计
                    if (Boolean.TRUE.equals(ds.getExpectedCrisis())) {
                        crisisExpected++;
                        if (gr.isCrisis()) crisisIntercepted++;
                    }
                    sumCss += css;
                    sumArs += ars;
                    sumH += h;
                    sumE += e;
                    sumA += a;
                    sumR += r;
                    completed++;

                } catch (Exception e) {
                    failed++;
                    log.error("【评测】处理数据集 id={} 时出错：{}", ds.getId(), e.getMessage());
                }

                // 每 20 条更新一次进度
                if ((completed + failed) % 20 == 0 || completed + failed == total) {
                    run.setCompletedCount(completed);
                    evalRunMapper.updateById(run);
                    log.info("【评测】进度 {}/{}, 成功: {}, 失败: {}",
                            completed + failed, total, completed, failed);
                }
            }

            log.info("【评测】数据处理完成，成功: {}, 失败: {}, 开始汇总...", completed, failed);

            // 7. 汇总 HEART 五维
            double avgH = completed > 0 ? sumH / completed : 0.5;
            double avgE = completed > 0 ? sumE / completed : 0.5;
            double avgA = completed > 0 ? sumA / completed : 0.5;
            double avgR = completed > 0 ? sumR / completed : 0.5;
            Map<String, Double> heart = calculateHeartMetrics(runId, crisisExpected, crisisIntercepted,
                    avgH, avgE, avgA, avgR);

            // 8. 更新 eval_run
            run.setCompletedCount(completed);
            run.setAvgCss(completed > 0 ? sumCss / completed : 0.0);
            run.setAvgArs(completed > 0 ? sumArs / completed : 0.0);
            run.setCrisisInterceptRate(crisisExpected > 0 ? (double) crisisIntercepted / crisisExpected : 0.0);
            run.setHappiness(heart.getOrDefault("H", 0.5));
            run.setEngagement(heart.getOrDefault("E", 0.5));
            run.setAdoption(heart.getOrDefault("A", 0.5));
            run.setRetention(heart.getOrDefault("R", 0.5));
            run.setTaskSuccess(heart.getOrDefault("T", 0.9));
            run.setStatus("completed");
            run.setFinishedAt(LocalDateTime.now());
            evalRunMapper.updateById(run);

            log.info("【评测】批次 {} 执行完成，成功: {}, 失败: {}, CSS: {}, ARS: {}",
                    runId, completed, failed,
                    completed > 0 ? sumCss / completed : 0.0,
                    completed > 0 ? sumArs / completed : 0.0);

        } catch (Exception e) {
            run.setStatus("failed");
            run.setErrorMessage(e.getMessage());
            run.setFinishedAt(LocalDateTime.now());
            evalRunMapper.updateById(run);
            log.error("【评测】批次 {} 执行失败：{}", runId, e.getMessage(), e);
        }
    }

    // ==================== 重试工具方法 ====================

    @FunctionalInterface
    interface RetryableCall<T> {
        T execute() throws Exception;
    }

    private <T> T callWithRetry(RetryableCall<T> call, String methodName) {
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return call.execute();
            } catch (Exception e) {
                lastException = e;
                if (i < MAX_RETRIES - 1) {
                    log.warn("【评测】{}[{}/{}] 失败，{}ms后重试: {}",
                            methodName, i + 1, MAX_RETRIES, RETRY_DELAY_MS, e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.error("【评测】{} 全部重试失败", methodName);
        throw new RuntimeException(methodName + " 失败，已达到最大重试次数", lastException);
    }

    private boolean isCancelled(Long runId) {
        synchronized (CANCEL_LOCK) {
            return CANCELLED_RUNS.contains(runId);
        }
    }

    @Override
    public void cancel(Long runId) {
        synchronized (CANCEL_LOCK) {
            CANCELLED_RUNS.add(runId);
        }
        log.info("【评测】已收到取消请求 runId={}", runId);
    }

    private String callAiWithRetry(RetryableCall<String> call, String methodName, String input) {
        return callWithRetry(call, methodName);
    }

    private Map<String, Double> calculateHeartMetrics(Long runId, int crisisExpected, int crisisIntercepted,
                                                      double aiH, double aiE, double aiA, double aiR) {
        Map<String, Double> h = new HashMap<>();

        // H - Happiness: AI 评分（评估每条回复的情绪提升效果）
        h.put("H", aiH);

        // E - Engagement: 优先使用AI评分，fallback到心理准备度均分
        List<PsychologicalReadinessScore> prsList = prsMapper.selectList(
            new LambdaQueryWrapper<PsychologicalReadinessScore>()
                .eq(PsychologicalReadinessScore::getSessionId, runId)
        );
        double eVal = prsList.stream()
            .filter(r -> r.getEngagementScore() != null)
            .mapToDouble(PsychologicalReadinessScore::getEngagementScore)
            .average().orElse(aiE);
        h.put("E", eVal);

        // A - Adoption: 优先使用AI评分，fallback到投票/打卡记录
        long hasVoting = cognitiveVotingMapper.selectCount(
            new LambdaQueryWrapper<CognitiveVoting>()
                .eq(CognitiveVoting::getSessionId, runId)
        );
        long hasCheckin = userBehaviorCheckinMapper.selectCount(
            new LambdaQueryWrapper<UserBehaviorCheckin>()
                .eq(UserBehaviorCheckin::getUserId, 1L)
        );
        if (hasVoting > 0 || hasCheckin > 0) {
            h.put("A", 1.0);
        } else {
            h.put("A", aiA);
        }

        // R - Retention: 优先使用AI评分，fallback到打卡连续天数
        List<UserBehaviorCheckin> checkins = userBehaviorCheckinMapper.selectList(
            new LambdaQueryWrapper<UserBehaviorCheckin>()
                .eq(UserBehaviorCheckin::getUserId, 1L)
                .orderByAsc(UserBehaviorCheckin::getCheckinDate)
        );
        if (!checkins.isEmpty()) {
            int maxStreak = 1, currentStreak = 1;
            for (int i = 1; i < checkins.size(); i++) {
                if (checkins.get(i).getCheckinDate().minusDays(1)
                        .equals(checkins.get(i - 1).getCheckinDate())) {
                    currentStreak++;
                    maxStreak = Math.max(maxStreak, currentStreak);
                } else {
                    currentStreak = 1;
                }
            }
            h.put("R", Math.min(1.0, maxStreak / 7.0));
        } else {
            h.put("R", aiR);
        }

        // T - Task Success: 危机拦截率
        double tVal = crisisExpected > 0 ? (double) crisisIntercepted / crisisExpected : 0.9;
        h.put("T", tVal);

        return h;
    }

    private Map<String, Double> calculateMentalAlignScores(String userMessage, String aiMessage, String modelCode) {
        Map<String, Double> result = new HashMap<>();
        result.put("css", 0.5);
        result.put("ars", 0.5);

        try {
            AiModelConfig config = aiModelConfigService.getByCode(modelCode);
            if (config == null) {
                log.warn("【评测评分】未找到模型配置：{}", modelCode);
                return result;
            }

            String apiKey = environment.getProperty(config.getApiKeyAlias(), "");
            String prompt = String.format(MENTAL_ALIGN_PROMPT, userMessage, aiMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "你是一个专业的心理治疗质量评估专家。"));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            var response = restTemplate.postForEntity(config.getApiUrl(), entity, String.class);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = extractJson(content);

            com.fasterxml.jackson.databind.JsonNode scoreNode = mapper.readTree(content);
            double css = scoreNode.path("css").asDouble(0.5);
            double ars = scoreNode.path("ars").asDouble(0.5);

            result.put("css", Math.max(0.0, Math.min(1.0, css)));
            result.put("ars", Math.max(0.0, Math.min(1.0, ars)));

        } catch (RestClientException e) {
            log.error("【评测评分】CSS/ARS API调用失败：{}", e.getMessage());
            throw e; // 让重试机制捕获
        } catch (Exception e) {
            log.error("【评测评分】CSS/ARS 计算失败：{}", e.getMessage());
        }

        return result;
    }

    /**
     * 基于AI回复内容推断HEART的E/A/R三个维度
     * E - Engagement: 回复是否有深度、能引发继续互动的意愿
     * A - Adoption: 回复是否提供可操作的建议、资源、清晰指导
     * R - Retention: 回复是否建立信任、展现共情、提供被关怀感
     */
    private Map<String, Double> calculateHeartEARFromAI(String userMessage, String aiMessage, String modelCode) {
        Map<String, Double> result = new HashMap<>();
        result.put("E", 0.5);
        result.put("A", 0.5);
        result.put("R", 0.5);

        try {
            AiModelConfig config = aiModelConfigService.getByCode(modelCode);
            if (config == null) {
                log.warn("【HEART-EAR评分】未找到模型配置：{}", modelCode);
                return result;
            }

            String apiKey = environment.getProperty(config.getApiKeyAlias(), "");
            String prompt = String.format(HEART_EAR_PROMPT, userMessage, aiMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system",
                    "content", "你是一个心理支持对话质量评估专家，评分应基于回复内容的专业性和用户价值。"));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            var response = restTemplate.postForEntity(config.getApiUrl(), entity, String.class);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = extractJson(content);

            com.fasterxml.jackson.databind.JsonNode scoreNode = mapper.readTree(content);
            double e = scoreNode.path("engagement").asDouble(0.5);
            double a = scoreNode.path("adoption").asDouble(0.5);
            double r = scoreNode.path("retention").asDouble(0.5);

            result.put("E", Math.max(0.0, Math.min(1.0, e)));
            result.put("A", Math.max(0.0, Math.min(1.0, a)));
            result.put("R", Math.max(0.0, Math.min(1.0, r)));

        } catch (RestClientException e) {
            log.error("【HEART-EAR评分】API调用失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("【HEART-EAR评分】E/A/R计算失败：{}", e.getMessage());
        }

        return result;
    }

    /**
     * 基于AI回复内容推断HEART的H维度
     * H - Happiness: 回复对用户情绪的改善效果
     */
    private Map<String, Double> calculateHeartHFromAI(String userMessage, String aiMessage, String modelCode) {
        Map<String, Double> result = new HashMap<>();
        result.put("H", 0.5);

        try {
            AiModelConfig config = aiModelConfigService.getByCode(modelCode);
            if (config == null) {
                log.warn("【HEART-H评分】未找到模型配置：{}", modelCode);
                return result;
            }

            String apiKey = environment.getProperty(config.getApiKeyAlias(), "");
            String prompt = String.format(HEART_H_PROMPT, userMessage, aiMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system",
                    "content", "你是一个心理支持效果评估专家，评分应基于回复对用户情绪的改善程度。"));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            var response = restTemplate.postForEntity(config.getApiUrl(), entity, String.class);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = extractJson(content);

            com.fasterxml.jackson.databind.JsonNode scoreNode = mapper.readTree(content);
            double h = scoreNode.path("valence").asDouble(0.5);

            result.put("H", Math.max(0.0, Math.min(1.0, h)));

        } catch (RestClientException e) {
            log.error("【HEART-H评分】API调用失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("【HEART-H评分】H计算失败：{}", e.getMessage());
        }

            return result;
    }

    /**
     * 从模型返回内容中智能提取 JSON 字符串
     * 支持模型在 JSON 前后添加自然语言说明、混用中文引号等情况
     */
    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            return "{}";
        }
        content = content.trim();

        // 统一替换中文/全角引号为标准 ASCII 引号（常见于模型输出）
        content = content.replace('\u201C', '"').replace('\u201D', '"')
                         .replace('\u2018', '\'').replace('\u2019', '\'');

        // 情况1：内容本身是合法 JSON，直接返回
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(content);
            return content;
        } catch (Exception ignored) {}

        // 情况2：寻找第一个 { ... } JSON 对象块
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            String jsonCandidate = content.substring(firstBrace, lastBrace + 1);
            try {
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonCandidate);
                return jsonCandidate;
            } catch (Exception ignored) {}
        }

        // 情况3：寻找第一个 [ ... ] JSON 数组块（兜底）
        int firstBracket = content.indexOf('[');
        int lastBracket = content.lastIndexOf(']');
        if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            String jsonCandidate = content.substring(firstBracket, lastBracket + 1);
            try {
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonCandidate);
                return jsonCandidate;
            } catch (Exception ignored) {}
        }

        log.warn("【JSON提取】无法从内容中提取JSON，返回空对象，内容前100字：{}",
                content.substring(0, Math.min(100, content.length())));
        return "{}";
    }
}
