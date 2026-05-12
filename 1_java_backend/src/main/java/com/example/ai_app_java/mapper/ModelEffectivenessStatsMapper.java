package com.example.ai_app_java.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.ModelEffectivenessStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 模型效果统计 Mapper
 *
 * 提供对 model_effectiveness_stats 表的数据访问操作
 * 支持查询各模型的统计历史数据，用于前端图表展示
 *
 * @author MentalAlign + HEART Framework Integration
 */
@Mapper
public interface ModelEffectivenessStatsMapper extends BaseMapper<ModelEffectivenessStats> {

    /**
     * 查询指定模型在指定日期范围内的统计记录
     *
     * @param modelCode 模型代码
     * @param statType 统计类型（daily/weekly/monthly）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计记录列表
     */
    @Select("SELECT * FROM model_effectiveness_stats " +
            "WHERE model_code = #{modelCode} " +
            "AND stat_type = #{statType} " +
            "AND stat_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY stat_date ASC")
    List<ModelEffectivenessStats> selectByModelAndDateRange(
            @Param("modelCode") String modelCode,
            @Param("statType") String statType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询所有模型的最新统计记录
     *
     * @param statType 统计类型
     * @param limit 返回记录数限制
     * @return 各模型的最新统计数据
     */
    @Select("SELECT * FROM model_effectiveness_stats m1 " +
            "WHERE stat_type = #{statType} " +
            "AND stat_date = (SELECT MAX(stat_date) FROM model_effectiveness_stats m2 " +
            "                 WHERE m2.model_code = m1.model_code AND m2.stat_type = m1.stat_type) " +
            "ORDER BY m1.avg_overall_score DESC " +
            "LIMIT #{limit}")
    List<ModelEffectivenessStats> selectLatestByAllModels(
            @Param("statType") String statType,
            @Param("limit") int limit);

    /**
     * 查询指定日期范围内的所有模型统计对比
     *
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 各模型的统计数据
     */
    @Select("SELECT * FROM model_effectiveness_stats " +
            "WHERE stat_type = #{statType} " +
            "AND stat_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY model_code, stat_date")
    List<ModelEffectivenessStats> selectComparisonByDateRange(
            @Param("statType") String statType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据模型代码和日期获取单条统计记录
     *
     * @param modelCode 模型代码
     * @param statType 统计类型
     * @param statDate 统计日期
     * @return 统计记录
     */
    @Select("SELECT * FROM model_effectiveness_stats " +
            "WHERE model_code = #{modelCode} " +
            "AND stat_type = #{statType} " +
            "AND stat_date = #{statDate}")
    ModelEffectivenessStats selectByModelAndDate(
            @Param("modelCode") String modelCode,
            @Param("statType") String statType,
            @Param("statDate") LocalDate statDate);
}
