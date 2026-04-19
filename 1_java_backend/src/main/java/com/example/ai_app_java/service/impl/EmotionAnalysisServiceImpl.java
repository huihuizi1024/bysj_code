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
            JsonNode json = mapper.readTree(aiResult);

            String emotionType = json.path("emotionType").asText("neutral");
            double emotionScore = json.path("emotionScore").asDouble(0.5);
            String keywords = json.path("keywords").asText("无");

            record.setEmotionType(emotionType);
            record.setEmotionScore(emotionScore);
            record.setKeywords(keywords);
        }catch(Exception e){
            System.out.println("【情绪分析】AI分析失败，使用默认值："+e.getMessage());
            //兜底：使用中性情绪
            record.setEmotionType("neutral");
            record.setEmotionScore(0.5);
            record.setKeywords("无");
        }
        //4、保存情绪记录
        emotionRecordMapper.insert(record);
        return record;
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