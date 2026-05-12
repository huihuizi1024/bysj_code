package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.IntentReconstructService;
import com.example.ai_app_java.service.RoleSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/intent")
public class IntentController {

    @Autowired
    private IntentReconstructService intentService;

    @Autowired
    private RoleSchedulerService roleService;

    /**
     * 重构用户输入的临床意图
     * POST /intent/reconstruct
     * Body: { userInput, emotionType?, emotionScore? }
     */
    @PostMapping("/reconstruct")
    public Result reconstruct(@RequestBody Map<String, Object> body) {
        String userInput = (String) body.get("userInput");
        String emotionType = (String) body.get("emotionType");
        Double emotionScore = body.get("emotionScore") != null
            ? ((Number) body.get("emotionScore")).doubleValue() : null;

        if (userInput == null || userInput.isBlank()) {
            return Result.fail(400, "用户输入不能为空");
        }

        var result = intentService.reconstruct(userInput, emotionType, emotionScore);
        return Result.success("意图重构成功", Map.of(
            "latentNeed", result.latentNeed(),
            "clinicalIntent", result.clinicalIntent(),
            "therapyModule", result.therapyModule(),
            "confidence", result.confidence()
        ));
    }

    /**
     * 获取所有支持的意图分类
     * GET /intent/list
     */
    @GetMapping("/list")
    public Result listIntents() {
        List<IntentReconstructService.IntentInfo> intents = intentService.getAllIntents();
        return Result.success("查询成功", intents);
    }

    /**
     * 获取所有治疗角色
     * GET /intent/roles
     */
    @GetMapping("/roles")
    public Result listRoles() {
        List<RoleSchedulerService.RoleInfo> roles = roleService.getAllRoles();
        return Result.success("查询成功", roles);
    }

    /**
     * 确定当前推荐角色
     * POST /intent/determineRole
     * Body: { userId?, sessionId?, intent?, emotionType?, emotionScore?, prsScore? }
     */
    @PostMapping("/determineRole")
    public Result determineRole(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") != null
            ? ((Number) body.get("userId")).longValue() : null;
        Long sessionId = body.get("sessionId") != null
            ? ((Number) body.get("sessionId")).longValue() : null;
        String intent = (String) body.get("intent");
        String emotionType = (String) body.get("emotionType");
        Double emotionScore = body.get("emotionScore") != null
            ? ((Number) body.get("emotionScore")).doubleValue() : null;
        Double prsScore = body.get("prsScore") != null
            ? ((Number) body.get("prsScore")).doubleValue() : null;

        String role = roleService.determineRole(userId, sessionId, intent,
            emotionType, emotionScore, prsScore);
        String fragment = roleService.getRolePromptFragment(role);

        return Result.success("角色确定成功", Map.of(
            "recommendedRole", role,
            "roleFragment", fragment
        ));
    }
}
