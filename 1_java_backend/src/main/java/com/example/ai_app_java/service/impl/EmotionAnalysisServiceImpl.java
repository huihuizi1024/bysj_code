package com.example.ai_app_java.service.impl;

import com.example.ai_app_java.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.EmotionRecord;
import com.example.ai_app_java.mapper.EmotionRecordMapper;
import com.example.ai_app_java.service.EmotionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;


@Service//标记为Spring管理的Bean，可以被其他类@Autowired注入
public class EmotionAnalysisServiceImpl implements EmotionAnalysisService {

    //注入EmotionRecordMapper，用于保存情绪记录
    @Autowired
    private EmotionRecordMapper emotionRecordMapper;

    //注入AiService，用于调用AI进行情绪分析
    @Autowired
    @Lazy
    private AiService aiService;

    //分析用户发送的消息，返回情绪记录
    @Override
    public EmotionRecord analyzeEmotion(Long userId, Long sessionId, Long messageId, String message,String modelCode) {
        //1、创建情绪记录对象
        EmotionRecord record = new EmotionRecord();
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setMessageId(messageId);//保存关联的消息ID
        record.setAnalysisTime(LocalDateTime.now());
        //2、调用AI进行情绪分析
        try{
            //调用AiService的analyzeEmotion方法获取AI分析结果
            String aiResult = aiService.analyzeEmotion(message,modelCode);

            //3、解析AI返回的JSON结果
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = null;
            String emotionType = "neutral";
            double emotionScore = 0.5;
            double valence = 0.0;
            double arousal = 0.5;
            String keywords = "无";

            try {
                json = mapper.readTree(aiResult);
                emotionType = json.path("emotionType").asText("neutral");
                emotionScore = json.path("emotionScore").asDouble(0.5);
                valence = json.path("valence").asDouble(0.0);
                arousal = json.path("arousal").asDouble(0.5);
                keywords = json.path("keywords").asText("无");
            } catch (Exception parseEx) {
                // AI可能返回了纯文本（如 "positive" 或 "depression"）而非JSON对象
                // 此时将纯文本作为情绪类型处理，并做合理推断
                String trimmed = aiResult.trim();
                if (!trimmed.isEmpty()) {
                    // 映射常见文本到标准类型
                    emotionType = mapToStandardEmotionType(trimmed);
                    // 根据情绪类型推断得分
                    emotionScore = inferScoreFromType(emotionType);
                    valence = inferValenceFromType(emotionType);
                    arousal = inferArousalFromType(emotionType);
                    keywords = trimmed;
                }
                System.out.println("【情绪分析】AI返回纯文本，已转换：type=" + emotionType + ", score=" + emotionScore);
            }

            record.setEmotionType(emotionType);
            record.setEmotionScore(emotionScore);
            record.setValence(valence);
            record.setArousal(arousal);
            record.setKeywords(keywords);
        }catch(Exception e){
            System.out.println("【情绪分析】AI分析失败，使用默认值："+e.getMessage());
            //兜底：使用中性情绪
            record.setEmotionType("neutral");
            record.setEmotionScore(0.5);
            record.setValence(0.0);
            record.setArousal(0.5);
            record.setKeywords("无");
        }
        //4、保存情绪记录
        emotionRecordMapper.insert(record);
        return record;
    }

    // =====================================================
    // 辅助方法：AI返回纯文本时转换为标准情绪类型
    // =====================================================

    /**
     * 将 AI 返回的任意文本映射为标准情绪类型
     */
    private String mapToStandardEmotionType(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("positive") || lower.contains("积极") || lower.contains("开心") || lower.contains("高兴") || lower.contains("快乐"))
            return "positive";
        if (lower.contains("negative") || lower.contains("消极") || lower.contains("负面") || lower.contains("难过"))
            return "negative";
        if (lower.contains("anxiety") || lower.contains("焦虑") || lower.contains("紧张") || lower.contains("担心") || lower.contains("害怕"))
            return "anxiety";
        if (lower.contains("depression") || lower.contains("抑郁") || lower.contains("低落") || lower.contains("绝望") || lower.contains("伤心"))
            return "depression";
        if (lower.contains("anger") || lower.contains("愤怒") || lower.contains("生气") || lower.contains("恼火"))
            return "anger";
        if (lower.contains("sad") || lower.contains("悲伤") || lower.contains("沮丧"))
            return "sad";
        // 兜底：全部转为小写后精确匹配
        for (String t : new String[]{"positive","negative","neutral","anxiety","depression","anger","sad"}) {
            if (lower.equals(t)) return t;
        }
        return "neutral";
    }

    /** 根据情绪类型推断情绪得分（0~1，值越大越积极） */
    private double inferScoreFromType(String type) {
        return switch (type) {
            case "positive"  -> 0.75;
            case "negative"  -> 0.30;
            case "anxiety"   -> 0.35;
            case "depression"-> 0.25;
            case "anger"     -> 0.30;
            case "sad"       -> 0.35;
            default          -> 0.50;
        };
    }

    /** 根据情绪类型推断情感价态（-1~1，负为消极，正为积极） */
    private double inferValenceFromType(String type) {
        return switch (type) {
            case "positive"  ->  0.75;
            case "negative"  -> -0.65;
            case "anxiety"   -> -0.30;
            case "depression"-> -0.70;
            case "anger"     -> -0.50;
            case "sad"       -> -0.55;
            default          ->  0.00;
        };
    }

    /** 根据情绪类型推断唤醒度（0~1，值越大越激动） */
    private double inferArousalFromType(String type) {
        return switch (type) {
            case "positive"  -> 0.55;
            case "negative"  -> 0.45;
            case "anxiety"   -> 0.80;
            case "depression"-> 0.25;
            case "anger"     -> 0.85;
            case "sad"       -> 0.35;
            default          -> 0.50;
        };
    }

    //=================================================
    // 2. 获取某个会话的情绪分析报告
    //============================================
    @Override
    public List<EmotionRecord> getSessionEmotions(Long sessionId) {
        QueryWrapper<EmotionRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id",sessionId)
               .orderByDesc("analysis_time");//按时间倒序排列，新的在上面，旧的在下面
        return emotionRecordMapper.selectList(wrapper);
    }
    //=================================================
    // 3. 获取用户的情绪变化趋势
    //============================================
    @Override
    public List<EmotionRecord> getUserEmotionTrend(Long userId, int days) {
        QueryWrapper<EmotionRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.between("analysis_time",LocalDateTime.now().minusDays(days),LocalDateTime.now());
        return emotionRecordMapper.selectList(wrapper);
    }
}