package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.AiModelConfig;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.AiModelConfigService;
import com.example.ai_app_java.service.UserModelPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/model")
@CrossOrigin
public class ModelConfigController {

    @Autowired
    private AiModelConfigService aiModelConfigService;

    @Autowired
    private UserModelPreferenceService userModelPreferenceService;

    // 获取所有可选模型列表
    @GetMapping("/list")
    public Result listModels() {
        List<AiModelConfig> list = aiModelConfigService.listEnabled();
        return Result.success("获取模型列表成功", list);
    }

    // 获取当前用户的默认模型
    @GetMapping("/current")
    public Result getCurrentModel(@RequestAttribute("currentUserId") Long userId) {
        String modelCode = userModelPreferenceService.getUserModelCode(userId);
        AiModelConfig model = aiModelConfigService.getByCode(modelCode);
        return Result.success("获取当前模型成功", model);
    }

    // 切换模型
    @PostMapping("/select")
    public Result selectModel(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody java.util.Map<String, String> body) {
        String modelCode = body.get("modelCode");
        if (modelCode == null || modelCode.isBlank()) {
            return Result.fail(400, "模型代码不能为空");
        }
        userModelPreferenceService.saveOrUpdate(userId, modelCode);
        return Result.success("切换成功",null);
    }
}