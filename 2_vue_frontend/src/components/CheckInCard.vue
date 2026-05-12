<template>
  <div class="checkin-card">
    <div class="checkin-header">
      <h4><i class="fa-solid fa-calendar-check"></i> 每日打卡</h4>
      <span class="streak-badge" v-if="streak > 0">
        <i class="fa-solid fa-fire"></i> {{ streak }}天
      </span>
    </div>

    <!-- 打卡类型切换 -->
    <div class="checkin-tabs">
      <button
        v-for="tab in checkinTypes"
        :key="tab.type"
        :class="['tab-btn', { active: activeTab === tab.type }]"
        @click="activeTab = tab.type"
      >
        <i :class="tab.icon"></i>
        {{ tab.label }}
      </button>
    </div>

    <!-- 打卡选项 -->
    <div class="checkin-options" v-if="activeTab === 'mood'">
      <button
        v-for="opt in moodOptions"
        :key="opt.value"
        :class="['opt-btn', opt.color, { selected: selectedValue === opt.value }]"
        @click="selectAndSubmit(opt.value)"
      >
        <span class="opt-icon">{{ opt.emoji }}</span>
        <span class="opt-label">{{ opt.label }}</span>
      </button>
    </div>

    <div class="checkin-options" v-else-if="activeTab === 'sleep'">
      <button
        v-for="opt in sleepOptions"
        :key="opt.value"
        :class="['opt-btn', opt.color, { selected: selectedValue === opt.value }]"
        @click="selectAndSubmit(opt.value)"
      >
        <span class="opt-icon">{{ opt.emoji }}</span>
        <span class="opt-label">{{ opt.label }}</span>
      </button>
    </div>

    <div class="checkin-options" v-else-if="activeTab === 'exercise'">
      <button
        v-for="opt in exerciseOptions"
        :key="opt.value"
        :class="['opt-btn', opt.color, { selected: selectedValue === opt.value }]"
        @click="selectAndSubmit(opt.value)"
      >
        <span class="opt-icon">{{ opt.emoji }}</span>
        <span class="opt-label">{{ opt.label }}</span>
      </button>
    </div>

    <div class="checkin-options" v-else-if="activeTab === 'social'">
      <button
        v-for="opt in socialOptions"
        :key="opt.value"
        :class="['opt-btn', opt.color, { selected: selectedValue === opt.value }]"
        @click="selectAndSubmit(opt.value)"
      >
        <span class="opt-icon">{{ opt.emoji }}</span>
        <span class="opt-label">{{ opt.label }}</span>
      </button>
    </div>

    <!-- 今日打卡状态 -->
    <div class="checkin-status" v-if="todayChecked">
      <i class="fa-solid fa-check-circle"></i>
      今日已打卡
    </div>

    <!-- 备注输入 -->
    <div class="note-area" v-if="showNote">
      <input
        v-model="noteText"
        type="text"
        class="note-input"
        placeholder="添加备注（可选）..."
        @keyup.enter="confirmSubmit"
      />
      <button class="note-confirm" @click="confirmSubmit">确认</button>
      <button class="note-cancel" @click="showNote = false">取消</button>
    </div>

    <!-- 提交提示 -->
    <div class="submit-toast" v-if="showToast">
      <i class="fa-solid fa-check"></i> {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { submitCheckin, getStreak, getCheckinStats } from '@/api/checkin'

const activeTab = ref('mood')
const selectedValue = ref('')
const noteText = ref('')
const showNote = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const streak = ref(0)
const todayChecked = ref(false)

const checkinTypes = [
  { type: 'mood', label: '心情', icon: 'fa-regular fa-face-smile' },
  { type: 'sleep', label: '睡眠', icon: 'fa-solid fa-moon' },
  { type: 'exercise', label: '运动', icon: 'fa-solid fa-person-walking' },
  { type: 'social', label: '社交', icon: 'fa-solid fa-users' }
]

const moodOptions = [
  { value: 'happy', label: '开心', emoji: '😊', color: 'mood-happy' },
  { value: 'ok', label: '一般', emoji: '😐', color: 'mood-ok' },
  { value: 'sad', label: '低落', emoji: '😢', color: 'mood-sad' },
  { value: 'anxious', label: '焦虑', emoji: '😰', color: 'mood-anxious' },
  { value: 'angry', label: '愤怒', emoji: '😠', color: 'mood-angry' }
]

const sleepOptions = [
  { value: 'good_sleep', label: '充足', emoji: '😴', color: 'sleep-good' },
  { value: 'poor_sleep', label: '不足', emoji: '🥱', color: 'sleep-poor' },
  { value: 'insomnia', label: '失眠', emoji: '😵', color: 'sleep-insomnia' }
]

const exerciseOptions = [
  { value: 'intense', label: '高强度', emoji: '🏃', color: 'exercise-high' },
  { value: 'mild', label: '轻度', emoji: '🚶', color: 'exercise-mild' },
  { value: 'none', label: '无', emoji: '🛋️', color: 'exercise-none' }
]

const socialOptions = [
  { value: 'social', label: '有社交', emoji: '🤝', color: 'social-active' },
  { value: 'alone', label: '独处', emoji: '🧍', color: 'social-alone' },
  { value: 'online', label: '线上社交', emoji: '💬', color: 'social-online' }
]

const showToastMsg = (msg) => {
  toastMessage.value = msg
  showToast.value = true
  setTimeout(() => { showToast.value = false }, 2500)
}

const selectAndSubmit = (value) => {
  selectedValue.value = value
  noteText.value = ''
  showNote.value = true
}

const confirmSubmit = async () => {
  if (!selectedValue.value) return
  try {
    await submitCheckin({
      checkinType: activeTab.value,
      checkinValue: selectedValue.value,
      note: noteText.value.trim() || ''
    })
    showNote.value = false
    todayChecked.value = true
    showToastMsg('打卡成功！')
    loadStats()
  } catch (err) {
    console.error('打卡失败', err)
    showToastMsg('打卡失败，请重试')
  }
}

const loadStats = async () => {
  try {
    const [streakRes, statsRes] = await Promise.all([getStreak(), getCheckinStats(7)])
    streak.value = streakRes.data?.streak || 0
  } catch (err) {
    console.error('加载打卡统计失败', err)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.checkin-card {
  background: rgba(255, 255, 255, 0.6);
  border-radius: var(--radius-md);
  padding: 14px;
  border: 1px solid var(--color-border);
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.checkin-header h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-dark);
  display: flex;
  align-items: center;
  gap: 6px;
}

.streak-badge {
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  color: white;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.checkin-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.tab-btn {
  flex: 1;
  min-width: 0;
  padding: 5px 4px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--color-text-light);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.tab-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.tab-btn i { font-size: 12px; }

.checkin-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 8px;
}

.opt-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 4px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 11px;
}

.opt-btn:hover { transform: translateY(-1px); box-shadow: var(--shadow-sm); }
.opt-btn.selected { border-color: var(--color-primary); background: rgba(107, 144, 128, 0.08); }

.opt-icon { font-size: 18px; }
.opt-label { color: var(--color-text-dark); font-weight: 500; }

.mood-happy { color: #2ecc71; }
.mood-ok { color: #95a5a6; }
.mood-sad { color: #3498db; }
.mood-anxious { color: #f39c12; }
.mood-angry { color: #e74c3c; }

.sleep-good { color: #2ecc71; }
.sleep-poor { color: #f39c12; }
.sleep-insomnia { color: #e74c3c; }

.exercise-high { color: #2ecc71; }
.exercise-mild { color: #3498db; }
.exercise-none { color: #95a5a6; }

.social-active { color: #2ecc71; }
.social-alone { color: #f39c12; }
.social-online { color: #3498db; }

.checkin-status {
  text-align: center;
  color: #2ecc71;
  font-size: 12px;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.note-area {
  display: flex;
  gap: 4px;
  align-items: center;
  margin-top: 8px;
}

.note-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: white;
  outline: none;
}

.note-input:focus { border-color: var(--color-primary); }

.note-confirm {
  padding: 6px 10px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 11px;
  cursor: pointer;
}

.note-cancel {
  padding: 6px 8px;
  background: transparent;
  color: var(--color-text-light);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  cursor: pointer;
}

.submit-toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: #2ecc71;
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 1000;
  animation: toastIn 0.3s ease;
}

@keyframes toastIn {
  from { opacity: 0; transform: translateX(-50%) translateY(10px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
</style>
