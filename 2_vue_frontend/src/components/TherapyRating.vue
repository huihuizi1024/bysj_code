<template>
  <transition name="rating-slide">
    <div v-if="visible" class="rating-overlay" @click.self="close">
      <div class="rating-card">
        <!-- 头部 -->
        <div class="rating-header">
          <div class="rating-icon">
            <i class="fa-solid fa-star"></i>
          </div>
          <h3>评价这次对话</h3>
          <p class="rating-subtitle">你的反馈能帮助我们优化 AI 的回复质量</p>
          <button class="close-btn" @click="close"><i class="fa-solid fa-xmark"></i></button>
        </div>

        <!-- 评分内容 -->
        <div class="rating-body" v-if="!submitted">
          <!-- MentalAlign 评分维度 -->
          <div class="rating-section">
            <h4 class="section-title">
              <i class="fa-solid fa-brain"></i>
              MentalAlign 疗效评估
            </h4>
            <p class="section-desc">评估 AI 回复在认知支持和情感共鸣方面的表现</p>

            <!-- CSS 评分 -->
            <div class="score-item">
              <div class="score-label">
                <span>认知支持得分 (CSS)</span>
                <span class="score-hint">引导性 · 信息量 · 专业性</span>
              </div>
              <div class="star-rating">
                <button
                  v-for="star in 5"
                  :key="'css-' + star"
                  :class="['star-btn', { active: cssRating >= star }]"
                  @click="cssRating = star"
                >
                  <i :class="cssRating >= star ? 'fa-solid fa-star' : 'fa-regular fa-star'"></i>
                </button>
                <span class="score-value">{{ cssRating }}.0 / 5.0</span>
              </div>
            </div>

            <!-- ARS 评分 -->
            <div class="score-item">
              <div class="score-label">
                <span>情感共鸣得分 (ARS)</span>
                <span class="score-hint">共情表达 · 温暖感 · 安全感</span>
              </div>
              <div class="star-rating">
                <button
                  v-for="star in 5"
                  :key="'ars-' + star"
                  :class="['star-btn', { active: arsRating >= star }]"
                  @click="arsRating = star"
                >
                  <i :class="arsRating >= star ? 'fa-solid fa-star' : 'fa-regular fa-star'"></i>
                </button>
                <span class="score-value">{{ arsRating }}.0 / 5.0</span>
              </div>
            </div>
          </div>

          <!-- 总体满意度 -->
          <div class="rating-section">
            <h4 class="section-title">
              <i class="fa-solid fa-heart"></i>
              总体满意度
            </h4>

            <div class="overall-rating">
              <button
                v-for="star in 5"
                :key="'overall-' + star"
                :class="['star-btn large', { active: overallRating >= star }]"
                @click="overallRating = star"
              >
                <i :class="overallRating >= star ? 'fa-solid fa-star' : 'fa-regular fa-star'"></i>
              </button>
            </div>
            <p class="rating-text">{{ ratingText }}</p>
          </div>

          <!-- 提交按钮 -->
          <button
            class="submit-btn"
            :disabled="overallRating === 0"
            @click="submitRating"
          >
            提交评价
          </button>
        </div>

        <!-- 提交成功 -->
        <div class="rating-result" v-else>
          <div class="result-icon">
            <i class="fa-solid fa-check-circle"></i>
          </div>
          <h4>感谢你的反馈</h4>
          <p class="result-text">
            你的评分将帮助我们持续优化 AI 的心理支持能力
          </p>
          <div class="result-scores">
            <div class="result-item">
              <span class="result-label">CSS 评分</span>
              <span class="result-value">{{ cssRating }}/5</span>
            </div>
            <div class="result-item">
              <span class="result-label">ARS 评分</span>
              <span class="result-value">{{ arsRating }}/5</span>
            </div>
            <div class="result-item">
              <span class="result-label">总体评分</span>
              <span class="result-value">{{ overallRating }}/5</span>
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
import { submitUserRating } from '@/api/evaluation'

const props = defineProps({
  visible: { type: Boolean, default: false },
  sessionId: { type: Number, default: 0 },
  messageId: { type: Number, default: 0 }
})

const emit = defineEmits(['close', 'submitted'])

// 评分状态
const cssRating = ref(0)
const arsRating = ref(0)
const overallRating = ref(0)
const submitted = ref(false)

// 评分文字描述
const ratingText = computed(() => {
  const ratings = ['', '不太满意', '一般', '还不错', '满意', '非常满意']
  return ratings[overallRating.value] || ''
})

// 提交评价
const submitRating = async () => {
  if (overallRating.value === 0) return

  try {
    // 将 5 星制转换为 0-5 的分数
    const cssScore = cssRating.value
    const arsScore = arsRating.value
    const overallScore = overallRating.value

    await submitUserRating({
      sessionId: props.sessionId,
      messageId: props.messageId,
      rating: overallScore,
      userCss: cssRating.value,
      userArs: arsRating.value
    })

    submitted.value = true
    emit('submitted', {
      cssScore,
      arsScore,
      overallScore
    })
  } catch (err) {
    console.error('提交评分失败', err)
    // 即使 API 失败也显示成功
    submitted.value = true
    emit('submitted', {
      cssScore: cssRating.value,
      arsScore: arsRating.value,
      overallScore: overallRating.value
    })
  }
}

// 关闭弹窗
const close = () => {
  submitted.value = false
  cssRating.value = 0
  arsRating.value = 0
  overallRating.value = 0
  emit('close')
}

// 监听显示状态，重置表单
watch(() => props.visible, (val) => {
  if (val) {
    submitted.value = false
    cssRating.value = 0
    arsRating.value = 0
    overallRating.value = 0
  }
})
</script>

<style scoped>
.rating-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.rating-card {
  background: white;
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.rating-header {
  background: linear-gradient(135deg, #6b9080, #a4c3b2);
  padding: 24px;
  text-align: center;
  position: relative;
  color: white;
}

.rating-icon {
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

.rating-header h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.rating-subtitle {
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
  font-size: 13px;
}

.rating-body {
  padding: 24px;
}

.rating-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-title i {
  color: var(--color-primary);
}

.section-desc {
  font-size: 12px;
  color: var(--color-text-light);
  margin-bottom: 16px;
}

.score-item {
  margin-bottom: 16px;
}

.score-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.score-label span:first-child {
  font-weight: 500;
  color: var(--color-text-dark);
}

.score-hint {
  font-size: 11px;
  color: var(--color-text-light);
}

.star-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star-btn {
  background: none;
  border: none;
  font-size: 22px;
  color: #ddd;
  cursor: pointer;
  transition: all 0.2s;
  padding: 2px;
}

.star-btn:hover,
.star-btn.active {
  color: #f39c12;
  transform: scale(1.1);
}

.star-btn.large {
  font-size: 32px;
}

.score-value {
  margin-left: 12px;
  font-size: 13px;
  color: var(--color-text-light);
}

.overall-rating {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.rating-text {
  text-align: center;
  font-size: 14px;
  color: var(--color-text);
  margin-bottom: 20px;
}

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

.submit-btn:hover:not(:disabled) {
  background: #5a7d6f;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 结果页面 */
.rating-result {
  padding: 32px 24px;
  text-align: center;
}

.result-icon {
  font-size: 56px;
  color: #2ecc71;
  margin-bottom: 16px;
}

.rating-result h4 {
  font-size: 20px;
  color: var(--color-text-dark);
  margin-bottom: 8px;
}

.result-text {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 24px;
}

.result-scores {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 24px;
}

.result-item {
  text-align: center;
}

.result-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-light);
  margin-bottom: 4px;
}

.result-value {
  font-size: 18px;
  font-weight: 600;
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
.rating-slide-enter-active,
.rating-slide-leave-active {
  transition: opacity 0.3s;
}

.rating-slide-enter-from,
.rating-slide-leave-to {
  opacity: 0;
}

.rating-slide-enter-active .rating-card {
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
