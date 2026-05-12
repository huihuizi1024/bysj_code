package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.EmotionRecord;
import com.example.ai_app_java.service.ChatSessionService;
import com.example.ai_app_java.service.EmotionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//情绪分析控制器
@RestController
@RequestMapping("/emotion")//所有 /emotion开头的请求都归这里管
public class EmotionController {

    //注入EmotionAnalysisService，用于获取情绪历史
    @Autowired
    private EmotionAnalysisService emotionAnalysisService;
    @Autowired
    private ChatSessionService chatSessionService;
    /**
     * 获取某个会话的情绪分析记录（完整历史）
     * 路由:GET http://localhost:8080/emotion/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public Result getEmotionHistory(
        @PathVariable Long sessionId,
        @RequestAttribute("currentUserId") Long userId) {
        //先校验权限，再查数据库
        Result check = verifyAccess(sessionId,userId);
        if(check != null) return check;
        //再查数据库
        List<EmotionRecord> list = emotionAnalysisService.getSessionEmotions(sessionId);
        return Result.success("获取情绪历史成功",list);
    }

    /**
     * 获取用户的情绪变化趋势
     * 路由:GET http://localhost:8080/emotion/trend?days=7
     */
    @GetMapping("/trend")
    public Result getEmotionTrend(
        @RequestAttribute("currentUserId") Long userId,
        @RequestParam(value = "days", defaultValue = "7") int days) {
        List<EmotionRecord> list = emotionAnalysisService.getUserEmotionTrend(userId, days);
        return Result.success("获取情绪变化趋势成功",list);
    }

    /**
     * 获取会话最新的情绪状态(最近一条情绪记录)
     * 路由:GET http://localhost:8080/emotion/latest/{sessionId}
     */
    @GetMapping("/latest/{sessionId}")
    public Result getLatestEmotion(
        @PathVariable Long sessionId,
        @RequestAttribute("currentUserId") Long userId) {
        //先校验权限，再查数据库
        Result check = verifyAccess(sessionId,userId);
        if(check != null) return check;
        //再查数据库
        List<EmotionRecord> list = emotionAnalysisService.getSessionEmotions(sessionId);
        if(list == null || list.isEmpty()) {
            return Result.success("会话暂无情绪记录",null);
        }
        return Result.success("获取会话最新的情绪状态成功",list.get(0));
    }

    /**
     * 对话摘要分析：提取本会话中的关键情绪主题和话题
     * 路由:GET http://localhost:8080/emotion/summary/{sessionId}
     */
    @GetMapping("/summary/{sessionId}")
    public Result getConversationSummary(
        @PathVariable Long sessionId,
        @RequestAttribute("currentUserId") Long userId) {
        Result check = verifyAccess(sessionId, userId);
        if(check != null) return check;

        List<EmotionRecord> records = emotionAnalysisService.getSessionEmotions(sessionId);
        if(records == null || records.isEmpty()) {
            return Result.success("暂无情绪数据", Map.of(
                "themes", List.of(),
                "keywords", List.of(),
                "emotionTrend", "stable"
            ));
        }

        // 统计各情绪类型出现次数
        Map<String, Long> typeCount = new HashMap<>();
        List<String> allKeywords = new ArrayList<>();
        for (EmotionRecord r : records) {
            String t = r.getEmotionType();
            typeCount.put(t, typeCount.getOrDefault(t, 0L) + 1);
            if (r.getKeywords() != null && !"无".equals(r.getKeywords())) {
                for (String kw : r.getKeywords().split("[,，、]")) {
                    kw = kw.trim();
                    if (!kw.isEmpty()) allKeywords.add(kw);
                }
            }
        }

        // 出现最多的情绪类型
        String dominantType = typeCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("neutral");

        // 情绪趋势：比较前半段和后半段的平均得分
        int mid = records.size() / 2;
        double earlyAvg = records.stream().skip(mid)
            .filter(r -> r.getEmotionScore() != null)
            .mapToDouble(EmotionRecord::getEmotionScore).average().orElse(0.5);
        double lateAvg = records.stream().limit(mid)
            .filter(r -> r.getEmotionScore() != null)
            .mapToDouble(EmotionRecord::getEmotionScore).average().orElse(0.5);

        String trend = lateAvg > earlyAvg + 0.05 ? "improving"
                    : lateAvg < earlyAvg - 0.05 ? "worsening" : "stable";

        // 高频关键词（取前5个）
        Map<String, Long> kwCount = allKeywords.stream()
            .collect(java.util.stream.Collectors.groupingBy(k -> k, java.util.stream.Collectors.counting()));
        List<String> topKeywords = kwCount.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(5).map(Map.Entry::getKey).toList();

        // 主题映射
        List<String> themes = inferThemes(dominantType, topKeywords);

        return Result.success("获取对话摘要成功", Map.of(
            "dominantEmotion", dominantType,
            "themes", themes,
            "keywords", topKeywords,
            "emotionTrend", trend,
            "totalRecords", records.size()
        ));
    }

    /** 根据情绪类型和高频关键词推断对话主题 */
    private List<String> inferThemes(String emotionType, List<String> keywords) {
        List<String> themes = new ArrayList<>();
        Set<String> kwSet = new java.util.HashSet<>(keywords);
        if (kwSet.stream().anyMatch(k -> k.contains("工作") || k.contains("职场") || k.contains("老板")))
            themes.add("职场压力");
        if (kwSet.stream().anyMatch(k -> k.contains("学习") || k.contains("考试") || k.contains("成绩")))
            themes.add("学业困扰");
        if (kwSet.stream().anyMatch(k -> k.contains("家庭") || k.contains("父母") || k.contains("亲子")))
            themes.add("家庭关系");
        if (kwSet.stream().anyMatch(k -> k.contains("恋爱") || k.contains("感情") || k.contains("分手")))
            themes.add("情感问题");
        if (kwSet.stream().anyMatch(k -> k.contains("失眠") || k.contains("睡眠") || k.contains("噩梦")))
            themes.add("睡眠问题");
        if (kwSet.stream().anyMatch(k -> k.contains("朋友") || k.contains("人际") || k.contains("孤独")))
            themes.add("人际关系");
        if (themes.isEmpty()) {
            themes.add(switch (emotionType) {
                case "anxiety" -> "情绪调适";
                case "depression" -> "情绪低落";
                case "positive" -> "积极探索";
                case "anger" -> "情绪管理";
                default -> "自我探索";
            });
        }
        return themes;
    }

    /**
     * 权限校验方法：用户只能访问自己的会话
     */
    private Result verifyAccess(Long sessionId, Long userId) {
        ChatSession session = chatSessionService.getById(sessionId);
        if(session == null ) {
            return Result.fail(404, "会话不存在");
        }
        if(!session.getUserId().equals(userId)) {
            return Result.fail(403, "无权访问此会话的情绪历史");
        }
        return null;//权限校验通过
    }
}