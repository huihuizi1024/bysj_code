package com.example.ai_app_java.service;
import com.example.ai_app_java.entity.EmotionRecord;
import java.util.List;

//情绪分析服务接口

public interface EmotionAnalysisService {
    /*
    分析用户发送的消息，返回情绪记录
    @param userId 用户ID
    @param sessionId 会话ID
    @param message 用户发送的消息
    @return 情绪记录
    */
    //分析用户发送的消息，返回情绪记录
    EmotionRecord analyzeEmotion(Long userId, Long sessionId, Long messageId, String message,String modelCode);
    
    /*
    获取某个会话的情绪分析报告
    @param sessionId 会话ID
    @return 情绪记录列表
    */
    List<EmotionRecord> getSessionEmotions(Long sessionId);

    /**
     * 获取用户的情绪变化趋势
     * @param userId 用户ID
     * @param days 统计最近多少天
     * @return 情绪记录列表（按时间顺序）
     */
    List<EmotionRecord> getUserEmotionTrend(Long userId, int days);

}