package com.example.ai_app_java.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_behavior_checkin")
public class UserBehaviorCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 打卡类型: mood / sleep / exercise / social */
    private String checkinType;

    /** 打卡值: happy / sad / ok / good_sleep / poor_sleep 等 */
    private String checkinValue;

    /** 用户备注 */
    private String note;

    /** 打卡日期 */
    private LocalDate checkinDate;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
