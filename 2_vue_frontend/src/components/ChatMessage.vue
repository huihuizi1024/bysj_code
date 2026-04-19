<template>
  <div :class="['message-item', message.role === 'user' ? 'is-user' : 'is-assistant', { 'is-crisis': isCrisis }]">
    <!-- AI 消息 -->
    <div v-if="message.role === 'assistant'" class="content-wrapper">
      <!-- 情绪标签 -->
      <div v-if="message.emotionType" class="emotion-tag-row">
        <span :class="['emotion-tag', message.emotionType]">
          {{ emotionLabels[message.emotionType] || message.emotionType }}
        </span>
      </div>
      <!-- 消息气泡 -->
      <div class="message-bubble ai-bubble">
        <div class="message-text" v-html="formattedContent"></div>
        <div class="message-time">{{ formatTime(message.createTime) }}</div>
      </div>
    </div>

    <!-- 用户消息 -->
    <div v-else class="content-wrapper user-content">
      <div :class="['message-bubble', 'user-bubble', { 'is-crisis': isCrisis }]">
        <div class="message-text" v-html="formattedContent"></div>
        <div class="message-time">{{ formatTime(message.createTime) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['crisis-detected'])

const emotionLabels = {
  positive: '积极',
  negative: '消极',
  anxiety: '焦虑',
  depression: '抑郁',
  anger: '愤怒',
  neutral: '中性'
}

// 检测危机关键词
const crisisKeywords = ['想死', '自杀', 'zs', 'si', '自残', '不想活', '活不下去', '没意义']
const isCrisis = computed(() => {
  if (props.message.role !== 'user') return false
  const content = props.message.content.toLowerCase()
  return crisisKeywords.some(keyword => content.includes(keyword))
})

// 格式化内容（简单 Markdown 支持）
const formattedContent = computed(() => {
  let text = props.message.content

  // 转义 HTML
  text = text.replace(/&/g, '&amp;')
             .replace(/</g, '&lt;')
             .replace(/>/g, '&gt;')

  // 粗体 **text**
  text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')

  // 斜体 *text*
  text = text.replace(/\*(.+?)\*/g, '<em>$1</em>')

  // 换行
  text = text.replace(/\n/g, '<br>')

  return text
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}
</script>

<style scoped>
.message-item {
  display: flex;
  align-items: flex-start;
  animation: fadeIn 0.4s ease-out;
}

/* AI 消息靠左 */
.message-item.is-assistant {
  justify-content: flex-start;
}

/* 用户消息靠右 */
.message-item.is-user {
  justify-content: flex-end;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 70%;
}

.user-content {
  align-items: flex-end;
  max-width: 55%;
}

/* 情绪标签行 */
.emotion-tag-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 4px;
}

.emotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.emotion-tag.positive {
  background: linear-gradient(135deg, rgba(72, 199, 116, 0.15), rgba(72, 199, 116, 0.08));
  color: #27ae60;
  border: 1px solid rgba(72, 199, 116, 0.3);
}

.emotion-tag.negative {
  background: linear-gradient(135deg, rgba(231, 76, 60, 0.15), rgba(231, 76, 60, 0.08));
  color: #c0392b;
  border: 1px solid rgba(231, 76, 60, 0.3);
}

.emotion-tag.anxiety {
  background: linear-gradient(135deg, rgba(243, 156, 18, 0.15), rgba(243, 156, 18, 0.08));
  color: #d68910;
  border: 1px solid rgba(243, 156, 18, 0.3);
}

.emotion-tag.depression {
  background: linear-gradient(135deg, rgba(155, 89, 182, 0.15), rgba(155, 89, 182, 0.08));
  color: #8e44ad;
  border: 1px solid rgba(155, 89, 182, 0.3);
}

.emotion-tag.anger {
  background: linear-gradient(135deg, rgba(192, 57, 43, 0.15), rgba(192, 57, 43, 0.08));
  color: #c0392b;
  border: 1px solid rgba(192, 57, 43, 0.3);
}

.emotion-tag.neutral {
  background: linear-gradient(135deg, rgba(149, 165, 166, 0.15), rgba(149, 165, 166, 0.08));
  color: #7f8c8d;
  border: 1px solid rgba(149, 165, 166, 0.3);
}

/* AI 消息气泡 */
.ai-bubble {
  background: #fff;
  color: var(--color-text);
  border: 1px solid rgba(124, 156, 181, 0.12);
  border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 10px 16px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.ai-bubble:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

/* 用户消息气泡 */
.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #fff;
  border-radius: 20px 20px 4px 20px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3);
  padding: 5px 12px !important;
  line-height: 1.3 !important;
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.user-bubble:hover {
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transform: translateY(-1px);
}

/* 危机消息样式 */
.is-crisis.user-bubble {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%) !important;
  box-shadow: 0 2px 12px rgba(231, 76, 60, 0.4) !important;
}

.message-text {
  font-size: 15px;
}

.message-text :deep(strong) {
  font-weight: 600;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.7;
}

.ai-bubble .message-time {
  color: var(--color-text-light);
}

.user-bubble .message-time {
  color: rgba(255, 255, 255, 0.8);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .content-wrapper {
    max-width: 85%;
  }
}
</style>
