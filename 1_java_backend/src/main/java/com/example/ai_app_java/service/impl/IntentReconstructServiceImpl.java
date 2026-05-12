package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.IntentClassification;
import com.example.ai_app_java.mapper.IntentClassificationMapper;
import com.example.ai_app_java.service.AiService;
import com.example.ai_app_java.service.IntentReconstructService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IntentReconstructServiceImpl implements IntentReconstructService {

    @Autowired
    @Lazy
    private AiService aiService;

    @Autowired
    private IntentClassificationMapper intentMapper;

    /** 默认模型 */
    @Value("${ai.default.model:DEEPSEEK}")
    private String defaultModel;

    /** 预定义的 11 个临床意图分类（数据库未初始化时的降级） */
    private static final Map<String, IntentInfo> DEFAULT_INTENTS = Map.ofEntries(
        Map.entry("existential_crisis",       new IntentInfo("existential_crisis",       "存在主义危机",        "对生命意义和价值产生质疑",            "ACT,CBT",   "socratic")),
        Map.entry("value_clarification",      new IntentInfo("value_clarification",      "价值澄清",            "探索个人核心价值观和生活方向",       "ACT",       "socratic")),
        Map.entry("cognitive_restructuring",   new IntentInfo("cognitive_restructuring",  "认知重构",            "识别和挑战负面自动思维",             "CBT",       "socratic")),
        Map.entry("behavioral_activation",    new IntentInfo("behavioral_activation",   "行为激活",            "增加积极行为以改善情绪",             "CBT",       "supportive")),
        Map.entry("emotion_regulation",       new IntentInfo("emotion_regulation",      "情绪调节",            "学习管理强烈情绪的技巧",           "DBT",       "supportive")),
        Map.entry("distress_tolerance",       new IntentInfo("distress_tolerance",       "痛苦耐受",            "在困难情境中保持冷静的能力",       "DBT",       "supportive")),
        Map.entry("social_skill",             new IntentInfo("social_skill",            "社交技能训练",         "改善人际交往和沟通能力",           "DBT",       "guided")),
        Map.entry("grief_processing",         new IntentInfo("grief_processing",       "悲伤处理",            "处理丧失和失落情绪",               "CBT",       "empathetic")),
        Map.entry("sleep_hygiene",           new IntentInfo("sleep_hygiene",          "睡眠卫生",            "改善睡眠质量和作息规律",           "CBT",       "guided")),
        Map.entry("self_compassion",          new IntentInfo("self_compassion",        "自我慈悲",            "培养对自己的善意和接纳",           "ACT",       "empathetic")),
        Map.entry("crisis_stabilization",    new IntentInfo("crisis_stabilization",   "危机稳定化",          "在危机时刻保持安全和控制感",       "DBT",       "crisis_mode"))
    );

    @Override
    public IntentResult reconstruct(String userInput, String emotionType, Double emotionScore) {
        String prompt = buildIntentPrompt(userInput, emotionType, emotionScore);
        try {
            String aiResponse = aiService.getAiResponse(null, prompt, defaultModel);
            return parseIntentResult(aiResponse);
        } catch (Exception e) {
            System.out.println("【意图重构】AI调用失败，使用降级策略：" + e.getMessage());
            return fallbackIntent(userInput, emotionType, emotionScore);
        }
    }

    @Override
    public List<IntentInfo> getAllIntents() {
        List<IntentClassification> dbIntents = intentMapper.selectList(null);
        if (dbIntents != null && !dbIntents.isEmpty()) {
            return dbIntents.stream().map(i ->
                new IntentInfo(i.getCode(), i.getName(), i.getDescription(),
                    i.getTherapyDimensions(), i.getAiRole())
            ).toList();
        }
        return new ArrayList<>(DEFAULT_INTENTS.values());
    }

    @Override
    public String getTherapyDimensions(String intentCode) {
        IntentInfo info = DEFAULT_INTENTS.get(intentCode);
        return info != null ? info.therapyDimensions() : "CBT";
    }

    private String buildIntentPrompt(String userInput, String emotionType, Double emotionScore) {
        return """
            你是一个专业的临床意图分类助手。请将用户的非结构化输入重构为临床可操作的意图分类。

            请从以下11个意图类别中选择最匹配的一个：
            1. existential_crisis（存在主义危机）：对生命意义和价值产生质疑
            2. value_clarification（价值澄清）：探索个人核心价值观
            3. cognitive_restructuring（认知重构）：识别和挑战负面自动思维
            4. behavioral_activation（行为激活）：增加积极行为
            5. emotion_regulation（情绪调节）：管理强烈情绪
            6. distress_tolerance（痛苦耐受）：在困难中保持冷静
            7. social_skill（社交技能）：改善人际交往
            8. grief_processing（悲伤处理）：处理丧失情绪
            9. sleep_hygiene（睡眠卫生）：改善睡眠
            10. self_compassion（自我慈悲）：对自己友善接纳
            11. crisis_stabilization（危机稳定化）：在危机中保持安全

            用户输入：%s
            情绪类型：%s
            情绪得分：%.2f

            请严格按以下JSON格式返回（不要有任何额外内容）：
            {
              "latent_need": "用一句话描述用户的隐性需求",
              "clinical_intent": "上述11个意图代码之一",
              "therapy_module": "最匹配的疗法模块代码，如ACT_value_clarification",
              "confidence": 0.0到1.0之间的置信度
            }
            """.formatted(userInput, emotionType != null ? emotionType : "unknown",
                          emotionScore != null ? emotionScore : 0.5);
    }

    private IntentResult parseIntentResult(String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(aiResponse);
            String intent = json.path("clinical_intent").asText("cognitive_restructuring");
            String latentNeed = json.path("latent_need").asText("寻求情绪支持");
            String therapyModule = json.path("therapy_module").asText(intent);
            double confidence = json.path("confidence").asDouble(0.7);

            // 验证 intent 合法性
            if (!DEFAULT_INTENTS.containsKey(intent)) {
                intent = "cognitive_restructuring";
            }
            return new IntentResult(latentNeed, intent, therapyModule, confidence);
        } catch (Exception e) {
            System.out.println("【意图重构】解析失败，降级到默认值：" + e.getMessage());
            return new IntentResult("寻求情绪支持", "cognitive_restructuring",
                "CBT_cognitive_restructuring", 0.5);
        }
    }

    private IntentResult fallbackIntent(String userInput, String emotionType, Double emotionScore) {
        // 基于情绪类型的降级策略
        if (emotionScore != null && emotionScore < 0.4) {
            if ("depression".equals(emotionType)) {
                return new IntentResult("感到无意义和低落", "existential_crisis",
                    "ACT_value_clarification", 0.6);
            } else if ("anxiety".equals(emotionType)) {
                return new IntentResult("感到紧张和不安", "emotion_regulation",
                    "DBT_emotion_regulation", 0.6);
            }
        }
        return new IntentResult("寻求情绪支持", "cognitive_restructuring",
            "CBT_cognitive_restructuring", 0.5);
    }
}
