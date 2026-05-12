<template>
  <transition name="survey-slide">
    <div v-if="visible" class="survey-overlay" @click.self="close">
      <div class="survey-card">
        <!-- 头部 -->
        <div class="survey-header">
          <div class="survey-icon">
            <i class="fa-solid fa-chart-radar"></i>
          </div>
          <h3>HEART 用户体验调查</h3>
          <p class="survey-subtitle">帮助我们了解你的使用体验</p>
          <button class="close-btn" @click="close"><i class="fa-solid fa-xmark"></i></button>
        </div>

        <!-- 调查内容 -->
        <div class="survey-body" v-if="!submitted">
          <!-- HEART 五维度评分 -->
          <div class="rating-section">
            <div class="rating-item">
              <div class="rating-label">
                <div class="label-left">
                  <i class="fa-solid fa-face-smile"></i>
                  <span>Happiness 满意度</span>
                </div>
                <span class="rating-value">{{ formatValue(happiness) }}</span>
              </div>
              <el-slider v-model="happiness" :min="0" :max="5" :step="0.5" :show-tooltip="false" />
              <div class="scale-hint">
                <span>不满意</span>
                <span>满意</span>
              </div>
            </div>

            <div class="rating-item">
              <div class="rating-label">
                <div class="label-left">
                  <i class="fa-solid fa-comments"></i>
                  <span>Engagement 参与度</span>
                </div>
                <span class="rating-value">{{ formatPercent(engagement) }}</span>
              </div>
              <el-slider v-model="engagement" :min="0" :max="1" :step="0.1" :show-tooltip="false" />
              <div class="scale-hint">
                <span>低</span>
                <span>高</span>
              </div>
            </div>

            <div class="rating-item">
              <div class="rating-label">
                <div class="label-left">
                  <i class="fa-solid fa-handshake"></i>
                  <span>Adoption 接受度</span>
                </div>
                <span class="rating-value">{{ formatPercent(adoption) }}</span>
              </div>
              <el-slider v-model="adoption" :min="0" :max="1" :step="0.1" :show-tooltip="false" />
              <div class="scale-hint">
                <span>不愿意</span>
                <span>愿意</span>
              </div>
            </div>

            <div class="rating-item">
              <div class="rating-label">
                <div class="label-left">
                  <i class="fa-solid fa-rotate"></i>
                  <span>Retention 留存意愿</span>
                </div>
                <span class="rating-value">{{ formatPercent(retention) }}</span>
              </div>
              <el-slider v-model="retention" :min="0" :max="1" :step="0.1" :show-tooltip="false" />
              <div class="scale-hint">
                <span>不考虑</span>
                <span>会推荐</span>
              </div>
            </div>

            <div class="rating-item">
              <div class="rating-label">
                <div class="label-left">
                  <i class="fa-solid fa-check-circle"></i>
                  <span>Task Success 任务成功</span>
                </div>
                <span class="rating-value">{{ formatPercent(taskSuccess) }}</span>
              </div>
              <el-slider v-model="taskSuccess" :min="0" :max="1" :step="0.1" :show-tooltip="false" />
              <div class="scale-hint">
                <span>未解决</span>
                <span>完全解决</span>
              </div>
            </div>
          </div>

          <!-- 综合评分 -->
          <div class="overall-section">
            <div class="overall-label">综合评分</div>
            <div class="overall-stars">
              <button
                v-for="star in 5"
                :key="'overall-' + star"
                :class="['star-btn', { active: overallRating >= star }]"
                @click="overallRating = star"
              >
                <i :class="overallRating >= star ? 'fa-solid fa-star' : 'fa-regular fa-star'"></i>
              </button>
            </div>
            <span class="overall-text">{{ ratingText }}</span>
          </div>

          <!-- 文字反馈 -->
          <div class="feedback-section">
            <textarea
              v-model="comment"
              class="feedback-input"
              placeholder="有什么想说的吗？（可选）"
              rows="3"
            ></textarea>
          </div>

          <!-- 提交按钮 -->
          <button class="submit-btn" @click="submitSurvey">
            提交反馈
          </button>
        </div>

        <!-- 提交成功 -->
        <div class="survey-result" v-else>
          <div class="result-icon">
            <i class="fa-solid fa-heart"></i>
          </div>
          <h4>感谢你的反馈</h4>
          <p class="result-text">
            你的反馈将帮助我们不断提升系统的用户体验
          </p>
          <div class="result-summary">
            <div class="summary-item">
              <div class="summary-label">HEART 综合得分</div>
              <div class="summary-value">{{ overallScore.toFixed(1) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">满意度</div>
              <div class="summary-value">{{ happiness.toFixed(1) }}</div>
            </div>
          </div>
          <button class="done-btn" @click="close">完成</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitSatisfaction, submitQuickSatisfaction } from '@/api/evaluation'

const props = defineProps({
  visible: { type: Boolean, default: false },
  sessionId: { type: Number, default: 0 },
  modelCode: { type: String, default: '' }
})

const emit = defineEmits(['close', 'submitted'])

// HEART 评分状态
const happiness = ref(3.0)
const engagement = ref(0.7)
const adoption = ref(0.7)
const retention = ref(0.7)
const taskSuccess = ref(0.7)
const overallRating = ref(0)
const comment = ref('')
const submitted = ref(false)

// 计算综合得分
const overallScore = computed(() => {
  return (happiness.value / 5.0 * 0.3 +
    engagement.value * 0.15 +
    adoption.value * 0.15 +
    retention.value * 0.20 +
    taskSuccess.value * 0.20) * 5
})

// 评分文字描述
const ratingText = computed(() => {
  const texts = ['', '非常不满意', '不满意', '一般', '满意', '非常满意']
  return texts[overallRating.value] || ''
})

// 格式化数值
const formatValue = (val) => val.toFixed(1)
const formatPercent = (val) => Math.round(val * 100) + '%'

// 提交调查
const submitSurvey = async () => {
  try {
    // 使用完整版提交
    await submitSatisfaction({
      sessionId: props.sessionId,
      modelCode: props.modelCode,
      happiness: happiness.value,
      engagement: engagement.value,
      adoption: adoption.value,
      retention: retention.value,
      taskSuccess: taskSuccess.value,
      comment: comment.value || null
    })

    submitted.value = true
    emit('submitted', {
      happiness: happiness.value,
      engagement: engagement.value,
      adoption: adoption.value,
      retention: retention.value,
      taskSuccess: taskSuccess.value,
      overallScore: overallScore.value
    })
  } catch (err) {
    console.error('提交满意度失败', err)
    ElMessage.error('提交失败，请重试')
  }
}

// 关闭弹窗
const close = () => {
  submitted.value = false
  happiness.value = 3.0
  engagement.value = 0.7
  adoption.value = 0.7
  retention.value = 0.7
  taskSuccess.value = 0.7
  overallRating.value = 0
  comment.value = ''
  emit('close')
}

// 监听显示状态
watch(() => props.visible, (val) => {
  if (val) {
    submitted.value = false
  }
})
</script>

<style scoped>
.survey-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.survey-card {
  background: white;
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 460px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.survey-header {
  background: linear-gradient(135deg, #4a6fa5, #6b8cba);
  padding: 24px;
  text-align: center;
  position: relative;
  color: white;
}

.survey-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 24px;
}

.survey-header h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.survey-subtitle {
  font-size: 13px;
  opacity: 0.85;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.survey-body {
  padding: 24px;
}

.rating-section {
  margin-bottom: 20px;
}

.rating-item {
  margin-bottom: 20px;
}

.rating-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.label-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-dark);
}

.label-left i {
  color: var(--color-primary);
}

.rating-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
  min-width: 40px;
  text-align: right;
}

.scale-hint {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-text-light);
  margin-top: 4px;
}

/* Element Plus 滑块样式覆盖 */
:deep(.el-slider__runway) {
  height: 6px;
  border-radius: 3px;
  background: #e0e0e0;
}

:deep(.el-slider__bar) {
  height: 6px;
  border-radius: 3px;
  background: var(--color-primary);
}

:deep(.el-slider__button) {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-primary);
}

/* 综合评分 */
.overall-section {
  text-align: center;
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(107, 144, 128, 0.08);
  border-radius: var(--radius-md);
}

.overall-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-dark);
  margin-bottom: 12px;
}

.overall-stars {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-bottom: 8px;
}

.star-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #ddd;
  cursor: pointer;
  transition: all 0.2s;
}

.star-btn:hover,
.star-btn.active {
  color: #f39c12;
  transform: scale(1.15);
}

.overall-text {
  font-size: 13px;
  color: var(--color-text);
}

/* 文字反馈 */
.feedback-section {
  margin-bottom: 20px;
}

.feedback-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  resize: vertical;
  outline: none;
  font-family: inherit;
}

.feedback-input:focus {
  border-color: var(--color-primary);
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 14px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover {
  background: #5a7d6f;
}

/* 结果页面 */
.survey-result {
  padding: 40px 24px;
  text-align: center;
}

.result-icon {
  font-size: 64px;
  color: #e74c3c;
  margin-bottom: 16px;
}

.survey-result h4 {
  font-size: 20px;
  color: var(--color-text-dark);
  margin-bottom: 8px;
}

.result-text {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 24px;
}

.result-summary {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 24px;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 12px;
  color: var(--color-text-light);
  margin-bottom: 4px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
}

.done-btn {
  padding: 12px 40px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
}

/* 过渡动画 */
.survey-slide-enter-active,
.survey-slide-leave-active {
  transition: opacity 0.3s;
}

.survey-slide-enter-from,
.survey-slide-leave-to {
  opacity: 0;
}

.survey-slide-enter-active .survey-card {
  animation: cardIn 0.3s ease;
}

@keyframes cardIn {
  from {
    transform: scale(0.9) translateY(20px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}
</style>
