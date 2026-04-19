package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.ResourceRecommendation;
import com.example.ai_app_java.entity.SupportiveResource;
import com.example.ai_app_java.service.ResourceService;
import com.example.ai_app_java.mapper.SupportiveResourceMapper;
import com.example.ai_app_java.mapper.ResourceRecommendationMapper;
import com.example.ai_app_java.annotation.RequireRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resource")
public class ResourceController {
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private SupportiveResourceMapper supportiveResourceMapper;
    @Autowired
    private ResourceRecommendationMapper resourceRecommendationMapper;

    //===============用户接口：需登录=============
    /**
     * 根据情绪参数推荐资源（供AI对话流程调用）
     * GET http://localhost:8080/resource/recommend?emotionType=depression&emotionScore=0.5
     */
    @GetMapping("/recommend")
    public Result getResourceRecommendation(
        @RequestParam(required = false) String emotionType,
        @RequestParam(required = false) Double emotionScore,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role) {
        List<SupportiveResource> resources = resourceService.getResourcesByEmotion(emotionType, emotionScore);
        //同时记录推荐日志
        if (resources != null && !resources.isEmpty()) {
            for (SupportiveResource r : resources) {
                resourceService.recordResourceRecommendation(
                        userId, null, r.getId(), emotionType, emotionScore);
            }
        }
        return Result.success("获取资源推荐成功", resources);

    }

    /**
     * 用户获取所有启用的资源（可按分类过滤）
     * 路由:GET http://localhost:8080/resource/all?category=crisis
     */
    @GetMapping("/all")
    public Result getAllResources(
        @RequestParam(required = false) String category,
        @RequestAttribute("currentUserId") Long userId) {
        List<SupportiveResource> resources = resourceService.getAllResources(category);
        return Result.success("获取资源列表成功",resources);
    }
    /**
     * 查看单条资源详情(登录用户)
     * GET http://localhost:8080/resource/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result getResourceDetail(
        @PathVariable Long id,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role) {
        SupportiveResource resource = resourceService.getResourceById(id);
        if(resource == null){
            return Result.fail(404,"资源不存在或无权限查看");
        }
        return Result.success("获取资源详情成功",resource);
    }
    /**
     * 用户查看自己的推荐记录
     * GET http://localhost:8080/resource/my/recommendations
     */
    @GetMapping("/my/recommendations")
    public Result getMyRecommendations(
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        List<?> records = resourceService.getUserRecommendations(userId);
        return Result.success("获取推荐记录成功",records);
        }
    //===============管理员接口：需管理员权限(ADMIN角色)=============

    /**
     * 分页查询资源列表
     * GET http://localhost:8080/resource/admin/list?category=crisis&pageNum=1&pageSize=10
     */
    @GetMapping("/admin/list")
    @RequireRole({"ADMIN"})
    public Result getResourceList(
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue= "10") int pageSize,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        List<SupportiveResource> resources = resourceService.adminGetResources(category, pageNum, pageSize);
        return Result.success("获取资源列表成功",resources);
    }
    /**
     * 管理员新增资源
     * POST http://localhost:8080/resource/admin/add
     */
    @PostMapping("/admin/add")
    @RequireRole({"ADMIN"})
    public Result addResource(
        @RequestBody SupportiveResource resource,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        Long id = resourceService.addResource(resource);
        if(id == null || id <= 0){
            return Result.fail(500,"新增资源失败");
        }
        return Result.success("新增资源成功",id);
    }
    /**
     * 管理员更新资源(没有则创建)
     * PUT http://localhost:8080/resource/admin/update/{id}
     */
    @PutMapping("/admin/update/{id}")
    @RequireRole({"ADMIN"})
    public Result updateResource(
        @PathVariable Long id,
        @RequestBody SupportiveResource resource,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        resource.setId(id);
        boolean success = resourceService.updateResource(resource);
        if(!success){
            return Result.fail(500,"更新资源失败");
        }
        return Result.success("更新资源成功",success);
    }
    /**
     * 管理员删除资源
     * DELETE http://localhost:8080/resource/admin/delete/{id}
     */
    @DeleteMapping("/admin/delete/{id}")
    @RequireRole({"ADMIN"})
    public Result deleteResource(
        @PathVariable Long id,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        boolean success = resourceService.deleteResource(id);
        if(!success){
            return Result.fail(500,"删除资源失败");
        }
        return Result.success("删除资源成功",success);
    }
    /**
     * 查看所有推荐记录（统计分析，管理员专属）
     * GET http://localhost:8080/resource/admin/recommendations
     */
    @GetMapping("/admin/recommendations")
    @RequireRole({"ADMIN"})
    public Result getRecommendations(
        @RequestParam(required = false) String emotionType,
        @RequestParam(required = false) Double emotionScore,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        List<?> records = resourceService.getRecommendations(emotionType,emotionScore);
        return Result.success("获取推荐记录成功",records);
    }
    /**
     * 管理员启用/禁用资源
     * PUT http://localhost:8080/resource/admin/toggle/{id}
     */
    @PutMapping("/admin/toggle/{id}")
    @RequireRole({"ADMIN"})
    public Result toggleResource(
        @PathVariable Long id,
        @RequestBody Map<String,Boolean> body,
        @RequestAttribute("currentUserId") Long userId,
        @RequestAttribute("currentRole") String role){
        boolean success = resourceService.toggleResource(id,body.get("enabled"));
        if(!success){
            return Result.fail(500,"启用/禁用资源失败");
        }
        return Result.success("启用/禁用资源成功",success);
    }
}