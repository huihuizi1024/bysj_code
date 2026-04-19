package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.PageResult;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.ResourceRepository;
import com.example.ai_app_java.service.ResourceRepositoryService;
import com.example.ai_app_java.annotation.RequireRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resource-repo")
public class ResourceRepositoryController {

    @Autowired
    private ResourceRepositoryService resourceRepositoryService;

    /**
     * 分页查询资源库列表（管理员）
     * GET http://localhost:8080/resource-repo/admin/list?category=crisis&pageNum=1&pageSize=10
     */
    @GetMapping("/admin/list")
    @RequireRole({"ADMIN"})
    public Result getList(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestAttribute("currentUserId") Long userId,
            @RequestAttribute("currentRole") String role) {
        PageResult<ResourceRepository> page = resourceRepositoryService.adminList(category, pageNum, pageSize);
        return Result.success("获取资源库列表成功", page);
    }

    /**
     * 获取单个资源库详情（管理员）
     * GET http://localhost:8080/resource-repo/admin/{id}
     */
    @GetMapping("/admin/{id}")
    @RequireRole({"ADMIN"})
    public Result getById(
            @PathVariable Long id,
            @RequestAttribute("currentUserId") Long userId,
            @RequestAttribute("currentRole") String role) {
        ResourceRepository repo = resourceRepositoryService.getById(id);
        if (repo == null) {
            return Result.fail(404, "资源库不存在");
        }
        return Result.success("获取资源库详情成功", repo);
    }

    /**
     * 新增资源库（管理员）
     * POST http://localhost:8080/resource-repo/admin/add
     */
    @PostMapping("/admin/add")
    @RequireRole({"ADMIN"})
    public Result add(
            @RequestBody ResourceRepository repo,
            @RequestAttribute("currentUserId") Long userId,
            @RequestAttribute("currentRole") String role) {
        if (repo.getCode() == null || repo.getCode().isBlank()) {
            return Result.fail(400, "资源库代码不能为空");
        }
        // 检查 code 是否重复
        ResourceRepository exist = resourceRepositoryService.getByCode(repo.getCode());
        if (exist != null) {
            return Result.fail(400, "资源库代码已存在: " + repo.getCode());
        }
        Long id = resourceRepositoryService.add(repo);
        if (id == null) {
            return Result.fail(500, "新增资源库失败");
        }
        return Result.success("新增资源库成功", id);
    }

    /**
     * 更新资源库（管理员）
     * PUT http://localhost:8080/resource-repo/admin/update/{id}
     */
    @PutMapping("/admin/update/{id}")
    @RequireRole({"ADMIN"})
    public Result update(
            @PathVariable Long id,
            @RequestBody ResourceRepository repo,
            @RequestAttribute("currentUserId") Long userId,
            @RequestAttribute("currentRole") String role) {
        repo.setId(id);
        boolean success = resourceRepositoryService.update(repo);
        if (!success) {
            return Result.fail(500, "更新资源库失败");
        }
        return Result.success("更新资源库成功", true);
    }

    /**
     * 删除资源库（管理员）
     * DELETE http://localhost:8080/resource-repo/admin/delete/{id}
     */
    @DeleteMapping("/admin/delete/{id}")
    @RequireRole({"ADMIN"})
    public Result delete(
            @PathVariable Long id,
            @RequestAttribute("currentUserId") Long userId,
            @RequestAttribute("currentRole") String role) {
        boolean success = resourceRepositoryService.delete(id);
        if (!success) {
            return Result.fail(500, "删除资源库失败");
        }
        return Result.success("删除资源库成功", true);
    }

    /**
     * 启用/禁用资源库（管理员）
     * PUT http://localhost:8080/resource-repo/admin/toggle/{id}
     */
    @PutMapping("/admin/toggle/{id}")
    @RequireRole({"ADMIN"})
    public Result toggle(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            @RequestAttribute("currentUserId") Long userId,
            @RequestAttribute("currentRole") String role) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return Result.fail(400, "缺少 enabled 参数");
        }
        boolean success = resourceRepositoryService.toggleEnabled(id, enabled);
        if (!success) {
            return Result.fail(500, "操作失败");
        }
        return Result.success((enabled ? "启用" : "禁用") + "资源库成功", true);
    }
}
