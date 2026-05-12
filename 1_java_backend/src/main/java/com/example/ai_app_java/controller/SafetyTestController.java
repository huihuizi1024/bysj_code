package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.GuardianService;
import com.example.ai_app_java.service.ReflectorService;
import com.example.ai_app_java.entity.GuardianResult;
import com.example.ai_app_java.entity.ReflectorResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 安全层测试专用 Controller
 * 用途：直接调用 Guardian 输入检测和 Reflector 输出审计，方便接口测试
 * 注意：生产环境可根据需要加权限控制或删除
 */
@RestController
@RequestMapping("/safety")
public class SafetyTestController {

    @Autowired
    private GuardianService guardianService;

    @Autowired
    private ReflectorService reflectorService;

    // ── Guardian 输入检测 ──────────────────────────────────────────────

    /**
     * 手动触发 Guardian 输入安全检测
     */
    @PostMapping("/guardian/check")
    public Result guardianCheck(@RequestBody Map<String, Object> body) {
        String userInput = (String) body.get("userInput");
        Long userId = body.get("userId") != null ? ((Number) body.get("userId")).longValue() : null;
        Long sessionId = body.get("sessionId") != null ? ((Number) body.get("sessionId")).longValue() : null;
        Long messageId = body.get("messageId") != null ? ((Number) body.get("messageId")).longValue() : null;
        GuardianResult result = guardianService.check(userInput, userId, sessionId, messageId);
        return Result.success("Guardian检测完成", result);
    }

    /**
     * 批量触发 Guardian 输入安全检测
     */
    @PostMapping("/guardian/batch")
    public Result batchCheckGuardian(@RequestBody Map<String, List<String>> body) {
        List<String> inputs = body.get("inputs");
        List<GuardianResult> results = guardianService.batchCheck(inputs);
        return Result.success("批量Guardian检测完成", results);
    }

    // ── Reflector 输出审计 ─────────────────────────────────────────────

    /**
     * 手动触发 Reflector 输出安全审计
     */
    @PostMapping("/reflector/audit")
    public Result auditReflector(@RequestBody Map<String, String> body) {
        String aiResponse = body.get("aiResponse");
        ReflectorResult result = reflectorService.audit(aiResponse);
        return Result.success("Reflector审计完成", result);
    }

    /**
     * 流式审计（用于 SSE 流式输出场景的内容校验）
     * 内容 < 20 字符时返回 null（跳过审计）
     */
    @PostMapping("/reflector/audit-stream")
    public Result auditStreamReflector(@RequestBody Map<String, String> body) {
        String partialContent = body.get("partialContent");
        String safeResponse = reflectorService.auditStream(partialContent);
        return Result.success("流式审计完成", safeResponse);
    }
}
