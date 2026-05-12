package com.example.ai_app_java.service.impl;

import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.ChatMessage;
import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.entity.EmotionRecord;
import com.example.ai_app_java.service.AiService;
import com.example.ai_app_java.service.ChatMessageService;
import com.example.ai_app_java.service.ChatSessionService;
import com.example.ai_app_java.service.EmotionAnalysisService;
import com.example.ai_app_java.service.CrisisDetectionService;
import com.example.ai_app_java.service.ResourceRepositoryService;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.UserModelPreferenceService;
import com.example.ai_app_java.service.PsychologicalReadinessService;
import com.example.ai_app_java.service.ReflectorService;
import com.example.ai_app_java.service.IntentReconstructService;
import com.example.ai_app_java.service.RoleSchedulerService;
import com.example.ai_app_java.service.TherapyEvaluationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private ChatSessionService chatSessionService;
    @Autowired
    private EmotionAnalysisService emotionAnalysisService;
    @Autowired
    private CrisisDetectionService crisisDetectionService;
    @Autowired
    private ResourceRepositoryService resourceRepositoryService;
    @Autowired
    private AiModelConfigService aiModelConfigService;
    @Autowired
    private UserModelPreferenceService userModelPreferenceService;
    @Autowired
    private PsychologicalReadinessService psychologicalReadinessService;
    @Autowired
    private ReflectorService reflectorService;
    @Autowired
    private IntentReconstructService intentReconstructService;
    @Autowired
    private RoleSchedulerService roleSchedulerService;
    @Autowired
    private TherapyEvaluationService therapyEvaluationService;
    @Autowired
    private org.springframework.core.env.Environment environment;

    /**
     * 系统提示词基础部分（不含资源上下文）
     */
    private static final String SYSTEM_PROMPT_BASE = """
        你是一位经过专业心理学训练、充满同理心的 AI 心理支持倾听者。

        【核心交互原则】
        1. 倾听与共情优先：不要急于给出建议。先复述用户的感受，表达完全的接纳和理解（如："我能感觉到你现在有多么辛苦"）。
        2. 启发式引导：在建立信任后，温和地运用认知行为疗法（CBT）的苏格拉底式提问，引导用户觉察自己的负面自动思维，探索其他视角。
        3. 身份边界：明确自己是 AI，绝对不虚构人类经历，不给出处方药建议，不替代真实的医疗诊断。

        【危机干预（最高优先级，凌驾于所有规则之上）】
        一旦用户的输入中包含"想死、活不下去、自杀、绝望、撑不住了、割腕、跳楼"等意图伤害自己的极端情绪表达，你必须立刻停止所有常规咨询逻辑，并严格且一字不差地输出以下格式的回复：

        [CRISIS_ALERT]
        我感受到了你现在承受着极大的痛苦，甚至觉得已经走投无路了。但请你一定要保护好自己，先停下来深呼吸。这世界还有人愿意倾听你、帮助你。
        请立刻拨打全国希望24小时心理危机干预热线：400-161-9995。那里有专业的老师，他们24小时都在，随时准备陪伴你度过这个难关。
        """;

    private static class ModelApiConfig {
        String url;
        String apiKey;
        String modelName;
        ModelApiConfig(String url, String apiKey, String modelName) {
            this.url = url;
            this.apiKey = apiKey;
            this.modelName = modelName;
        }
    }

    private ModelApiConfig getModelConfig(String modelCode) {
        AiModelConfig config = aiModelConfigService.getByCode(modelCode);
        if (config == null) {
            throw new RuntimeException("未找到模型配置: " + modelCode);
        }
        String apiKey = environment.getProperty(config.getApiKeyAlias(), "");
        return new ModelApiConfig(config.getApiUrl(), apiKey, config.getModelName());
    }

    @Override
    public String getAiResponse(Long userId, String content, String modelCode) {
        try {
            ModelApiConfig cfg = getModelConfig(modelCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cfg.apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", cfg.modelName);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system",
                "content", "你是一个温柔、专业、富有同理心的心理健康助手。请用简短温暖的中文回复用户，像知心朋友一样沟通。"));
            messages.add(Map.of("role", "user", "content", content));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(cfg.url, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String aiReply = root.path("choices").get(0).path("message").path("content").asText();
            return aiReply;
        } catch (Exception e) {
            return "抱歉我的大脑暂时开小差，请稍后再试。";
        }
    }

    @Override
    public SseEmitter streamChat(Long userId, Long sessionId, String content, String modelCode) {
        SseEmitter emitter = new SseEmitter(120000L);

        // 1. 保存用户消息到数据库
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setContent(content);
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setCreateTime(LocalDateTime.now());
        Long userMsgId = chatMessageService.saveAndGetId(userMsg);

        // 2. 同步执行情绪分析（必须等结果，用于构建资源上下文）
        EmotionRecord emotionRecord = null;
        String emotionType = "neutral";
        double emotionScore = 0.5;
        String emotionKeywords = "无";
        try {
            emotionRecord = emotionAnalysisService.analyzeEmotion(
                userId, sessionId, userMsgId, content, modelCode);
            if (emotionRecord != null) {
                emotionType = emotionRecord.getEmotionType() != null
                    ? emotionRecord.getEmotionType() : "neutral";
                emotionScore = emotionRecord.getEmotionScore() != null
                    ? emotionRecord.getEmotionScore() : 0.5;
                emotionKeywords = emotionRecord.getKeywords() != null
                    ? emotionRecord.getKeywords() : "无";
            }
        } catch (Exception e) {
            System.out.println("【AI服务】情绪分析失败，使用默认值：" + e.getMessage());
        }

        // 3. 异步检测危机（不阻塞 AI 回复流程）
        new Thread(() -> {
            try {
                crisisDetectionService.checkCrisis(userId, sessionId, userMsgId, content);
            } catch (Exception e) {
                System.out.println("【AI服务】危机检测失败：" + e.getMessage());
            }
        }).start();

        // 4. 计算心理准备度得分（PRS）并获取干预深度
        Double prsScore = null;
        String interventionDepth = "supportive";
        if (emotionRecord != null) {
            try {
                var prsResult = psychologicalReadinessService.calculatePRS(
                    emotionRecord, userId, sessionId, userMsgId);
                prsScore = prsResult.getTotalScore();
                interventionDepth = prsResult.getInterventionDepth() != null
                    ? prsResult.getInterventionDepth() : "supportive";
            } catch (Exception e) {
                System.out.println("【AI服务】PRS计算失败：" + e.getMessage());
            }
        }

        // 5. 意图重构（临床意图分类 + 疗法模块选择）
        String clinicalIntent = null;
        String therapyModule = null;
        String latentNeed = null;
        try {
            var intentResult = intentReconstructService.reconstruct(content, emotionType, emotionScore);
            clinicalIntent = intentResult.clinicalIntent();
            therapyModule = intentResult.therapyModule();
            latentNeed = intentResult.latentNeed();
        } catch (Exception e) {
            System.out.println("【AI服务】意图重构失败：" + e.getMessage());
        }

        // 6. 治疗角色调度（领导权动态平衡）
        String aiRole = null;
        String rolePromptFragment = null;
        try {
            aiRole = roleSchedulerService.determineRole(userId, sessionId,
                clinicalIntent, emotionType, emotionScore, prsScore);
            rolePromptFragment = roleSchedulerService.getRolePromptFragment(aiRole);
        } catch (Exception e) {
            System.out.println("【AI服务】角色调度失败：" + e.getMessage());
        }

        // 将需要在 lambda 中使用的变量保存为 final
        final String finalContent = content;
        final Long finalUserId = userId;
        final Long finalSessionId = sessionId;
        final Long finalUserMsgId = userMsgId;
        final String finalModelCode = modelCode;
        final String finalEmotionType = emotionType;
        final String finalClinicalIntent = clinicalIntent != null ? clinicalIntent : "";
        final String finalTherapyModule = therapyModule != null ? therapyModule : "";
        final String finalInterventionDepth = interventionDepth;
        final String finalAiRole = aiRole != null ? aiRole : "";

        // 7. 构建PRS上下文（干预深度配置）
        String prsContext = psychologicalReadinessService.buildPrsContext(prsScore, interventionDepth);

        // 8. 构建意图上下文
        String intentContext = buildIntentContext(clinicalIntent, therapyModule, latentNeed, aiRole, rolePromptFragment);

        // 9. 构建资源上下文（多维度匹配：情绪+得分+意图）
        String resourceContext = resourceRepositoryService.buildDynamicStrategy(
            emotionType, emotionScore, emotionKeywords, clinicalIntent, interventionDepth, aiRole);

        // 10. 构建完整的 system prompt（基础 + PRS上下文 + 意图上下文 + 资源上下文）
        String fullSystemPrompt = SYSTEM_PROMPT_BASE + prsContext + intentContext + resourceContext;

        // 8. 构建消息列表（系统提示词 + 历史上下文 + 当前消息）
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", fullSystemPrompt));

        // 加载历史消息（最多保留最近20条，防止token溢出）
        // 注意：getHistoryBySessionId 已包含当前用户消息，不要重复添加
        // 注意：getHistoryBySessionId 返回按时间倒序（最新的在前），需要反转保持 oldest→newest 顺序
        List<ChatMessage> historyMessages = chatMessageService.getHistoryBySessionId(sessionId);
        Collections.reverse(historyMessages); // 反转为 oldest→newest
        for (ChatMessage msg : historyMessages) {
            String role = "user".equals(msg.getRole()) ? "user" : "assistant";
            messages.add(Map.of("role", role, "content", msg.getContent()));
        }

        // 9. 新开线程执行 HTTP 流式请求
        List<String> responseChunks = Collections.synchronizedList(new ArrayList<>());
        new Thread(() -> {
            try {
                ModelApiConfig cfg = getModelConfig(modelCode);
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", cfg.modelName);
                requestBody.put("stream", true);
                requestBody.put("messages", messages);

                ObjectMapper mapper = new ObjectMapper();
                String jsonPayload = mapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(cfg.url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + cfg.apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpClient client = HttpClient.newHttpClient();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                        .thenAccept(response -> {
                            try {
                                response.body().forEach(line -> {
                                    try {
                                        if (line != null && line.startsWith("data: ")) {
                                            String data = line.substring(6).trim();
                                            if (data.equals("[DONE]")) {
                                                return;
                                            }
                                            JsonNode rootNode = mapper.readTree(data);
                                            JsonNode deltaNode = rootNode.path("choices").get(0).path("delta");
                                            if (deltaNode.has("content")) {
                                                String word = deltaNode.get("content").asText();
                                                emitter.send(SseEmitter.event().name("chunk").data(word));
                                                responseChunks.add(word);
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.out.println("解析流式碎片出错：" + e.getMessage());
                                    }
                                });

                                // 将所有分片 join 为完整字符串（线程安全）
                                String fullAiResponse = String.join("", responseChunks);

                                // 保存 AI 回复
                                ChatMessage aiMsg = new ChatMessage();
                                aiMsg.setSessionId(sessionId);
                                aiMsg.setUserId(userId);
                                aiMsg.setRole("assistant");
                                aiMsg.setContent(fullAiResponse);
                                aiMsg.setCreateTime(LocalDateTime.now());
                                chatMessageService.save(aiMsg);

                                // 异步生成会话标题
                                autoSummarizeTitle(sessionId, content);

                                // ---- Layer 2: Reflector 输出安全审计（不阻塞返回）----
                                new Thread(() -> {
                                    try {
                                        reflectorService.audit(fullAiResponse);
                                    } catch (Exception e) {
                                        System.out.println("【AI服务】Reflector审计失败：" + e.getMessage());
                                    }
                                }).start();

                                // 发送命名事件，告知前端流式响应正常结束
                                emitter.send(SseEmitter.event().name("done").data("ok"));
                                emitter.complete();

                                // ---- Layer 3: MentalAlign 疗效评估（不阻塞返回）----
                                new Thread(() -> {
                                    try {
                                        therapyEvaluationService.evaluateResponse(
                                            finalContent,
                                            fullAiResponse,
                                            finalUserId,
                                            finalSessionId,
                                            finalUserMsgId + 1,
                                            finalModelCode,
                                            finalClinicalIntent,
                                            finalTherapyModule,
                                            finalInterventionDepth,
                                            finalAiRole
                                        );
                                    } catch (Exception e) {
                                        System.out.println("【AI服务】MentalAlign疗效评估失败：" + e.getMessage());
                                    }
                                }).start();
                            } catch (Exception e) {
                                try {
                                    emitter.send(SseEmitter.event().name("done").data("error:" + e.getMessage()));
                                } catch (Exception ignored) {}
                                emitter.completeWithError(e);
                            }
                        })
                        .exceptionally(ex -> {
                            emitter.completeWithError(ex);
                            return null;
                        });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    /**
     * 构建意图上下文文本，注入到 AI system prompt 中
     * 包含临床意图分类、疗法模块、隐性需求、AI角色片段
     */
    private String buildIntentContext(String clinicalIntent, String therapyModule,
                                    String latentNeed, String aiRole, String rolePromptFragment) {
        if (clinicalIntent == null && aiRole == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【临床意图上下文】\n");
        if (latentNeed != null && !latentNeed.isBlank()) {
            sb.append("用户隐性需求：").append(latentNeed).append("\n");
        }
        if (clinicalIntent != null && !clinicalIntent.isBlank()) {
            sb.append("临床意图：").append(clinicalIntent).append("\n");
        }
        if (therapyModule != null && !therapyModule.isBlank()) {
            sb.append("疗法模块：").append(therapyModule).append("\n");
        }
        if (rolePromptFragment != null && !rolePromptFragment.isBlank()) {
            sb.append("\n").append(rolePromptFragment).append("\n");
        }
        return sb.toString();
    }

    private void autoSummarizeTitle(Long sessionId, String firstUserMessage) {
        new Thread(() -> {
            try {
                ChatSession session = chatSessionService.getById(sessionId);
                if (session == null || !"新的心理探索".equals(session.getTitle())) {
                    return;
                }
                String prompt = "请你根据用户输入的这段话，总结一个非常简短的对话标题（不超过8个字，不要带标点符号和引号）：\n" + firstUserMessage;
                String modelCode = userModelPreferenceService.getUserModelCode(session.getUserId());
                String generatedTitle = getAiResponse(session.getUserId(), prompt, modelCode);
                generatedTitle = generatedTitle.replace("\"", "").replace("'", "").trim();
                if (generatedTitle.length() > 15) {
                    generatedTitle = generatedTitle.substring(0, 15) + "...";
                }
                session.setTitle(generatedTitle);
                chatSessionService.updateById(session);
            } catch (Exception e) {
                System.out.println("【AI服务】自动总结标题失败：" + e.getMessage());
            }
        }).start();
    }

    @Override
    public String analyzeEmotion(String content, String modelCode) {
        try {
            ModelApiConfig cfg = getModelConfig(modelCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cfg.apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", cfg.modelName);
            List<Map<String, String>> messages = new ArrayList<>();
            String systemPrompt = """
                你是一个专业的情绪分析助手。请分析用户输入的文本，判断其情绪状态。
                你必须严格按照以下JSON格式返回，不要有任何额外内容：
                {
                    "emotionType": "情绪类型，取值为 positive/negative/neutral/anxiety/depression/anger 之一",
                    "emotionScore": 0.0到1.0之间的数值，0.0代表极度负面，1.0代表极度积极，
                    "valence": -1.0到1.0之间的数值，-1.0代表极度消极/不愉快，1.0代表极度积极/愉快，
                    "arousal": 0.0到1.0之间的数值，0.0代表极度平静/放松，1.0代表极度激动/紧张，
                    "keywords": "识别出的情绪关键词，用逗号分隔"
                }
                """;
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", content));
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(cfg.url, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "{\"emotionType\":\"neutral\",\"emotionScore\":0.5,\"keywords\":\"无\"}";
        }
    }
}
