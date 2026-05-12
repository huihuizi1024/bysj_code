<template>
  <transition name="voting-slide">
    <div v-if="visible" class="voting-overlay" @click.self="close">
      <div class="voting-card">
        <div class="voting-header">
          <div class="voting-icon">
            <i class="fa-solid fa-hand-pointer"></i>
          </div>
          <h3>认知小投票</h3>
          <p class="voting-subtitle">帮你更了解自己的思维模式</p>
          <button class="close-btn" @click="close"><i class="fa-solid fa-xmark"></i></button>
        </div>

        <div class="voting-body" v-if="!submitted">
          <p class="question-text">{{ currentQuestion }}</p>

          <div class="options-list">
            <button
              v-for="(opt, index) in currentOptions"
              :key="index"
              :class="['option-btn', { selected: selectedOption === opt }]"
              @click="selectedOption = opt"
            >
              <span class="option-letter">{{ String.fromCharCode(65 + index) }}</span>
              <span class="option-text">{{ opt }}</span>
              <i v-if="selectedOption === opt" class="fa-solid fa-check check-icon"></i>
            </button>
          </div>

          <button
            class="submit-btn"
            :disabled="!selectedOption"
            @click="submitVote"
          >
            提交选择
          </button>
        </div>

        <div class="voting-result" v-else>
          <div class="result-icon">
            <i class="fa-solid fa-check-circle"></i>
          </div>
          <h4>感谢你的分享</h4>
          <p class="result-text">
            你的选择是：<strong>{{ selectedOption }}</strong>
          </p>
          <p class="result-tip">
            这种思考方式很常见。记住，觉察是改变的第一步。
          </p>
          <button class="done-btn" @click="close">我知道了</button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import { submitVote as apiSubmitVote, getNextQuestion } from '@/api/voting'

const props = defineProps({
  visible: { type: Boolean, default: false },
  emotionType: { type: String, default: 'neutral' },
  recentVotingType: { type: String, default: '' }
})

const emit = defineEmits(['close', 'submitted'])

const currentQuestion = ref('')
const currentOptions = ref([])
const currentVotingType = ref('')
const selectedOption = ref('')
const submitted = ref(false)

const loadQuestion = async () => {
  try {
    const res = await getNextQuestion(props.emotionType, props.recentVotingType)
    if (res.data) {
      currentQuestion.value = res.data.question
      currentOptions.value = res.data.options || []
      currentVotingType.value = res.data.type || ''
    }
  } catch (err) {
    console.error('加载投票问题失败', err)
    // 备用问题
    currentQuestion.value = '下面哪种想法更符合你的思考方式？'
    currentOptions.value = [
      '全或无思维：事情不是完美就是彻底失败',
      '灾难化思维：最坏的情况一定会发生',
      '读心术：我知道别人一定在评判我'
    ]
    currentVotingType.value = 'thought_distortion'
  }
}

const submitVote = async () => {
  if (!selectedOption.value) return
  try {
    await apiSubmitVote({
      votingType: currentVotingType.value,
      question: currentQuestion.value,
      selectedOption: selectedOption.value
    })
    submitted.value = true
    emit('submitted', { type: currentVotingType.value, option: selectedOption.value })
  } catch (err) {
    console.error('投票提交失败', err)
    submitted.value = true // 即使失败也显示成功
  }
}

const close = () => {
  submitted.value = false
  selectedOption.value = ''
  emit('close')
}

watch(() => props.visible, (val) => {
  if (val) {
    submitted.value = false
    selectedOption.value = ''
    loadQuestion()
  }
})
</script>

<style scoped>
.voting-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.voting-card {
  background: white;
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.voting-header {
  background: linear-gradient(135deg, #6b9080, #a4c3b2);
  padding: 24px;
  text-align: center;
  position: relative;
  color: white;
}

.voting-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-size: 20px;
}

.voting-header h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.voting-subtitle {
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

.voting-body { padding: 24px; }

.question-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
  line-height: 1.5;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.option-btn:hover {
  border-color: var(--color-primary);
  background: rgba(107, 144, 128, 0.05);
}

.option-btn.selected {
  border-color: var(--color-primary);
  background: rgba(107, 144, 128, 0.08);
}

.option-letter {
  width: 26px;
  height: 26px;
  background: rgba(107, 144, 128, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-primary);
  flex-shrink: 0;
}

.option-text {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-dark);
  line-height: 1.4;
}

.check-icon {
  color: var(--color-primary);
  font-size: 14px;
  flex-shrink: 0;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) { background: #5a7d6f; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.voting-result {
  padding: 32px 24px;
  text-align: center;
}

.result-icon {
  font-size: 48px;
  color: #2ecc71;
  margin-bottom: 12px;
}

.voting-result h4 {
  font-size: 18px;
  color: var(--color-text-dark);
  margin-bottom: 8px;
}

.result-text {
  font-size: 14px;
  color: var(--color-text);
  margin-bottom: 6px;
}

.result-tip {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 20px;
}

.done-btn {
  padding: 10px 32px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
}

/* Transition */
.voting-slide-enter-active, .voting-slide-leave-active {
  transition: opacity 0.3s;
}
.voting-slide-enter-from, .voting-slide-leave-to {
  opacity: 0;
}
.voting-slide-enter-active .voting-card {
  animation: cardIn 0.3s ease;
}
@keyframes cardIn {
  from { transform: scale(0.9) translateY(20px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}
</style>
