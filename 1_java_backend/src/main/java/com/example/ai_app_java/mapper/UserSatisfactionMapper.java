package com.example.ai_app_java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.UserSatisfaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户满意度 Mapper
 *
 * 提供对 user_satisfaction 表的数据访问操作
 * 支持按模型、用户、时间范围等条件查询满意度记录
 *
 * @author HEART Framework Integration
 */
@Mapper
public interface UserSatisfactionMapper extends BaseMapper<UserSatisfaction> {

    /**
     * 查询指定模型的满意度记录
     *
     * @param modelCode 模型代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 满意度记录列表
     */
    @Select("SELECT * FROM user_satisfaction " +
            "WHERE model_code = #{modelCode} " +
            "AND submitted_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY submitted_at DESC")
    List<UserSatisfaction> selectByModelAndTimeRange(
            @Param("modelCode") String modelCode,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定用户的所有满意度记录
     *
     * @param userId 用户ID
     * @param limit 返回记录数限制
     * @return 满意度记录列表
     */
    @Select("SELECT * FROM user_satisfaction " +
            "WHERE user_id = #{userId} " +
            "ORDER BY submitted_at DESC " +
            "LIMIT #{limit}")
    List<UserSatisfaction> selectRecentByUserId(
            @Param("userId") Long userId,
            @Param("limit") int limit);

    /**
     * 查询指定会话的满意度记录
     *
     * @param sessionId 会话ID
     * @return 满意度记录列表
     */
    @Select("SELECT * FROM user_satisfaction " +
            "WHERE session_id = #{sessionId}")
    UserSatisfaction selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 获取所有模型的 HEART 指标统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各模型的 HEART 统计数据
     */
    @Select("SELECT model_code, " +
            "AVG(happiness) as happiness, " +
            "AVG(engagement) as engagement, " +
            "AVG(adoption) as adoption, " +
            "AVG(retention) as retention, " +
            "AVG(task_success) as taskSuccess, " +
            "AVG(overall_score) as overallScore, " +
            "COUNT(*) as count " +
            "FROM user_satisfaction " +
            "WHERE submitted_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY model_code")
    List<UserSatisfaction> selectHeartStatsByModel(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询所有模型在指定时间范围的综合评分趋势
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日综合评分
     */
    @Select("SELECT DATE(submitted_at) as date, model_code, AVG(overall_score) as overallScore " +
            "FROM user_satisfaction " +
            "WHERE submitted_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(submitted_at), model_code " +
            "ORDER BY date")
    List<UserSatisfaction> selectScoreTrend(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
