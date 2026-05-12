<template>
  <div class="profile-tag-panel">
    <h4><i class="fa-solid fa-user-circle"></i> 心理画像</h4>

    <!-- 压力等级 -->
    <div class="tag-row">
      <span class="tag-label">压力等级</span>
      <span :class="['tag-value', stressTagClass]">
        <i :class="stressIcon"></i>
        {{ stressLabel }}
      </span>
    </div>

    <!-- 情绪趋势（可点击跳转） -->
    <div class="tag-row clickable" @click="$emit('jump-report')">
      <span class="tag-label">情绪趋势</span>
      <span :class="['tag-value', trendTagClass]">
        <i :class="trendIcon"></i>
        {{ trendLabel }}
        <i class="fa-solid fa-chevron-right" style="font-size: 10px; margin-left: 2px; opacity: 0.5;"></i>
      </span>
    </div>

    <!-- 打卡连续天数 -->
    <div class="tag-row">
      <span class="tag-label">连续打卡</span>
      <span class="tag-value tag-streak">
        <i class="fa-solid fa-fire" style="color: #ff6b6b"></i>
        {{ streak }}天
      </span>
    </div>

    <!-- 参与度 -->
    <div class="tag-row">
      <span class="tag-label">活跃度</span>
      <div class="activity-bar-wrap">
        <div class="activity-bar">
          <div class="activity-fill" :style="{ width: activityScore * 100 + '%' }"></div>
        </div>
        <span class="activity-pct">{{ Math.round(activityScore * 100) }}%</span>
      </div>
    </div>

    <!-- 画像更新时间 -->
    <div class="tag-footer" v-if="updatedAt">
      <i class="fa-regular fa-clock"></i>
      {{ updatedAt }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStreak, getCheckinStats } from '@/api/checkin'

const props = defineProps({
  stressLevel: { type: String, default: 'medium' }, // low / medium / high
  emotionalTrend: { type: String, default: 'stable' } // rising / falling / stable
})

defineEmits(['jump-report'])

const streak = ref(0)
const activityScore = ref(0.5)
const updatedAt = ref('')

const stressLabel = computed(() => ({ low: '低', medium: '中', high: '高' }[props.stressLevel] || '中'))
const stressIcon = computed(() => ({
  low: 'fa-solid fa-leaf',
  medium: 'fa-solid fa-fire-flame-curved',
  high: 'fa-solid fa-triangle-exclamation'
}[props.stressLevel] || 'fa-solid fa-fire-flame-curved'))
const stressTagClass = computed(() => ({
  low: 'tag-low',
  medium: 'tag-medium',
  high: 'tag-high'
}[props.stressLevel] || 'tag-medium'))

const trendLabel = computed(() => ({
  rising: '好转 ↑',
  falling: '下降 ↓',
  stable: '稳定 →'
}[props.emotionalTrend] || '稳定 →'))
const trendIcon = computed(() => ({
  rising: 'fa-solid fa-arrow-trend-up',
  falling: 'fa-solid fa-arrow-trend-down',
  stable: 'fa-solid fa-minus'
}[props.emotionalTrend] || 'fa-solid fa-minus'))
const trendTagClass = computed(() => ({
  rising: 'tag-rising',
  falling: 'tag-falling',
  stable: 'tag-stable'
}[props.emotionalTrend] || 'tag-stable'))

const loadProfile = async () => {
  try {
    const [streakRes, statsRes] = await Promise.all([
      getStreak(),
      getCheckinStats(7)
    ])
    streak.value = streakRes.data?.streak || 0
    const scoreStr = statsRes.data?.activeScore || '0.5'
    activityScore.value = parseFloat(scoreStr)
    const now = new Date()
    updatedAt.value = `${now.getMonth() + 1}/${now.getDate()} ${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`
  } catch (err) {
    console.error('加载画像失败', err)
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-tag-panel {
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-md);
  padding: 14px;
  border: 1px solid var(--color-border);
}

.profile-tag-panel h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-dark);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
}

.tag-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.tag-row:last-of-type { border-bottom: none; }

.tag-row.clickable {
  cursor: pointer;
  border-radius: 6px;
  padding: 6px 4px;
  transition: background 0.2s;
}

.tag-row.clickable:hover {
  background: rgba(107, 144, 128, 0.08);
}

.tag-label {
  font-size: 12px;
  color: var(--color-text-light);
  min-width: 60px;
}

.tag-value {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-low { background: rgba(46, 204, 113, 0.12); color: #27ae60; }
.tag-medium { background: rgba(243, 156, 18, 0.12); color: #e67e22; }
.tag-high { background: rgba(231, 76, 60, 0.12); color: #c0392b; }

.tag-rising { background: rgba(46, 204, 113, 0.12); color: #27ae60; }
.tag-falling { background: rgba(231, 76, 60, 0.12); color: #c0392b; }
.tag-stable { background: rgba(52, 152, 219, 0.12); color: #2980b9; }

.tag-streak { background: rgba(255, 107, 107, 0.1); color: #e74c3c; }

.activity-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: flex-end;
}

.activity-bar {
  width: 80px;
  height: 6px;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 3px;
  overflow: hidden;
}

.activity-fill {
  height: 100%;
  background: linear-gradient(90deg, #6b9080, #a4c3b2);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.activity-pct {
  font-size: 11px;
  color: var(--color-text-light);
  min-width: 30px;
  text-align: right;
}

.tag-footer {
  margin-top: 8px;
  font-size: 10px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
}
</style>
