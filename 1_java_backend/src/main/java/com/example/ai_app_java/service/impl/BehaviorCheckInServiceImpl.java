package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.UserBehaviorCheckin;
import com.example.ai_app_java.mapper.UserBehaviorCheckinMapper;
import com.example.ai_app_java.service.BehaviorCheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BehaviorCheckInServiceImpl implements BehaviorCheckInService {

    @Autowired
    private UserBehaviorCheckinMapper checkinMapper;

    @Override
    public UserBehaviorCheckin submitCheckin(Long userId, String checkinType,
                                            String checkinValue, String note) {
        UserBehaviorCheckin checkin = new UserBehaviorCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinType(checkinType);
        checkin.setCheckinValue(checkinValue);
        checkin.setNote(note);
        checkin.setCheckinDate(LocalDate.now());
        checkin.setCreatedAt(LocalDateTime.now());
        checkinMapper.insert(checkin);
        return checkin;
    }

    @Override
    public List<UserBehaviorCheckin> getRecentCheckins(Long userId, int days) {
        QueryWrapper<UserBehaviorCheckin> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .between("checkin_date",
                   LocalDate.now().minusDays(days), LocalDate.now())
               .orderByDesc("created_at");
        return checkinMapper.selectList(wrapper);
    }

    @Override
    public List<UserBehaviorCheckin> getCheckinsByDate(Long userId, LocalDate date) {
        QueryWrapper<UserBehaviorCheckin> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("checkin_date", date)
               .orderByDesc("created_at");
        return checkinMapper.selectList(wrapper);
    }

    @Override
    public int getStreakDays(Long userId) {
        // 一次查询近90天数据，在内存中计算连续天数
        QueryWrapper<UserBehaviorCheckin> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("checkin_date")
               .last("LIMIT 90");
        List<UserBehaviorCheckin> checkins = checkinMapper.selectList(wrapper);

        if (checkins == null || checkins.isEmpty()) {
            return 0;
        }

        Set<LocalDate> dates = checkins.stream()
                .map(UserBehaviorCheckin::getCheckinDate)
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        // 最多向后追溯365天
        for (int i = 0; i < 365; i++) {
            if (dates.contains(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                // 跳过gap（中间某天没打卡）
                if (i == 0) {
                    // 今天还没打卡，继续看昨天
                    checkDate = checkDate.minusDays(1);
                    continue;
                }
                break;
            }
        }
        return streak;
    }

    @Override
    public Map<String, Long> getCheckinStats(Long userId, int days) {
        List<UserBehaviorCheckin> checkins = getRecentCheckins(userId, days);
        Map<String, Long> stats = new HashMap<>();
        for (UserBehaviorCheckin c : checkins) {
            String type = c.getCheckinType();
            stats.put(type, stats.getOrDefault(type, 0L) + 1);
        }
        return stats;
    }

    @Override
    public double calculateCheckinScore(Long userId, int days) {
        // 活跃度 = 实际打卡天数 / 统计天数
        List<UserBehaviorCheckin> checkins = getRecentCheckins(userId, days);
        long distinctDays = checkins.stream()
            .map(UserBehaviorCheckin::getCheckinDate)
            .distinct()
            .count();
        // 每天多次打卡算1天
        return Math.min(1.0, (double) distinctDays / days);
    }
}
