<template>
  <el-container class="chat-layout">
    <!-- 左侧会话列表 -->
    <el-aside width="280px" class="chat-sidebar">
      <div class="sidebar-header">
        <h3>🧠 心理探索对话</h3>
        <el-button type="primary" :icon="'Plus'" circle @click="createNewSession" />
      </div>
      
      <el-menu :default-active="String(currentSessionId)" class="session-menu" v-loading="loadingSessions">
        <el-menu-item 
          v-for="session in sessions" 
          :key="session.id" 
          :index="String(session.id)"
          @click="selectSession(session.id)"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span class="session-title">{{ session.title || '新的探索' }}</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-profile">
            <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <span class="username">{{ userInfo?.username || 'User' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-aside>

    <!-- 右侧聊天区域 -->
    <el-container>
      <el-header class="chat-header">
        <span v-if="currentSessionId">{{ currentSessionTitle }}</span>
        <span v-else>请选择或创建一个会话</span>
      </el-header>
      
      <el-main class="chat-main" ref="chatMainRef" v-loading="loadingMessages">
        <div v-if="messages.length === 0" class="empty-tip">
          <el-empty description="暂无消息，来打个招呼吧~" />
        </div>
        
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          :class="['message-item', msg.role === 'user' ? 'is-user' : 'is-ai', { 'is-crisis': msg.isCrisis }]"
        >
          <el-avatar 
            class="msg-avatar" 
            :src="msg.role === 'user' ? 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' : 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" 
          />
          <div class="msg-content">
            <div class="msg-text">{{ msg.content }}</div>
            <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
      </el-main>

      <el-footer class="chat-footer">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入你想说的话 (Shift + Enter 换行，Enter 发送)"
          @keydown.enter.prevent="handleEnter"
          :disabled="!currentSessionId || sending"
        />
        <div class="footer-actions">
          <el-button type="primary" :loading="sending" :disabled="!currentSessionId || !inputMessage.trim()" @click="sendMessage">
            发送 <el-icon class="el-icon--right"><Position /></el-icon>
          </el-button>
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

// 状态变量
const userInfo = ref(null)
const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')

const loadingSessions = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const chatMainRef = ref(null)

// 获取当前会话标题
const currentSessionTitle = computed(() => {
  const session = sessions.value.find(s => s.id === currentSessionId.value)
  return session ? session.title : ''
})

// 生命周期
onMounted(() => {
  const storedUser = localStorage.getItem('userInfo')
  if (!storedUser) {
    ElMessage.warning('未登录，请先登录！')
    router.push('/login')
    return
  }
  userInfo.value = JSON.parse(storedUser)
  loadSessions()
})

// 方法：加载会话列表
const loadSessions = async () => {
  loadingSessions.value = true
  try {
    const res = await request.get('/chat/session/list')
    sessions.value = res.data || []
    if (sessions.value.length > 0 && !currentSessionId.value) {
      selectSession(sessions.value[0].id)
    }
  } catch (err) {
    console.error('加载会话失败', err)
  } finally {
    loadingSessions.value = false
  }
}

// 方法：创建新会话
const createNewSession = async () => {
  try {
    const res = await request.post('/chat/session/create', { title: '新的心理探索' })
    const newSession = res.data
    sessions.value.unshift(newSession)
    selectSession(newSession.id)
  } catch (err) {
    console.error('创建会话失败', err)
  }
}

// 方法：选择会话
const selectSession = async (id) => {
  currentSessionId.value = id
  await loadMessages(id)
}

// 方法：加载会话消息
const loadMessages = async (id) => {
  loadingMessages.value = true
  try {
    const res = await request.get(`/chat/history?sessionId=${id}`)
    messages.value = res.data || []
    scrollToBottom()
  } catch (err) {
    console.error('加载消息失败', err)
  } finally {
    loadingMessages.value = false
  }
}

// 方法：处理键盘回车事件
const handleEnter = (e) => {
  if (e.shiftKey) {
    // Shift+Enter 允许换行，不处理
    return
  }
  // 纯 Enter 发送
  sendMessage()
}

// 方法：发送消息
// 方法：发送消息 (流式版)
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSessionId.value) return
  
  const content = inputMessage.value.trim()
  const sessionId = currentSessionId.value
  
  // 乐观更新：先在界面显示用户消息
  const tempUserMsg = {
    id: Date.now(),
    role: 'user',
    content: content,
    createTime: new Date().toISOString()
  }
  messages.value.push(tempUserMsg)
  inputMessage.value = ''
  scrollToBottom()
  
  sending.value = true

  // 准备 AI 消息的占位符
  const aiMsgId = Date.now() + 1
  const aiMsg = {
    id: aiMsgId,
    role: 'assistant',
    content: '',
    createTime: new Date().toISOString()
  }
  messages.value.push(aiMsg)

  try {
    const token = localStorage.getItem('token')
    // 使用原生的 fetch API 或 EventSource 来处理 SSE 流式数据
    // 注意：因为要带 Token，通常 GET + EventSource 不好带 Headers。
    // 如果后端的 /chat/stream 是 GET 接口，这里我们用 fetch 模拟读取流
    const response = await fetch(`http://localhost:8080/chat/stream?sessionId=${sessionId}&content=${encodeURIComponent(content)}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = '' // 使用缓冲区处理流式数据
    
    // 循环读取数据流
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      
      // 按换行符分割，只处理完整的行
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 最后一行可能不完整，保留到下一次
      
      let textToAppend = ''
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          // 去除前缀，提取真实内容
          let content = line.substring(5)
          if (content.startsWith(' ')) {
            content = content.substring(1)
          }
          textToAppend += content
        }
      }
      
      // 追加内容到刚才的占位消息中
      const targetMsg = messages.value.find(m => m.id === aiMsgId)
      if (targetMsg && textToAppend) {
        targetMsg.content += textToAppend.replace(/\\n/g, '\n')
        scrollToBottom()
      }
    }
  } catch (err) {
    console.error('流式发送消息失败', err)
    ElMessage.error('消息发送失败')
    const targetMsg = messages.value.find(m => m.id === aiMsgId)
    // 异常处理兜底：如果 AI 一句话都没憋出来就挂了，给用户一个系统提示
    if (targetMsg && targetMsg.content === '') {
      targetMsg.content = '[系统提示：消息发送失败或无响应]'
    }
  } finally {
    // 无论成功失败，都解除发送按钮的 loading 状态
    sending.value = false
    
    // 【核心亮点】自动总结会话标题的无感刷新机制
    // 每次流式消息结束后，检查当前会话是不是新创建的默认标题
    const session = sessions.value.find(s => s.id === sessionId)
    if (session && session.title === '新的心理探索') {
      // 延迟 1.5 秒，给后端独立线程请求大模型（总结标题并写入 MySQL）留出时间
      setTimeout(() => {
        loadSessions() // 重新拉取会话列表，实现无刷新的标题变更
      }, 1500) 
    }
  }
  }

// 方法：滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatMainRef.value) {
      const container = chatMainRef.value.$el || chatMainRef.value
      container.scrollTop = container.scrollHeight
    }
  })
}

// 方法：格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 方法：处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}
</script>

<style scoped>
.chat-layout {
  height: 100vh;
  background-color: #f5f7fa;
}

.chat-sidebar {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.session-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
}

.session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 10px;
  font-weight: 500;
  color: #606266;
}

.chat-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  padding: 0 20px;
}

.chat-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.empty-tip {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-item.is-user {
  flex-direction: row-reverse;
}

.message-item.is-ai {
  flex-direction: row;
}

.msg-avatar {
  flex-shrink: 0;
  margin: 0 15px;
}

.msg-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.is-user .msg-content {
  align-items: flex-end;
}

.is-ai .msg-content {
  align-items: flex-start;
}

.msg-text {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.is-user .msg-text {
  background-color: #409EFF;
  color: #fff;
  border-top-right-radius: 0;
}

.is-ai .msg-text {
  background-color: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-top-left-radius: 0;
}

/* 危机干预特殊样式 */
.is-crisis .msg-text {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  font-weight: bold;
}

.msg-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.chat-footer {
  height: auto !important;
  background-color: #fff;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}

.footer-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>