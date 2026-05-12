package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.UserBehaviorCheckin;
import com.example.ai_app_java.service.BehaviorCheckInService;
import com.example.ai_app_java.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
public class BehaviorCheckInController {

    @Autowired
    private BehaviorCheckInService checkInService;

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 提交打卡
     * POST /checkin
     * Body: { checkinType, checkinValue, note }
     */
    @PostMapping
    public Result submitCheckin(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody Map<String, String> body) {
        String checkinType = body.get("checkinType");
        String checkinValue = body.get("checkinValue");
        String note = body.get("note");

        if (checkinType == null || checkinValue == null) {
            return Result.fail(400, "打卡类型和值不能为空");
        }

        UserBehaviorCheckin record = checkInService.submitCheckin(
            userId, checkinType, checkinValue, note);

        // 同步更新画像
        new Thread(() -> {
            try {
                userProfileService.dynamicProfileCorrection(userId);
            } catch (Exception e) {
                System.out.println("画像更新失败：" + e.getMessage());
            }
        }).start();

        return Result.success("打卡成功", record);
    }

    /**
     * 获取连续打卡天数
     * GET /checkin/streak
     */
    @GetMapping("/streak")
    public Result getStreak(@RequestAttribute("currentUserId") Long userId) {
        int streak = checkInService.getStreakDays(userId);
        return Result.success("查询成功", Map.of("streak", streak));
    }

    /**
     * 获取打卡统计
     * GET /checkin/stats?days=7
     */
    @GetMapping("/stats")
    public Result getStats(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        Map<String, Long> stats = checkInService.getCheckinStats(userId, days);
        int streak = checkInService.getStreakDays(userId);
        double score = checkInService.calculateCheckinScore(userId, days);
        return Result.success("查询成功", Map.of(
            "stats", stats,
            "streak", streak,
            "activeScore", String.format("%.2f", score)
        ));
    }

    /**
     * 获取最近打卡记录
     * GET /checkin/recent?days=7
     */
    @GetMapping("/recent")
    public Result getRecentCheckins(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        List<UserBehaviorCheckin> records = checkInService.getRecentCheckins(userId, days);
        return Result.success("查询成功", records);
    }
}
