package com.example.ai_app_java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.TherapyEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI疗效评估 Mapper
 *
 * 提供对 therapy_evaluation 表的数据访问操作
 * 支持按模型、会话、用户、时间范围等条件查询评估记录
 *
 * @author MentalAlign Framework Integration
 */
@Mapper
public interface TherapyEvaluationMapper extends BaseMapper<TherapyEvaluation> {

    /**
     * 查询指定模型在指定时间范围内的评估记录
     *
     * @param modelCode 模型代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 评估记录列表
     */
    @Select("SELECT * FROM therapy_evaluation " +
            "WHERE model_code = #{modelCode} " +
            "AND evaluated_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY evaluated_at DESC")
    List<TherapyEvaluation> selectByModelAndTimeRange(
            @Param("modelCode") String modelCode,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定会话的所有评估记录
     *
     * @param sessionId 会话ID
     * @return 评估记录列表
     */
    @Select("SELECT * FROM therapy_evaluation " +
            "WHERE session_id = #{sessionId} " +
            "ORDER BY evaluated_at DESC")
    List<TherapyEvaluation> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询指定用户的所有评估记录
     *
     * @param userId 用户ID
     * @param limit 返回记录数限制
     * @return 评估记录列表
     */
    @Select("SELECT * FROM therapy_evaluation " +
            "WHERE user_id = #{userId} " +
            "ORDER BY evaluated_at DESC " +
            "LIMIT #{limit}")
    List<TherapyEvaluation> selectRecentByUserId(
            @Param("userId") Long userId,
            @Param("limit") int limit);

    /**
     * 计算指定模型的平均 CSS/ARS 得分
     *
     * @param modelCode 模型代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 包含 avgCss 和 avgArs 的数组
     */
    @Select("SELECT AVG(css_score) as avgCss, AVG(ars_score) as avgArs, COUNT(*) as evalCount " +
            "FROM therapy_evaluation " +
            "WHERE model_code = #{modelCode} " +
            "AND evaluated_at BETWEEN #{startTime} AND #{endTime}")
    List<TherapyEvaluation> selectAvgScoresByModel(
            @Param("modelCode") String modelCode,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取所有模型的最新统计数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各模型的统计数据
     */
    @Select("SELECT model_code, AVG(css_score) as avgCss, AVG(ars_score) as avgArs, " +
            "AVG(user_rating) as avgUserRating, COUNT(*) as evalCount " +
            "FROM therapy_evaluation " +
            "WHERE evaluated_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY model_code")
    List<TherapyEvaluation> selectModelStats(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
