package com.example.ai_app_java.service;

import com.example.ai_app_java.entity.CrisisAlert;
import java.util.List;

//危机预警服务接口

public interface CrisisDetectionService {
    /**
     * 检测用户消息是否包含危机信号
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageId 消息ID
     * @param content 用户消息内容
     * @return 如果监测到危机，返回危机预警对象
     *         否则返回null
     */
    CrisisAlert checkCrisis(Long userId, Long sessionId, Long messageId, String content);
    
    /**
     * 获取所有待处理的危机预警
     * @return 危机预警列表
     */
    List<CrisisAlert> getPendingAlerts();

    /**
     * 处理危机预警
     * @param alertId 危机预警ID
     * @param handlerNotes 处理备注
     */
    void handleAlert(Long alertId, String handlerNotes);
    
    /**
     * 获取用户的危机预警列表
     * @param userId 用户ID
     * @return 危机预警列表
     */
    List<CrisisAlert> getUserAlerts(Long userId);

    /**
     * 获取所有危机预警（支持按状态筛选）
     * @param status 状态筛选，可为 null（返回全部）
     * @return 危机预警列表
     */
    List<CrisisAlert> getAllAlerts(String status);

}