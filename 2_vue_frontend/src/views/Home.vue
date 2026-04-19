<template>
  <div class="home-container">
    <ChatLayout
      :sessions="sessions"
      :current-session-id="currentSessionId"
      :messages="messages"
      :is-sending="isSending"
      :is-typing="isTyping"
      :show-crisis-alert="showCrisisAlert"
      :crisis-alert-visible="crisisAlertVisible"
      @new-chat="createNewSession"
      @select-session="selectSession"
      @send="handleSend"
      @crisis-detected="handleCrisis"
      @open-crisis="openCrisisHelp"
      @close-crisis="crisisAlertVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ChatLayout from '@/components/ChatLayout.vue'
import { getSessionList, createSession, getChatHistory } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { BASE_URL } from '@/api/index'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const inputText = ref('')
const isSending = ref(false)
const isTyping = ref(false)
const showCrisisAlert = ref(false)
const crisisAlertVisible = ref(false)
const currentCrisisData = ref(null)

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await loadSessions()
})

const loadSessions = async () => {
  try {
    const res = await getSessionList()
    sessions.value = res.data || []
    appStore.setCurrentSession(currentSessionId.value)
    if (sessions.value.length > 0) {
      await selectSession(sessions.value[0].id)
    }
  } catch (err) {
    console.error('加载会话失败', err)
  }
}

const createNewSession = async () => {
  try {
    const res = await createSession()
    const newSession = res.data
    sessions.value.unshift(newSession)
    await selectSession(newSession.id)
  } catch (err) {
    console.error('创建会话失败', err)
  }
}

const selectSession = async (id) => {
  currentSessionId.value = id
  appStore.setCurrentSession(id)
  await loadMessages(id)
}

const loadMessages = async (id) => {
  try {
    const res = await getChatHistory(id)
    messages.value = res.data || []
    scrollToBottom()
  } catch (err) {
    console.error('加载消息失败', err)
  }
}

const handleSend = async (text) => {
  if (!text?.trim() || !currentSessionId.value || isSending.value) return

  const content = text.trim()
  const sessionId = currentSessionId.value

  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: content,
    createTime: new Date().toISOString()
  }
  messages.value.push(userMsg)
  scrollToBottom()

  isSending.value = true
  isTyping.value = true
  inputText.value = ''

  const aiMsg = {
    id: Date.now() + 1,
    role: 'assistant',
    content: '',
    createTime: new Date().toISOString()
  }
  messages.value.push(aiMsg)

  try {
    const response = await fetch(
      `${BASE_URL}/chat/stream?sessionId=${sessionId}&content=${encodeURIComponent(content)}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${userStore.token}`
        }
      }
    )

    if (!response.ok) throw new Error('请求失败')

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let textToAppend = ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          textToAppend += line.substring(5).trim()
        }
      }

      if (textToAppend) {
        const targetMsg = messages.value.find(m => m.id === aiMsg.id)
        if (targetMsg) {
          targetMsg.content += textToAppend.replace(/\\n/g, '\n')
          scrollToBottom()
        }
      }
    }
  } catch (err) {
    console.error('发送消息失败', err)
    ElMessage.error('消息发送失败，请重试')
    const targetMsg = messages.value.find(m => m.id === aiMsg.id)
    if (targetMsg) {
      targetMsg.content = '[系统提示：消息发送失败，请稍后重试]'
    }
  } finally {
    isSending.value = false
    isTyping.value = false
  }
}

const handleCrisis = (data) => {
  currentCrisisData.value = data
  showCrisisAlert.value = true
}

const openCrisisHelp = () => {
  crisisAlertVisible.value = true
  showCrisisAlert.value = false
}

const scrollToBottom = () => {
  nextTick(() => {
    const container = document.querySelector('.messages-container')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

watch(messages, scrollToBottom, { deep: true })
</script>

<style scoped>
.home-container {
  height: 100vh;
}
</style>
