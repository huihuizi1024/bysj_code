package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.ChatSession;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.EmotionRecord;
import com.example.ai_app_java.service.ChatSessionService;
import com.example.ai_app_java.service.EmotionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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