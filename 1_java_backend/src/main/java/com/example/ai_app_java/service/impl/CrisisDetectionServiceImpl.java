package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ai_app_java.entity.CrisisAlert;
import com.example.ai_app_java.mapper.CrisisAlertMapper;
import com.example.ai_app_java.service.CrisisDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

//=================================================
//          危机预警服务实现类
//=================================================

@Service
public class CrisisDetectionServiceImpl implements CrisisDetectionService {
    @Autowired
    private CrisisAlertMapper crisisAlertMapper;
    //=================================================
    //              高危关键词：自杀/自残相关
    //=================================================
    private static final String[] HIGH_RISK_KEYWORDS = {
        "想死", "不想活了", "活不下去", "想自杀", "自杀", "轻生",
        "死了算了", "活着没意义", "活着没意思", "一了百了",
        "割腕", "跳楼", "服毒", "上吊", "zs", "si"
    };
    //=================================================
    //              中危关键词：自残/暴力倾向
    //=================================================
    private static final String[] MEDIUM_RISK_KEYWORDS = {
        "自残", "割自己", "想割", "伤害自己", "暴力", "杀人",
        "报复社会", "绝望", "崩溃", "撑不住了"
    };
    //=================================================
    //              低危关键词：消极情绪
    //=================================================
    private static final String[] LOW_RISK_KEYWORDS = {
        "好累", "撑不下去", "坚持不住了", "完蛋了",
        "没希望了", "没人能帮我", "没人理解我"
    };

    //=================================================
    //          检测用户消息是否包含危机信号
    //=================================================
    @Override
    public CrisisAlert checkCrisis(Long userId, Long sessionId, Long messageId, String content) {
       if(content == null || content.isEmpty()){
        return null;
       }
            //1、检查消息是否包含高危关键词
            String found = checkKeywords(content, HIGH_RISK_KEYWORDS);
            if(found != null){
                CrisisAlert alert = createAlert(userId, sessionId, messageId, "high", "自杀倾向", found);
                crisisAlertMapper.insert(alert);
                return alert;
            }
            //2、检查消息是否包含中危关键词
            found = checkKeywords(content, MEDIUM_RISK_KEYWORDS);
            if(found != null){
                CrisisAlert alert = createAlert(userId, sessionId, messageId, "medium", "自残倾向", found);
                crisisAlertMapper.insert(alert);
                return alert;
            }
            //3、检查消息是否包含低危关键词
            found = checkKeywords(content, LOW_RISK_KEYWORDS);
            if(found != null){
                CrisisAlert alert = createAlert(userId, sessionId, messageId, "low", "消极情绪", found);
                crisisAlertMapper.insert(alert);
                return alert;
            }
            return null;//没有检测到危机
    }

    //=================================================
    //          获取所有待处理的危机预警
    //=================================================
    @Override
    public List<CrisisAlert> getPendingAlerts() {
        QueryWrapper<CrisisAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("status","pending")
        .orderByDesc("created_at");//按时间倒序排列，新的在上面，旧的在下面
        return crisisAlertMapper.selectList(wrapper);
    }

    //=================================================
    //          处理危机预警
    //=================================================
    @Override
    public void handleAlert(Long alertId, String handlerNotes) {
        CrisisAlert alert = crisisAlertMapper.selectById(alertId);
            if(alert != null){
                alert.setStatus("handled");
                alert.setHandledAt(LocalDateTime.now());
                alert.setHandlerNotes(handlerNotes);
                crisisAlertMapper.updateById(alert);//更新危机预警状态
            }
    }

    //=================================================
    //          获取用户的危机预警列表
    //=================================================
    @Override
    public List<CrisisAlert> getUserAlerts(Long userId) {
        QueryWrapper<CrisisAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId)
        .orderByDesc("created_at");//按时间倒序排列，新的在上面，旧的在下面
        return crisisAlertMapper.selectList(wrapper);
    }

    //=================================================
    //          获取所有危机预警（支持按状态筛选）
    //=================================================
    @Override
    public List<CrisisAlert> getAllAlerts(String status) {
        QueryWrapper<CrisisAlert> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_at");
        return crisisAlertMapper.selectList(wrapper);
    }

    //=================================================
    //          私有辅助方法
    //=================================================
    
    
    //=================================================
    //          检查消息是否包含关键词
    //=================================================
    private String checkKeywords(String content, String[] keywords) {
        StringBuilder found = new StringBuilder();
        for(String keyword : keywords){
            if(content.contains(keyword)){
                if(found.length() > 0){
                    found.append(",");
                }
                found.append(keyword);
            }
        }
        return found.length() >0 ? found.toString() : null;
    }

    //=================================================
    //          创建危机预警对象
    //=================================================
    private CrisisAlert createAlert(Long userId, Long sessionId, Long messageId, 
                                    String level, String type, String keywords) {
        CrisisAlert alert = new CrisisAlert();
        alert.setUserId(userId);
        alert.setSessionId(sessionId);
        alert.setMessageId(messageId);
        alert.setAlertLevel(level);
        alert.setAlertType(type);
        alert.setKeywords(keywords);
        alert.setStatus("pending");
        alert.setCreatedAt(LocalDateTime.now());
        return alert;
    }

    

}