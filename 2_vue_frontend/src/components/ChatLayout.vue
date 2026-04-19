<template>
  <div class="layout-container">
    <!-- 左侧会话列表 -->
    <aside :class="['sidebar', { collapsed: isCollapsed }]">

      <!-- 新建按钮 -->
      <div class="new-chat" @click="$emit('new-chat')">
        <el-icon><Edit /></el-icon>
        <span v-if="!isCollapsed">新建对话</span>
      </div>

      <!-- 会话列表 -->
      <div class="sessions-wrapper" v-if="!isCollapsed">
        <div class="sessions-header">历史记录</div>
        <div class="sessions-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-item', { active: session.id === currentSessionId }]"
            @click="$emit('select-session', session.id)"
          >
            <el-icon class="session-icon"><ChatLineSquare /></el-icon>
            <span class="session-title">{{ session.title || '新的对话' }}</span>
          </div>
        </div>
      </div>

    </aside>

    <!-- 主内容区 -->
    <main :class="['main-content', { 'sidebar-collapsed': isCollapsed }]">
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleSidebar">
        <el-icon v-if="!isCollapsed"><DArrowLeft /></el-icon>
        <el-icon v-else><DArrowRight /></el-icon>
      </div>

      <!-- 顶部导航 -->
      <TopNav />

      <!-- 消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <WelcomeCard
          v-if="messages.length === 0"
          @start-chat="$emit('new-chat')"
          @view-emotion="appStore.openEmotionDrawer"
        />

        <div v-else class="message-list">
          <ChatMessage
            v-for="msg in messages"
            :key="msg.id"
            :message="msg"
            @crisis-detected="$emit('crisis-detected', $event)"
          />

          <div v-if="isTyping" class="typing-indicator">
            <div class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 危机预警 -->
      <transition name="crisis-fade">
        <div v-if="showCrisisAlert" class="crisis-banner">
          <el-icon><WarningFilled /></el-icon>
          <span>检测到您可能需要帮助</span>
          <el-button type="danger" size="small" @click="$emit('open-crisis')">
            获取帮助
          </el-button>
          <el-button text @click="showCrisisAlert = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </transition>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            :disabled="!currentSessionId || isSending"
            placeholder="输入你想说的话..."
            resize="none"
            @keydown.enter.prevent="$emit('send', inputText); inputText = ''"
          />
          <el-button
            type="primary"
            :loading="isSending"
            :disabled="!inputText.trim() || !currentSessionId"
            @click="$emit('send', inputText); inputText = ''"
            class="send-btn"
          >
            <el-icon v-if="!isSending"><Promotion /></el-icon>
          </el-button>
        </div>
        <div class="input-tips">
          <span>AI 会尽力提供情感支持，但不能替代专业心理咨询</span>
        </div>
      </div>
    </main>

    <!-- 抽屉组件 -->
    <EmotionDrawer />
    <ResourceDrawer />
    <ModelDialog />
    <CrisisAlert
      :visible="crisisAlertVisible"
      @close="$emit('close-crisis')"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  DArrowLeft, DArrowRight, Edit, ChatLineSquare,
  WarningFilled, Close, Promotion
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import TopNav from '@/components/TopNav.vue'
import WelcomeCard from '@/components/WelcomeCard.vue'
import ChatMessage from '@/components/ChatMessage.vue'
import EmotionDrawer from '@/components/EmotionDrawer.vue'
import ResourceDrawer from '@/components/ResourceDrawer.vue'
import ModelDialog from '@/components/ModelDialog.vue'
import CrisisAlert from '@/components/CrisisAlert.vue'

const props = defineProps({
  sessions: { type: Array, default: () => [] },
  currentSessionId: { type: [Number, String], default: null },
  messages: { type: Array, default: () => [] },
  isSending: { type: Boolean, default: false },
  isTyping: { type: Boolean, default: false },
  showCrisisAlert: { type: Boolean, default: false },
  crisisAlertVisible: { type: Boolean, default: false }
})

const emit = defineEmits([
  'new-chat', 'select-session', 'send',
  'crisis-detected', 'open-crisis', 'close-crisis'
])

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapsed = ref(false)
const inputText = ref('')

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: var(--color-bg);
}

/* ========== 侧边栏 - 平滑过渡 ========== */
.sidebar {
  width: 280px;
  height: 100%;
  background: #fff;
  border-right: 1px solid rgba(124, 156, 181, 0.15);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1), 
              transform 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.4s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 0;
  border-right: none;
}

/* 折叠按钮 - 固定在左侧边缘，随侧边栏平滑移动 */
.collapse-btn {
  position: fixed;
  top: 72px;
  width: 28px;
  height: 28px;
  background: #fff;
  border: 1px solid rgba(124, 156, 181, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 101;
  transition: left 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.3s ease,
              background 0.2s ease,
              color 0.2s ease,
              border-color 0.2s ease,
              box-shadow 0.2s ease;
  color: #7c9cb5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  left: 256px;
}

.collapse-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transform: scale(1.1);
}

.main-content.sidebar-collapsed .collapse-btn {
  left: 16px;
}

/* 新建对话 */
.new-chat {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  margin: 12px;
  background: var(--color-bg);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--color-text);
  font-weight: 500;
  white-space: nowrap;
}

.new-chat:hover {
  background: rgba(124, 156, 181, 0.12);
  color: var(--color-primary);
}

.new-chat .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

/* 会话列表 */
.sessions-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: opacity 0.3s ease;
}

.sessions-header {
  padding: 8px 16px;
  font-size: 12px;
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: var(--color-text);
  margin-bottom: 2px;
}

.session-item:hover {
  background: var(--color-bg);
}

.session-item.active {
  background: rgba(124, 156, 181, 0.12);
  color: var(--color-primary);
  font-weight: 500;
}

.session-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.session-title {
  flex: 1;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 主内容区 ========== */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  transition: margin-left 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding-left: 0;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 60px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  margin-left: 0;
}

.typing-dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 20px 20px 20px 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  animation: typing-dot 1.4s infinite;
}

.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing-dot {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* 危机预警 */
.crisis-banner {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: var(--color-crisis-bg);
  border: 1px solid var(--color-crisis);
  border-radius: var(--radius-md);
  color: var(--color-crisis);
  z-index: 100;
  box-shadow: var(--shadow-md);
}

.crisis-banner .el-icon { font-size: 20px; }
.crisis-banner span { font-weight: 500; }

.crisis-fade-enter-active,
.crisis-fade-leave-active { transition: all 0.3s ease; }

.crisis-fade-enter-from,
.crisis-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* 输入区域 */
.input-area {
  padding: 16px 60px 24px;
  background: var(--color-bg);
  border-top: 1px solid rgba(124, 156, 181, 0.1);
  transition: padding 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  max-width: 900px;
  margin: 0 auto;
}

.input-wrapper .el-textarea { flex: 1; }

.input-wrapper :deep(.el-textarea__inner) {
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.5;
  border: 2px solid rgba(124, 156, 181, 0.2);
  transition: border-color 0.3s;
  background: #fff;
}

.input-wrapper :deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
}

.send-btn {
  height: 42px;
  width: 42px;
  padding: 0;
  border-radius: var(--radius-md);
}

.input-tips {
  text-align: center;
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-light);
}
</style>
