<template>
  <div class="evaluation-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><i class="fa-solid fa-chart-line"></i> 模型疗效评估</h2>
      <p class="page-desc">MentalAlign + HEART 框架下的 AI 模型效果对比分析</p>

      <!-- 时间范围选择 -->
      <div class="time-selector">
        <el-radio-group v-model="timeRange" size="default" @change="handleTimeRangeChange">
          <el-radio-button label="7">近7天</el-radio-button>
          <el-radio-button label="14">近14天</el-radio-button>
          <el-radio-button label="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><i class="fa-solid fa-spinner"></i></el-icon>
      <span>加载评估数据中...</span>
    </div>

    <!-- 评估内容 -->
    <div v-else class="dashboard-content">
      <!-- 概览卡片 -->
      <div class="overview-cards">
        <div class="stat-card">
          <div class="stat-icon blue">
            <i class="fa-solid fa-brain"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">平均认知支持</div>
            <div class="stat-value">{{ platformStats.avgCss?.toFixed(2) || '0.00' }}</div>
            <div class="stat-hint">CSS (MentalAlign)</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon pink">
            <i class="fa-solid fa-heart"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">平均情感共鸣</div>
            <div class="stat-value">{{ platformStats.avgArs?.toFixed(2) || '0.00' }}</div>
            <div class="stat-hint">ARS (MentalAlign)</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon green">
            <i class="fa-solid fa-star"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">平均满意度</div>
            <div class="stat-value">{{ platformStats.avgHappiness?.toFixed(1) || '0.0' }}</div>
            <div class="stat-hint">HEART (满分5分)</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon orange">
            <i class="fa-solid fa-users"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">总评估次数</div>
            <div class="stat-value">{{ platformStats.evalCount || 0 }}</div>
            <div class="stat-hint">MentalAlign 评分记录</div>
          </div>
        </div>
      </div>

      <!-- 模型对比区域 -->
      <div class="comparison-section">
        <h3 class="section-title">
          <i class="fa-solid fa-chart-bar"></i>
          模型效果对比
        </h3>

        <!-- MentalAlign 对比 -->
        <div class="comparison-block">
          <h4>MentalAlign 疗效评估</h4>
          <div class="chart-container">
            <div v-if="therapyComparison.length === 0" class="empty-chart">
              <i class="fa-solid fa-chart-simple"></i>
              <p>暂无评估数据</p>
            </div>
            <div v-else class="bar-chart">
              <div
                v-for="(model, index) in therapyComparison"
                :key="model.modelCode"
                class="bar-item"
              >
                <div class="bar-label">
                  <span class="model-name">{{ model.modelName || model.modelCode }}</span>
                  <span class="bar-count">{{ model.count || 0 }} 次</span>
                </div>
                <div class="bar-group">
                  <div class="bar-wrapper">
                    <div class="bar-fill css" :style="{ width: (model.avgCss * 100) + '%' }">
                      <span class="bar-value">{{ (model.avgCss || 0).toFixed(2) }}</span>
                    </div>
                  </div>
                  <span class="bar-name">CSS</span>
                </div>
                <div class="bar-group">
                  <div class="bar-wrapper">
                    <div class="bar-fill ars" :style="{ width: (model.avgArs * 100) + '%' }">
                      <span class="bar-value">{{ (model.avgArs || 0).toFixed(2) }}</span>
                    </div>
                  </div>
                  <span class="bar-name">ARS</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- HEART 对比 -->
        <div class="comparison-block">
          <h4>HEART 用户体验评估</h4>
          <div class="chart-container">
            <div v-if="heartComparison.length === 0" class="empty-chart">
              <i class="fa-solid fa-chart-simple"></i>
              <p>暂无满意度数据</p>
            </div>
            <div v-else class="radar-container">
              <div
                v-for="(model, index) in heartComparison"
                :key="model.modelCode"
                class="model-radar"
              >
                <div class="radar-header">
                  <span class="model-name">{{ model.modelName || model.modelCode }}</span>
                  <span class="radar-score">{{ (model.avgOverall || 0).toFixed(1) }} 分</span>
                </div>
                <div class="heart-bars">
                  <div class="heart-item">
                    <span class="heart-label">Happiness</span>
                    <el-progress :percentage="Math.round((model.avgHappiness / 5) * 100)" :stroke-width="8" />
                    <span class="heart-value">{{ (model.avgHappiness || 0).toFixed(1) }}</span>
                  </div>
                  <div class="heart-item">
                    <span class="heart-label">Engagement</span>
                    <el-progress :percentage="Math.round((model.avgEngagement || 0) * 100)" :stroke-width="8" />
                    <span class="heart-value">{{ ((model.avgEngagement || 0) * 100).toFixed(0) }}%</span>
                  </div>
                  <div class="heart-item">
                    <span class="heart-label">Adoption</span>
                    <el-progress :percentage="Math.round((model.avgAdoption || 0) * 100)" :stroke-width="8" />
                    <span class="heart-value">{{ ((model.avgAdoption || 0) * 100).toFixed(0) }}%</span>
                  </div>
                  <div class="heart-item">
                    <span class="heart-label">Retention</span>
                    <el-progress :percentage="Math.round((model.avgRetention || 0) * 100)" :stroke-width="8" />
                    <span class="heart-value">{{ ((model.avgRetention || 0) * 100).toFixed(0) }}%</span>
                  </div>
                  <div class="heart-item">
                    <span class="heart-label">Task Success</span>
                    <el-progress :percentage="Math.round((model.avgTaskSuccess || 0) * 100)" :stroke-width="8" />
                    <span class="heart-value">{{ ((model.avgTaskSuccess || 0) * 100).toFixed(0) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 趋势图表 -->
      <div class="trend-section">
        <h3 class="section-title">
          <i class="fa-solid fa-chart-line"></i>
          疗效趋势
        </h3>

        <div class="trend-tabs">
          <el-radio-group v-model="trendModel" size="small">
            <el-radio-button
              v-for="model in therapyComparison"
              :key="model.modelCode"
              :label="model.modelCode"
            >
              {{ model.modelName || model.modelCode }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <div class="trend-chart" v-if="therapyTrend.length > 0">
          <div class="trend-legend">
            <span class="legend-item css"><i class="fa-solid fa-circle"></i> 认知支持 (CSS)</span>
            <span class="legend-item ars"><i class="fa-solid fa-circle"></i> 情感共鸣 (ARS)</span>
          </div>
          <div class="trend-bars">
            <div
              v-for="(item, index) in therapyTrend"
              :key="index"
              class="trend-bar-item"
            >
              <div class="trend-date">{{ item.date }}</div>
              <div class="trend-values">
                <div
                  class="trend-bar css"
                  :style="{ height: (item.avgCss * 100) + 'px' }"
                  :title="'CSS: ' + item.avgCss"
                ></div>
                <div
                  class="trend-bar ars"
                  :style="{ height: (item.avgArs * 100) + 'px' }"
                  :title="'ARS: ' + item.avgArs"
                ></div>
              </div>
              <div class="trend-value">{{ (item.avgCss || 0).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-trend">
          <i class="fa-solid fa-chart-line"></i>
          <p>暂无趋势数据</p>
        </div>
      </div>

      <!-- 模型排名 -->
      <div class="ranking-section">
        <h3 class="section-title">
          <i class="fa-solid fa-trophy"></i>
          模型排名
        </h3>

        <div class="ranking-table">
          <div class="ranking-header">
            <span class="rank-col">排名</span>
            <span class="model-col">模型</span>
            <span class="css-col">CSS</span>
            <span class="ars-col">ARS</span>
            <span class="score-col">综合评分</span>
          </div>

          <div
            v-for="(model, index) in rankedModels"
            :key="model.modelCode"
            :class="['ranking-row', { top: index < 3 }]"
          >
            <span class="rank-col">
              <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
            </span>
            <span class="model-col">{{ model.modelName || model.modelCode }}</span>
            <span class="css-col">{{ (model.avgCss || 0).toFixed(2) }}</span>
            <span class="ars-col">{{ (model.avgArs || 0).toFixed(2) }}</span>
            <span class="score-col">
              <span class="score-badge">{{ ((model.avgCss + model.avgArs) / 2 * 5).toFixed(1) }}</span>
              <el-rate :model-value="(model.avgCss + model.avgArs) / 2 * 5" disabled show-score disabled-text="" />
            </span>
          </div>
        </div>
      </div>

      <!-- 框架说明 -->
      <div class="framework-info">
        <h3 class="section-title">
          <i class="fa-solid fa-info-circle"></i>
          评估框架说明
        </h3>

        <div class="framework-cards">
          <div class="framework-card">
            <div class="framework-header">
              <i class="fa-solid fa-brain"></i>
              <h4>MentalAlign 框架</h4>
            </div>
            <div class="framework-content">
              <p>MentalAlign 是一个用于评估 AI 在心理健康支持场景中治疗质量的双基准框架。</p>
              <ul>
                <li><strong>CSS (Cognitive Support Score)</strong> - 认知支持得分，评估 AI 在引导性、信息量、专业性、结构化方面的表现</li>
                <li><strong>ARS (Affective Resonance Score)</strong> - 情感共鸣得分，评估 AI 在共情表达、情感验证、温暖感、安全感方面的表现</li>
              </ul>
            </div>
          </div>

          <div class="framework-card">
            <div class="framework-header">
              <i class="fa-solid fa-heart"></i>
              <h4>HEART 框架</h4>
            </div>
            <div class="framework-content">
              <p>HEART 是 Google 提出的用户体验评估框架，包含五个核心指标。</p>
              <ul>
                <li><strong>Happiness</strong> - 用户满意度</li>
                <li><strong>Engagement</strong> - 用户参与度</li>
                <li><strong>Adoption</strong> - 产品接受度</li>
                <li><strong>Retention</strong> - 用户留存率</li>
                <li><strong>Task Success</strong> - 任务完成度</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTherapyComparison,
  getHeartComparison,
  getTherapyTrend,
  getPlatformStats
} from '@/api/evaluation'

// 状态
const loading = ref(true)
const timeRange = ref('7')
const trendModel = ref('')

// 数据
const therapyComparison = ref([])
const heartComparison = ref([])
const therapyTrend = ref([])
const platformStats = ref({})

// 排名计算
const rankedModels = computed(() => {
  return [...therapyComparison.value].sort((a, b) => {
    const scoreA = ((a.avgCss || 0) + (a.avgArs || 0)) / 2
    const scoreB = ((b.avgCss || 0) + (b.avgArs || 0)) / 2
    return scoreB - scoreA
  })
})

// 加载数据
const loadData = async () => {
  loading.value = true
  const days = parseInt(timeRange.value)

  try {
    const [therapyRes, heartRes, statsRes] = await Promise.all([
      getTherapyComparison(days),
      getHeartComparison(days),
      getPlatformStats(days)
    ])

    if (therapyRes.data?.data) {
      therapyComparison.value = therapyRes.data.data
    }
    if (heartRes.data?.data) {
      heartComparison.value = heartRes.data.data
    }
    if (statsRes.data?.data) {
      platformStats.value = statsRes.data.data
    }

    // 设置默认趋势模型
    if (therapyComparison.value.length > 0 && !trendModel.value) {
      trendModel.value = therapyComparison.value[0].modelCode
    }
  } catch (err) {
    console.error('加载评估数据失败', err)
    ElMessage.error('加载评估数据失败')
  } finally {
    loading.value = false
  }
}

// 加载趋势数据
const loadTrend = async () => {
  if (!trendModel.value) return

  try {
    const res = await getTherapyTrend(trendModel.value, parseInt(timeRange.value))
    if (res.data?.data) {
      therapyTrend.value = res.data.data
    }
  } catch (err) {
    console.error('加载趋势数据失败', err)
  }
}

// 时间范围变化
const handleTimeRangeChange = () => {
  loadData()
  loadTrend()
}

// 监听趋势模型变化
watch(trendModel, () => {
  loadTrend()
})

onMounted(() => {
  loadData()
  loadTrend()
})
</script>

<style scoped>
.evaluation-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h2 i {
  color: var(--color-primary);
}

.page-desc {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 16px;
}

.time-selector {
  display: flex;
  gap: 12px;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px;
  color: var(--color-text-light);
  font-size: 14px;
}

.loading-container i {
  font-size: 24px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 概览卡片 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
}

.stat-icon.blue { background: linear-gradient(135deg, #4a6fa5, #6b8cba); }
.stat-icon.pink { background: linear-gradient(135deg, #e74c3c, #ff7675); }
.stat-icon.green { background: linear-gradient(135deg, #27ae60, #2ecc71); }
.stat-icon.orange { background: linear-gradient(135deg, #f39c12, #e67e22); }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-dark);
}

.stat-hint {
  font-size: 11px;
  color: var(--color-text-light);
  margin-top: 2px;
}

/* 区块标题 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: var(--color-primary);
}

/* 模型对比 */
.comparison-section {
  margin-bottom: 24px;
}

.comparison-block {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.comparison-block h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
}

.empty-chart {
  text-align: center;
  padding: 40px;
  color: var(--color-text-light);
}

.empty-chart i {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.3;
}

/* 柱状图 */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bar-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: var(--radius-sm);
}

.bar-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.model-name {
  font-weight: 600;
  color: var(--color-text-dark);
}

.bar-count {
  font-size: 12px;
  color: var(--color-text-light);
}

.bar-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bar-wrapper {
  flex: 1;
  height: 20px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.bar-fill.css { background: linear-gradient(90deg, #4a6fa5, #6b8cba); }
.bar-fill.ars { background: linear-gradient(90deg, #e74c3c, #ff7675); }

.bar-value {
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.bar-name {
  font-size: 12px;
  color: var(--color-text-light);
  width: 30px;
}

/* HEART 雷达 */
.radar-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.model-radar {
  padding: 16px;
  background: #f8f9fa;
  border-radius: var(--radius-sm);
}

.radar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.radar-header .model-name {
  font-size: 14px;
}

.radar-score {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary);
}

.heart-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.heart-item {
  display: grid;
  grid-template-columns: 90px 1fr 36px;
  align-items: center;
  gap: 8px;
}

.heart-label {
  font-size: 11px;
  color: var(--color-text-light);
}

.heart-value {
  font-size: 11px;
  color: var(--color-text-dark);
  text-align: right;
}

:deep(.el-progress) {
  width: 100%;
}

:deep(.el-progress__text) {
  display: none;
}

/* 趋势 */
.trend-section {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.trend-tabs {
  margin-bottom: 16px;
}

.trend-chart {
  min-height: 200px;
}

.trend-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-item.css i { color: #4a6fa5; }
.legend-item.ars i { color: #e74c3c; }

.trend-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 160px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.trend-bar-item {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.trend-date {
  font-size: 10px;
  color: var(--color-text-light);
  margin-bottom: 4px;
}

.trend-values {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 4px;
  justify-content: center;
}

.trend-bar {
  width: 20px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
}

.trend-bar.css { background: #4a6fa5; }
.trend-bar.ars { background: #e74c3c; }

.trend-value {
  font-size: 10px;
  color: var(--color-text-light);
  margin-top: 4px;
}

.empty-trend {
  text-align: center;
  padding: 40px;
  color: var(--color-text-light);
}

.empty-trend i {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.3;
}

/* 排名 */
.ranking-section {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.ranking-table {
  display: flex;
  flex-direction: column;
}

.ranking-header {
  display: grid;
  grid-template-columns: 60px 1fr 80px 80px 160px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-light);
}

.ranking-row {
  display: grid;
  grid-template-columns: 60px 1fr 80px 80px 160px;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  align-items: center;
}

.ranking-row.top {
  background: rgba(107, 144, 128, 0.05);
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  background: #e0e0e0;
  color: white;
}

.rank-badge.rank-1 { background: linear-gradient(135deg, #f39c12, #e67e22); }
.rank-badge.rank-2 { background: linear-gradient(135deg, #95a5a6, #7f8c8d); }
.rank-badge.rank-3 { background: linear-gradient(135deg, #cd7f32, #b87333); }

.ranking-row .model-col {
  font-weight: 500;
  color: var(--color-text-dark);
}

.ranking-row .css-col,
.ranking-row .ars-col {
  font-weight: 600;
  color: var(--color-primary);
}

.score-badge {
  display: inline-block;
  padding: 4px 10px;
  background: var(--color-primary);
  color: white;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  margin-right: 8px;
}

:deep(.el-rate) {
  display: inline-flex;
}

/* 框架说明 */
.framework-info {
  background: white;
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.framework-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.framework-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.framework-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: linear-gradient(135deg, rgba(107, 144, 128, 0.1), rgba(107, 144, 128, 0.05));
  border-bottom: 1px solid var(--color-border);
}

.framework-header i {
  font-size: 20px;
  color: var(--color-primary);
}

.framework-header h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
}

.framework-content {
  padding: 16px;
}

.framework-content p {
  font-size: 13px;
  color: var(--color-text);
  margin-bottom: 12px;
  line-height: 1.6;
}

.framework-content ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.framework-content li {
  font-size: 12px;
  color: var(--color-text-light);
  padding: 6px 0;
  border-bottom: 1px dashed #eee;
}

.framework-content li:last-child {
  border-bottom: none;
}

.framework-content li strong {
  color: var(--color-text-dark);
}
</style>
