package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.UserBehaviorCheckin;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 行为打卡服务接口
 *
 * 支持四类打卡：mood（心情）/ sleep（睡眠）/ exercise（运动）/ social（社交）
 */
public interface BehaviorCheckInService {

    /**
     * 提交打卡
     * @param userId 用户ID
     * @param checkinType 打卡类型
     * @param checkinValue 打卡值
     * @param note 用户备注
     * @return 打卡记录
     */
    UserBehaviorCheckin submitCheckin(Long userId, String checkinType, String checkinValue, String note);

    /**
     * 获取用户最近N天的打卡记录
     * @param userId 用户ID
     * @param days 天数
     * @return 打卡记录列表
     */
    List<UserBehaviorCheckin> getRecentCheckins(Long userId, int days);

    /**
     * 获取用户某日的打卡记录
     * @param userId 用户ID
     * @param date 日期
     * @return 打卡记录（可能为空）
     */
    List<UserBehaviorCheckin> getCheckinsByDate(Long userId, LocalDate date);

    /**
     * 获取用户连续打卡天数
     * @param userId 用户ID
     * @return 连续打卡天数
     */
    int getStreakDays(Long userId);

    /**
     * 获取用户打卡统计（各类型打卡次数）
     * @param userId 用户ID
     * @param days 统计天数
     * @return Map（类型 → 次数）
     */
    Map<String, Long> getCheckinStats(Long userId, int days);

    /**
     * 计算打卡活跃度得分（0~1）
     * 基于最近N天的打卡天数比例
     * @param userId 用户ID
     * @param days 统计天数
     * @return 活跃度得分
     */
    double calculateCheckinScore(Long userId, int days);
}
