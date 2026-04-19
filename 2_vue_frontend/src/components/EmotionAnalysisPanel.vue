<template>
  <aside class="emotion-panel glass-panel">
    <div class="panel-header">
      <span class="header-title">
        <i class="fa-solid fa-brain"></i>
        实时分析
      </span>
      <span class="badge">AI Copilot</span>
    </div>

    <div class="panel-content">
      <!-- 情绪状态侦测 -->
      <div class="section">
        <h3 class="section-title">
          <i class="fa-solid fa-wave-square"></i>
          情绪状态侦测
        </h3>
        <div class="emotion-bars">
          <div class="emotion-item">
            <span class="emotion-label">平静</span>
            <div class="bar-track">
              <div
                class="bar-fill calm"
                :style="{ width: emotionData.calm + '%' }"
              ></div>
            </div>
            <span class="emotion-value">{{ emotionData.calm }}%</span>
          </div>
          <div class="emotion-item">
            <span class="emotion-label">焦虑</span>
            <div class="bar-track">
              <div
                class="bar-fill anxious"
                :style="{ width: emotionData.anxious + '%' }"
              ></div>
            </div>
            <span class="emotion-value">{{ emotionData.anxious }}%</span>
          </div>
          <div class="emotion-item">
            <span class="emotion-label">低落</span>
            <div class="bar-track">
              <div
                class="bar-fill sad"
                :style="{ width: emotionData.sad + '%' }"
              ></div>
            </div>
            <span class="emotion-value">{{ emotionData.sad }}%</span>
          </div>
        </div>
      </div>

      <!-- 对话备忘录 -->
      <div class="section memo-card">
        <h3 class="section-title">
          <i class="fa-solid fa-pen-to-square"></i>
          对话备忘录
        </h3>
        <div class="memo-content">
          <p v-if="!memoItems.length" class="memo-placeholder">
            AI正在倾听并提取关键信息...
          </p>
          <ul v-else class="memo-list">
            <li v-for="(item, idx) in memoItems" :key="idx" class="memo-item">
              {{ item }}
            </li>
          </ul>
        </div>
      </div>

      <!-- AI 辅导建议 -->
      <div class="section advice-card">
        <h3 class="section-title advice-title">
          <i class="fa-solid fa-lightbulb"></i>
          心理辅导建议
        </h3>
        <p class="advice-content" v-html="adviceText"></p>
      </div>
    </div>

    <!-- 危机热线 -->
    <div class="sos-section">
      <button class="sos-btn" @click="handleSOS">
        <i class="fa-solid fa-phone-volume"></i>
        危机干预热线
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const props = defineProps({
  // 情绪数据：0-100
  calm: { type: Number, default: 70 },
  anxious: { type: Number, default: 30 },
  sad: { type: Number, default: 20 }
})

const emotionData = reactive({
  calm: props.calm,
  anxious: props.anxious,
  sad: props.sad
})

const memoItems = ref([])
const adviceText = ref('随着对话进行，将在这里提供针对性的CBT（认知行为疗法）干预建议。')

// 暴露更新方法供父组件调用
const updateEmotion = (calm, anxious, sad) => {
  emotionData.calm = calm
  emotionData.anxious = anxious
  emotionData.sad = sad
}

const updateMemo = (items) => {
  memoItems.value = items
}

const updateAdvice = (text) => {
  adviceText.value = text
}

// SOS 按钮
const handleSOS = () => {
  appStore.openCrisisAlert()
}

defineExpose({ updateEmotion, updateMemo, updateAdvice })
</script>

<style scoped>
.emotion-panel {
  width: 320px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(107, 144, 128, 0.1);
  flex-shrink: 0;
}

.header-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text-dark);
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title i {
  color: var(--color-primary);
  font-size: 14px;
}

.badge {
  background: var(--color-primary);
  color: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-content::-webkit-scrollbar {
  width: 4px;
}

.panel-content::-webkit-scrollbar-thumb {
  background: rgba(107, 144, 128, 0.2);
  border-radius: 2px;
}

/* Section */
.section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 12px;
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title i {
  font-size: 12px;
  color: var(--color-primary-light);
}

/* Emotion Bars */
.emotion-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.emotion-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.emotion-label {
  width: 38px;
  font-size: 13px;
  color: var(--color-text);
  font-weight: 500;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 6px;
  background: #e8efed;
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.bar-fill.calm {
  background: linear-gradient(90deg, #2ecc71, #58d68d);
}

.bar-fill.anxious {
  background: linear-gradient(90deg, #f39c12, #f5b041);
}

.bar-fill.sad {
  background: linear-gradient(90deg, #3498db, #5dade2);
}

.emotion-value {
  width: 36px;
  font-size: 12px;
  color: var(--color-text-light);
  text-align: right;
  flex-shrink: 0;
}

/* Memo Card */
.memo-card {
  background: #fff;
  border: 1px solid rgba(107, 144, 128, 0.1);
  border-radius: var(--radius-md);
  padding: 14px;
}

.memo-content {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text);
}

.memo-placeholder {
  color: #95a5a6;
  font-style: italic;
  font-size: 12px;
}

.memo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.memo-item {
  position: relative;
  padding-left: 16px;
  font-size: 13px;
  color: var(--color-text);
  line-height: 1.5;
}

.memo-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 7px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-primary-light);
}

/* Advice Card */
.advice-card {
  background: #fdfbf7;
  border: 1px solid #f1e4c3;
  border-radius: var(--radius-md);
  padding: 14px;
}

.advice-title {
  color: #d35400 !important;
}

.advice-title i {
  color: #d35400 !important;
}

.advice-content {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-light);
}

/* SOS */
.sos-section {
  padding: 16px 20px;
  flex-shrink: 0;
}

.sos-btn {
  width: 100%;
  background: #fff0f0;
  color: #e74c3c;
  border: 1px dashed #ffb7b2;
  padding: 12px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition-base);
}

.sos-btn:hover {
  background: #ffe4e4;
  border-color: #e74c3c;
  transform: translateY(-1px);
}

.sos-btn i {
  font-size: 14px;
}
</style>
