package com.example.ai_app_java.service.impl;

import com.example.ai_app_java.entity.GuardianResult;
import com.example.ai_app_java.mapper.CrisisAlertMapper;
import com.example.ai_app_java.service.GuardianService;
import com.example.ai_app_java.service.VectorSimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class GuardianServiceImpl implements GuardianService {

    @Autowired
    private VectorSimilarityService vectorSimilarityService;

    @Autowired
    private CrisisAlertMapper crisisAlertMapper;

    /** 语义相似度阈值 */
    private static final double SEMANTIC_THRESHOLD = 0.55;

    // ================================================
    // PHQ-9 第9项：自伤意念语义检测关键词组合
    // ================================================
    private static final String[] PHQ9_KEYWORDS = {
        "想死", "不想活", "活不下去", "死了算了", "zs", "si",
        "轻生", "一了百了", "不如死了", "想自杀", "想离开这个世界",
        "活着没意义", "活着没意思",
        "从楼上跳", "跳下去", "跳楼"
    };

    // ================================================
    // 高危情境词（用于关系脉络门控）
    // ================================================
    private static final String[] LOSS_CONTEXT_KEYWORDS = {
        "去世", "逝世", "死了", "离开了我", "失去了", "失去", "分手", "离婚",
        "失业", "被辞退", "破产", "出事", "走了", "没了"
    };

    // ================================================
    // 高危关键词：自杀/自残相关
    // ================================================
    private static final String[] HIGH_RISK_KEYWORDS = {
        "想死", "不想活了", "活不下去", "想自杀", "自杀", "轻生",
        "死了算了", "活着没意义", "活着没意思", "一了百了",
        "割腕", "跳楼", "服毒", "上吊", "zs", "si"
    };

    // ================================================
    // 中危关键词：自残/暴力倾向
    // ================================================
    private static final String[] MEDIUM_RISK_KEYWORDS = {
        "自残", "割自己", "想割", "伤害自己", "暴力", "杀人",
        "报复社会", "绝望", "崩溃", "撑不住了"
    };

    // ================================================
    // 低危关键词：消极情绪
    // ================================================
    private static final String[] LOW_RISK_KEYWORDS = {
        "好累", "撑不下去", "坚持不住了", "完蛋了",
        "没希望了", "没人能帮我", "没人理解我"
    };

    @Override
    public GuardianResult check(String userInput, Long userId, Long sessionId, Long messageId) {
        if (userInput == null || userInput.isBlank()) {
            return GuardianResult.noCrisis();
        }

        String normalized = userInput.trim();

        // Step 1: PHQ-9 第9项语义检测（最高优先级）
        GuardianResult phq9Result = checkPHQ9(normalized);
        if (phq9Result != null && phq9Result.isCrisis()) {
            return phq9Result;
        }

        // Step 2: 语义相似度检测（BGE 向量匹配）
        GuardianResult semanticResult = checkSemantic(normalized);
        if (semanticResult != null && semanticResult.isCrisis()) {
            return semanticResult;
        }

        // Step 3: 关系脉络门控
        GuardianResult relationResult = checkRelationContext(normalized);
        if (relationResult != null && relationResult.isCrisis()) {
            return relationResult;
        }

        // Step 4: 关键词硬匹配（原有三级）
        return checkKeyword(normalized);
    }

    @Override
    public List<GuardianResult> batchCheck(List<String> inputs) {
        if (inputs == null) return Collections.emptyList();
        List<GuardianResult> results = new ArrayList<>();
        for (String input : inputs) {
            results.add(check(input, null, null, null));
        }
        return results;
    }

    // ================================================
    // Step 1: PHQ-9 第9项语义检测
    // ================================================
    private GuardianResult checkPHQ9(String text) {
        for (String kw : PHQ9_KEYWORDS) {
            if (text.contains(kw)) {
                GuardianResult result = new GuardianResult();
                result.setCrisis(true);
                result.setRiskLevel("high");
                result.setTriggerType("PHQ9");
                result.setMatchedKeywords(kw);
                return result;
            }
        }
        return null;
    }

    // ================================================
    // Step 2: 语义相似度检测
    // ================================================
    private GuardianResult checkSemantic(String text) {
        try {
            List<Long> matches = vectorSimilarityService.findMatchingSamples(text, SEMANTIC_THRESHOLD);
            if (!matches.isEmpty()) {
                double maxSim = vectorSimilarityService.getMaxSimilarity(text, SEMANTIC_THRESHOLD);
                GuardianResult result = new GuardianResult();
                result.setCrisis(true);
                result.setRiskLevel("high");
                result.setTriggerType("SEMANTIC");
                result.setMatchedSampleId(matches.get(0));
                result.setSimilarityScore(maxSim);
                result.setMatchedKeywords("语义相似度匹配");
                return result;
            }
        } catch (Exception e) {
            System.out.println("【Guardian】语义检测失败（向量模型可能不可用）：" + e.getMessage());
        }
        return null;
    }

    // ================================================
    // Step 3: 关系脉络门控
    // ================================================
    private GuardianResult checkRelationContext(String text) {
        // 检测人称代词 + 高危情境词组合
        // 例如："我失去了..." "我和他分手了" "我妈妈去世了"
        boolean hasFirstPerson = hasFirstPersonPronoun(text);
        for (String contextKw : LOSS_CONTEXT_KEYWORDS) {
            if (text.contains(contextKw) && hasFirstPerson) {
                GuardianResult result = new GuardianResult();
                result.setCrisis(true);
                result.setRiskLevel("medium");
                result.setTriggerType("RELATION");
                result.setMatchedKeywords("第一人称+" + contextKw);
                return result;
            }
        }
        return null;
    }

    private boolean hasFirstPersonPronoun(String text) {
        String[] firstPerson = {"我", "我自己", "我自己", "我感觉", "我认为", "我的"};
        for (String pronoun : firstPerson) {
            if (text.contains(pronoun)) {
                return true;
            }
        }
        return false;
    }

    // ================================================
    // Step 4: 关键词硬匹配
    // ================================================
    private GuardianResult checkKeyword(String text) {
        String found = checkKeywords(text, HIGH_RISK_KEYWORDS);
        if (found != null) {
            return buildResult(true, "high", "KEYWORD", found);
        }
        found = checkKeywords(text, MEDIUM_RISK_KEYWORDS);
        if (found != null) {
            return buildResult(true, "medium", "KEYWORD", found);
        }
        found = checkKeywords(text, LOW_RISK_KEYWORDS);
        if (found != null) {
            return buildResult(true, "low", "KEYWORD", found);
        }
        return GuardianResult.noCrisis();
    }

    private String checkKeywords(String content, String[] keywords) {
        StringBuilder found = new StringBuilder();
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                if (found.length() > 0) found.append(",");
                found.append(keyword);
            }
        }
        return found.length() > 0 ? found.toString() : null;
    }

    private GuardianResult buildResult(boolean crisis, String riskLevel,
                                      String triggerType, String keywords) {
        GuardianResult result = new GuardianResult();
        result.setCrisis(crisis);
        result.setRiskLevel(riskLevel);
        result.setTriggerType(triggerType);
        result.setMatchedKeywords(keywords);
        return result;
    }
}
