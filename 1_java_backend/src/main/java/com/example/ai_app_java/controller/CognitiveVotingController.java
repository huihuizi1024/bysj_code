package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.CognitiveVoting;
import com.example.ai_app_java.service.CognitiveVotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/voting")
public class CognitiveVotingController {

    @Autowired
    private CognitiveVotingService votingService;

    /**
     * 提交认知投票
     * POST /voting
     * Body: { votingType, question, selectedOption, sessionId? }
     */
    @PostMapping
    public Result submitVote(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody Map<String, Object> body) {
        String votingType = (String) body.get("votingType");
        String question = (String) body.get("question");
        String selectedOption = (String) body.get("selectedOption");
        Long sessionId = body.get("sessionId") != null
            ? ((Number) body.get("sessionId")).longValue() : null;

        if (votingType == null || selectedOption == null) {
            return Result.fail(400, "投票类型和选项不能为空");
        }

        CognitiveVoting record = votingService.submitVote(
            userId, votingType, question, selectedOption, sessionId);
        return Result.success("投票成功", record);
    }

    /**
     * 获取投票历史
     * GET /voting/history?limit=20
     */
    @GetMapping("/history")
    public Result getHistory(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<CognitiveVoting> records = votingService.getRecentVotings(userId, limit);
        return Result.success("查询成功", records);
    }

    /**
     * 根据会话获取投票记录
     * GET /voting/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public Result getBySession(
            @PathVariable Long sessionId,
            @RequestAttribute("currentUserId") Long userId) {
        List<CognitiveVoting> records = votingService.getVotingsBySession(sessionId);
        return Result.success("查询成功", records);
    }

    /**
     * 获取当前应展示的投票问题
     * GET /voting/next?emotionType=depression&recentType=
     */
    @GetMapping("/next")
    public Result getNextQuestion(
            @RequestParam String emotionType,
            @RequestParam(value = "recentType", required = false) String recentType) {
        var question = votingService.getNextQuestion(emotionType, recentType);
        return Result.success("获取成功", Map.of(
            "type", question.type(),
            "question", question.question(),
            "options", question.options()
        ));
    }

    /**
     * 检查当前情绪是否应触发投票
     * GET /voting/shouldTrigger?emotionType=depression&emotionScore=0.35
     */
    @GetMapping("/shouldTrigger")
    public Result shouldTrigger(
            @RequestParam String emotionType,
            @RequestParam double emotionScore) {
        boolean should = votingService.shouldTriggerVoting(emotionType, emotionScore);
        return Result.success("查询成功", Map.of("shouldTrigger", should));
    }
}
