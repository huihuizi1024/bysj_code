<template>
  <el-drawer
    v-model="appStore.emotionDrawerVisible"
    title="情绪分析报告"
    direction="rtl"
    size="500px"
  >
    <template #header>
      <div class="drawer-header">
        <span class="title">📊 情绪分析报告</span>
      </div>
    </template>

    <div class="emotion-content">
      <!-- 时间范围选择 -->
      <div class="time-range">
        <el-radio-group v-model="days" size="default" @change="loadData">
          <el-radio-button value="7">7天</el-radio-button>
          <el-radio-button value="30">30天</el-radio-button>
          <el-radio-button value="90">全部</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 情绪趋势图 -->
      <div class="chart-container">
        <EmotionChart :data="chartData" :loading="loading" />
      </div>

      <!-- 情绪统计 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon">📈</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.avgScore }}</div>
            <div class="stat-label">平均情绪得分</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">💬</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalRecords }}</div>
            <div class="stat-label">情绪记录数</div>
          </div>
        </div>
      </div>

      <!-- 情绪分布 -->
      <div class="distribution-section">
        <h3>情绪类型分布</h3>
        <div class="distribution-bars">
          <div
            v-for="(item, index) in distribution"
            :key="index"
            class="distribution-item"
          >
            <div class="distribution-label">
              <span>{{ emotionLabels[item.type] || item.type }}</span>
              <span>{{ item.count }}次</span>
            </div>
            <el-progress
              :percentage="item.percentage"
              :color="emotionColors[item.type] || '#909399'"
              :show-text="false"
            />
          </div>
        </div>
      </div>

      <!-- 近期情绪记录 -->
      <div class="recent-records">
        <h3>📋 近期情绪记录</h3>
        <div v-if="recentRecords.length === 0" class="empty-state">
          <el-empty description="暂无情绪记录" :image-size="80" />
        </div>
        <div v-else class="record-list">
          <div
            v-for="record in recentRecords"
            :key="record.id"
            class="record-item"
          >
            <div class="record-time">{{ formatTime(record.analysisTime) }}</div>
            <span :class="['emotion-tag', record.emotionType]">
              {{ emotionLabels[record.emotionType] || record.emotionType }}
            </span>
            <div class="record-score">得分: {{ record.emotionScore }}</div>
            <div class="record-keywords" v-if="record.keywords">
              {{ record.keywords }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAppStore } from '@/stores/app'
import EmotionChart from './EmotionChart.vue'
import { getEmotionTrend } from '@/api/emotion'

const appStore = useAppStore()

const days = ref(7)
const loading = ref(false)
const emotionData = ref([])

const emotionLabels = {
  positive: '😊 积极',
  negative: '😢 消极',
  anxiety: '😰 焦虑',
  depression: '😔 抑郁',
  anger: '😠 愤怒',
  neutral: '😐 中性'
}

const emotionColors = {
  positive: '#67C23A',
  negative: '#E57373',
  anxiety: '#E6A23C',
  depression: '#7C9CB5',
  anger: '#F56C6C',
  neutral: '#909399'
}

// 图表数据
const chartData = computed(() => {
  return emotionData.value.map(item => ({
    date: formatDate(item.analysisTime),
    score: item.emotionScore,
    type: item.emotionType
  }))
})

// 统计数据
const stats = computed(() => {
  if (emotionData.value.length === 0) {
    return { avgScore: '0.00', totalRecords: 0 }
  }
  const total = emotionData.value.reduce((sum, item) => sum + item.emotionScore, 0)
  return {
    avgScore: (total / emotionData.value.length).toFixed(2),
    totalRecords: emotionData.value.length
  }
})

// 情绪分布
const distribution = computed(() => {
  const counts = {}
  emotionData.value.forEach(item => {
    const type = item.emotionType || 'neutral'
    counts[type] = (counts[type] || 0) + 1
  })

  const total = emotionData.value.length || 1
  return Object.entries(counts).map(([type, count]) => ({
    type,
    count,
    percentage: Math.round((count / total) * 100)
  }))
})

// 近期记录（最近10条）
const recentRecords = computed(() => {
  return emotionData.value
    .slice()
    .sort((a, b) => new Date(b.analysisTime) - new Date(a.analysisTime))
    .slice(0, 10)
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getEmotionTrend(days.value)
    emotionData.value = res.data || []
  } catch (err) {
    console.error('加载情绪数据失败', err)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 抽屉打开时加载数据
watch(() => appStore.emotionDrawerVisible, (visible) => {
  if (visible) {
    loadData()
  }
})
</script>

<style scoped>
.emotion-content {
  padding: 0 4px;
}

.drawer-header .title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-dark);
}

.time-range {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.chart-container {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.stats-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  font-size: 28px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-light);
}

.distribution-section {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.distribution-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
}

.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.distribution-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-light);
}

.recent-records {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.recent-records h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
}

.empty-state {
  padding: 20px 0;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 10px;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.record-time {
  font-size: 12px;
  color: var(--color-text-light);
  min-width: 100px;
}

.emotion-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.emotion-tag.positive { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
.emotion-tag.negative { background: rgba(229, 115, 115, 0.1); color: #E57373; }
.emotion-tag.anxiety { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
.emotion-tag.depression { background: rgba(124, 156, 181, 0.1); color: #7C9CB5; }
.emotion-tag.anger { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
.emotion-tag.neutral { background: rgba(144, 147, 153, 0.1); color: #909399; }

.record-score {
  font-size: 12px;
  color: var(--color-text-light);
}

.record-keywords {
  width: 100%;
  font-size: 11px;
  color: var(--color-text-light);
  padding-top: 4px;
  border-top: 1px dashed rgba(0,0,0,0.05);
}
</style>
