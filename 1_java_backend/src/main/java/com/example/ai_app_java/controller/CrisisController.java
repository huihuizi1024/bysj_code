package com.example.ai_app_java.controller;

import com.example.ai_app_java.annotation.RequireRole;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.CrisisAlert;
import com.example.ai_app_java.service.CrisisDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//危机预警控制器
@RestController
@RequestMapping("/crisis")//所有 /crisis开头的请求都归这里管
@CrossOrigin //处理前后端跨域，允许所有前端页面跨域访问这里的接口
public class CrisisController {
    @Autowired
    private CrisisDetectionService crisisDetectionService;

    /**
     * 获取所有待处理的危机预警（仅管理员用）
     * 路由:GET http://localhost:8080/crisis/pending
     */
    @GetMapping("/pending")
    @RequireRole({"ADMIN"})//只有管理员才能访问这个接口
    public Result getPendingAlerts() {
        List<CrisisAlert> list = crisisDetectionService.getPendingAlerts();
        return Result.success("获取所有待处理的危机预警成功",list);
    }

    /**
     * 处理危机预警（仅管理员用）
     * 路由:POST http://localhost:8080/crisis/handle/{alertId}
     */
    @PostMapping("/handle/{alertId}")
    @RequireRole({"ADMIN"})//只有管理员才能访问这个接口
    public Result handleAlert(
        @PathVariable Long alertId,
        @RequestBody Map<String,String> body) {
        String handlerNotes = body.getOrDefault("handlerNotes","");
        crisisDetectionService.handleAlert(alertId,handlerNotes);
        return Result.success("处理危机预警成功",null);
        }

    /**
     * 获取当前用户的危机预警列表
     * 路由:GET http://localhost:8080/crisis/user/alerts
     */
    @GetMapping("/user/alerts")
    public Result getUserAlerts(@RequestAttribute("currentUserId") Long userId) {
        List<CrisisAlert> list = crisisDetectionService.getUserAlerts(userId);
        return Result.success("获取用户的危机预警列表成功",list);
    }

    /**
     * 获取所有危机预警（支持按状态筛选，仅管理员用）
     * 路由:GET http://localhost:8080/crisis/all?status=pending
     * @param status 可选状态筛选：pending/handled，不传则返回全部
     */
    @GetMapping("/all")
    @RequireRole({"ADMIN"})
    public Result getAllAlerts(@RequestParam(required = false) String status) {
        List<CrisisAlert> list = crisisDetectionService.getAllAlerts(status);
        return Result.success("获取危机预警列表成功", list);
    }

}