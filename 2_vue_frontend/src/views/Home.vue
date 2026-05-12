<template>
  <div class="home-container">
    <div class="layout-container">
      <!-- ================= 左侧：导航栏（固定底部） ================= -->
      <aside class="glass-panel sidebar">
        <div class="sidebar-content">
          <div class="brand">
            <i class="fa-solid fa-leaf"></i> 心晴 Pro
          </div>

          <ul class="nav-menu">
            <li
              v-for="item in navItems"
              :key="item.id"
              :class="['nav-item', { active: currentNav === item.id }]"
              @click="handleNavClick(item)"
            >
              <i :class="item.icon"></i>
              <span>{{ item.label }}</span>
            </li>
          </ul>
        </div>
      </aside>

      <!-- ================= 中间：主内容区（聊天 / 情绪报告 / 正念空间 / 咨询档案） ================= -->
      <main class="glass-panel chat-section">
        <header class="chat-header">
          <div class="ai-title">
            <div class="pulse"></div>
            {{ panelTitle }}
          </div>
          <div class="header-tools">
            <!-- AI 模型切换 -->
            <button class="model-switch-btn" @click="appStore.openModelDialog()">
              <i class="fa-solid fa-sliders"></i>
              <span class="model-switch-label">{{ appStore.currentModel.name || '选择模型' }}</span>
              <i class="fa-solid fa-caret-down"></i>
            </button>

            <!-- 用户下拉菜单 -->
            <div class="user-dropdown" v-click-outside="closeDropdown">
              <button class="user-btn" @click="toggleDropdown">
                <div class="user-avatar">
                  <i class="fa-solid fa-user"></i>
                </div>
                <span class="user-name">{{ userStore.username }}</span>
                <i class="fa-solid fa-chevron-down" :class="{ rotated: showUserMenu }"></i>
              </button>

              <transition name="dropdown-fade">
                <div v-if="showUserMenu" class="dropdown-menu">
                  <div class="dropdown-header">
                    <div class="dropdown-name">{{ userStore.userInfo?.username }}</div>
                    <div class="dropdown-role">
                      <span v-if="userStore.isAdmin" class="role-badge admin">
                        <i class="fa-solid fa-shield-halved"></i> 管理员
                      </span>
                      <span v-else class="role-badge user">
                        <i class="fa-regular fa-user"></i> 普通用户
                      </span>
                    </div>
                  </div>
                  <div class="dropdown-divider"></div>
                  <router-link to="/profile" class="dropdown-item" @click="closeDropdown">
                    <i class="fa-regular fa-user-circle"></i>
                    个人中心
                  </router-link>
                  <router-link v-if="userStore.isAdmin" to="/admin" class="dropdown-item" @click="closeDropdown">
                    <i class="fa-solid fa-gears"></i>
                    管理控制台
                  </router-link>
                  <div class="dropdown-divider"></div>
                  <div class="dropdown-item logout" @click="handleLogout">
                    <i class="fa-solid fa-right-from-bracket"></i>
                    退出登录
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </header>

        <!-- ========== 聊天面板 ========== -->
        <template v-if="currentNav === 'chat'">
          <div class="chat-body" ref="chatBodyRef">
            <!-- 欢迎卡片 -->
            <div v-if="messages.length === 0" class="welcome-area">
              <div class="welcome-icon">
                <i class="fa-solid fa-spa"></i>
              </div>
              <h2>你好，我是晓风</h2>
              <p>我是你的 AI 心理倾听者。有什么想聊的吗？</p>

              <div class="quick-actions">
                <button class="quick-btn" @click="fillInput('我想聊聊最近的心情')">
                  <i class="fa-regular fa-comments"></i> 聊聊心情
                </button>
                <button class="quick-btn" @click="fillInput('最近压力有点大')">
                  <i class="fa-solid fa-brain"></i> 缓解压力
                </button>
                <button class="quick-btn" @click="fillInput('带我做一个放松练习')">
                  <i class="fa-solid fa-wind"></i> 放松练习
                </button>
              </div>
            </div>

            <!-- 消息列表 -->
            <template v-else>
              <div
                v-for="msg in messages"
                :key="msg.id"
                :class="['message', msg.role]"
              >
                <div class="avatar">
                  <i :class="msg.role === 'assistant' ? 'fa-solid fa-spa' : 'fa-solid fa-user'"></i>
                </div>
                <div class="bubble" v-html="formatMessage(msg.content)"></div>
              </div>

              <!-- 打字指示器 -->
              <div v-if="isTyping" class="message bot">
                <div class="avatar">
                  <i class="fa-solid fa-spa"></i>
                </div>
                <div class="bubble">
                  <div class="typing">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <div class="input-box">
              <button class="icon-btn" title="新建会话" @click="createNewSession">
                <i class="fa-solid fa-plus"></i>
              </button>
              <button class="icon-btn" title="上传日记">
                <i class="fa-solid fa-paperclip"></i>
              </button>
              <input
                type="text"
                v-model="inputText"
                placeholder="输入你想表达的内容..."
                @keyup.enter="handleSendMessage"
                ref="inputRef"
              />
              <button class="icon-btn btn-send" @click="handleSendMessage" :disabled="!inputText.trim()">
                <i class="fa-solid fa-paper-plane"></i>
              </button>
            </div>
          </div>
        </template>

        <!-- ========== 情绪报告面板 ========== -->
        <template v-else-if="currentNav === 'report'">
          <div class="panel-body">
            <!-- 概览卡片 -->
            <div class="report-overview">
              <div class="report-stat glass-panel">
                <div class="stat-icon" style="background: rgba(46,204,113,0.12); color: #2ecc71">
                  <i class="fa-solid fa-face-smile"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-val">{{ emotionStats.avgScore }}</div>
                  <div class="stat-label">平均情绪得分</div>
                </div>
              </div>
              <div class="report-stat glass-panel">
                <div class="stat-icon" style="background: rgba(52,152,219,0.12); color: #3498db">
                  <i class="fa-solid fa-list-check"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-val">{{ emotionStats.totalRecords }}</div>
                  <div class="stat-label">情绪记录数</div>
                </div>
              </div>
              <div class="report-stat glass-panel">
                <div class="stat-icon" style="background: rgba(243,156,18,0.12); color: #f39c12">
                  <i class="fa-solid fa-comments"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-val">{{ emotionStats.totalSessions }}</div>
                  <div class="stat-label">参与会话数</div>
                </div>
              </div>
            </div>

            <!-- 趋势图表 -->
            <div class="report-chart glass-panel">
              <div class="report-chart-header">
                <h3>情绪趋势</h3>
                <el-select v-model="reportDays" size="small" style="width: 120px">
                  <el-option label="近7天" :value="7" />
                  <el-option label="近30天" :value="30" />
                  <el-option label="近90天" :value="90" />
                </el-select>
              </div>
              <div v-loading="loadingReport" style="height: 220px">
                <v-chart v-if="reportTrendData.length > 0" :option="reportTrendOption" autoresize style="height: 220px" />
                <div v-else class="chart-empty">
                  <i class="fa-solid fa-chart-line"></i>
                  <p>暂无趋势数据</p>
                </div>
              </div>
            </div>

            <!-- 情绪分布 -->
            <div class="report-dist glass-panel">
              <h3>情绪类型分布</h3>
              <div v-loading="loadingReport" style="height: 200px">
                <v-chart v-if="reportDistData.length > 0" :option="reportDistOption" autoresize style="height: 200px" />
                <div v-else class="chart-empty">
                  <p>暂无分布数据</p>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- ========== 正念空间面板 ========== -->
        <template v-else-if="currentNav === 'mindful'">
          <div class="panel-body mindful-body">
            <div class="mindful-header">
              <div class="mindful-icon-wrap">
                <i class="fa-solid fa-om"></i>
              </div>
              <h2>正念空间</h2>
              <p>选择一个练习，专注于当下</p>
            </div>

            <!-- 练习选择 -->
            <div class="mindful-categories">
              <div
                v-for="cat in mindfulCats"
                :key="cat.id"
                :class="['mindful-cat', { active: activeMindfulCat === cat.id }]"
                @click="activeMindfulCat = cat.id"
              >
                <i :class="cat.icon"></i>
                <span>{{ cat.label }}</span>
              </div>
            </div>

            <!-- 练习卡片 -->
            <div class="mindful-exercises">
              <div
                v-for="ex in currentExercises"
                :key="ex.id"
                :class="['exercise-card', { playing: playingId === ex.id }]"
                @click="toggleExercise(ex)"
              >
                <div class="exercise-cover" :style="{ background: getExerciseColor(ex.id) }">
                  <i :class="ex.iconClass || 'fa-solid fa-spa'"></i>
                  <div v-if="playingId === ex.id" class="playing-indicator">
                    <span></span><span></span><span></span>
                  </div>
                </div>
                <div class="exercise-info">
                  <div class="exercise-title">{{ ex.title }}</div>
                  <div class="exercise-meta">
                    <span><i class="fa-regular fa-clock"></i> {{ ex.durationMinutes || 5 }}分钟</span>
                    <span><i class="fa-solid fa-signal"></i> {{ ex.difficultyLevel === 'entry' ? '入门' : '进阶' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 正在播放 -->
            <transition name="player-slide">
              <div v-if="playingExercise" class="mindful-player glass-panel">
                <div class="player-cover" :style="{ background: getExerciseColor(playingExercise.id) }">
                  <i :class="playingExercise.iconClass || 'fa-solid fa-spa'"></i>
                </div>
                <div class="player-info">
                  <div class="player-title">{{ playingExercise.title }}</div>
                  <div class="player-sub">{{ playingExercise.applicableScene }}</div>
                  <div class="player-progress">
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: playProgress + '%' }"></div>
                    </div>
                    <div class="progress-time">
                      <span>{{ formatPlayTime(playTime) }}</span>
                      <span>{{ formatPlayTime((playingExercise.durationMinutes || 5) * 60) }}</span>
                    </div>
                  </div>
                </div>
                <div class="player-controls">
                  <button class="ctrl-btn" @click.stop="togglePlay">
                    <i :class="isPlaying ? 'fa-solid fa-pause' : 'fa-solid fa-play'"></i>
                  </button>
                  <button class="ctrl-btn" @click.stop="stopExercise">
                    <i class="fa-solid fa-stop"></i>
                  </button>
                </div>
              </div>
            </transition>
          </div>
        </template>

        <!-- ========== 咨询档案面板 ========== -->
        <template v-else-if="currentNav === 'archive'">
          <div class="panel-body archive-body">
            <div class="archive-header">
              <h2><i class="fa-solid fa-folder-open"></i> 咨询档案</h2>
              <p>查看和管理您的历史咨询记录</p>
            </div>

            <!-- 统计卡片 -->
            <div class="archive-stats">
              <div class="archive-stat glass-panel clickable" @click="jumpToReport">
                <i class="fa-solid fa-comments"></i>
                <div class="astat-val">{{ archiveStats.totalSessions }}</div>
                <div class="astat-label">总咨询次数</div>
              </div>
              <div class="archive-stat glass-panel clickable" @click="jumpToReport">
                <i class="fa-solid fa-clock-rotate-left"></i>
                <div class="astat-val">{{ archiveStats.totalRecords }}</div>
                <div class="astat-label">情绪记录数</div>
              </div>
              <div class="archive-stat glass-panel clickable" @click="jumpToReport">
                <i class="fa-solid fa-calendar-check"></i>
                <div class="astat-val">{{ archiveStats.totalCheckinDays }}</div>
                <div class="astat-label">打卡总天数</div>
              </div>
            </div>

            <!-- 历史会话列表 -->
            <div class="archive-section glass-panel">
              <div class="archive-section-header">
                <h3><i class="fa-solid fa-clock-rotate-left"></i> 历史会话</h3>
                <span class="badge">{{ sessions.length }}条</span>
              </div>
              <div v-loading="loadingArchive" class="session-list">
                <div v-if="!loadingArchive && sessions.length === 0" class="list-empty">
                  <i class="fa-solid fa-inbox"></i>
                  <p>暂无咨询记录</p>
                </div>
                <div
                  v-for="session in sessions"
                  :key="session.id"
                  class="session-card"
                  @click="viewSession(session)"
                >
                  <div class="session-icon">
                    <i class="fa-solid fa-comment-dots"></i>
                  </div>
                  <div class="session-detail">
                    <div class="session-title">{{ session.title || '新的心理探索' }}</div>
                    <div class="session-date">{{ formatDate(session.createTime) }}</div>
                  </div>
                  <div class="session-arrow">
                    <i class="fa-solid fa-chevron-right"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 危机预警横幅 -->
        <transition name="crisis-slide">
          <div v-if="showCrisisAlert && currentNav === 'chat'" class="crisis-banner">
            <div class="crisis-content">
              <i class="fa-solid fa-heart-crack"></i>
              <span>检测到您可能需要帮助</span>
            </div>
            <button class="crisis-btn" @click="openCrisisHelp">获取帮助</button>
            <button class="crisis-close" @click="showCrisisAlert = false">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        </transition>
      </main>

      <!-- ================= 右侧：AI 分析面板 + 打卡 + 画像（统一在第三列） ================= -->
      <div class="right-sidebar-wrapper">
        <aside class="glass-panel analysis-panel">
          <div class="panel-header">
            <span>实时分析</span>
            <span class="badge">AI Copilot</span>
          </div>

          <div class="panel-content">
            <!-- 情绪雷达模块 -->
            <div class="emotion-module">
              <h3><i class="fa-solid fa-wave-square"></i> 情绪状态侦测</h3>
              <div class="emotion-bar-group">
                <div class="emotion-item">
                  <span class="emotion-label">平静</span>
                  <div class="bar-bg">
                    <div
                      class="bar-fill bar-calm"
                      :style="{ width: emotionData.calm + '%' }"
                    ></div>
                  </div>
                  <span class="emotion-value">{{ emotionData.calm }}%</span>
                </div>
                <div class="emotion-item">
                  <span class="emotion-label">焦虑</span>
                  <div class="bar-bg">
                    <div
                      class="bar-fill bar-anxious"
                      :style="{ width: emotionData.anxious + '%' }"
                    ></div>
                  </div>
                  <span class="emotion-value">{{ emotionData.anxious }}%</span>
                </div>
                <div class="emotion-item">
                  <span class="emotion-label">低落</span>
                  <div class="bar-bg">
                    <div
                      class="bar-fill bar-sad"
                      :style="{ width: emotionData.sad + '%' }"
                    ></div>
                  </div>
                  <span class="emotion-value">{{ emotionData.sad }}%</span>
                </div>
              </div>
            </div>

            <!-- 对话备忘录 -->
            <div class="summary-module">
              <h3><i class="fa-solid fa-pen-to-square"></i> 对话备忘录</h3>
              <div v-if="conversationSummary.length > 0">
                <ul class="summary-list">
                  <li v-for="(item, index) in conversationSummary" :key="index">
                    {{ item }}
                  </li>
                </ul>
              </div>
              <p v-else class="summary-placeholder">
                <i class="fa-solid fa-comment-dots"></i>
                AI正在倾听并提取关键信息...
              </p>
            </div>

            <!-- 心理辅导建议 -->
            <div class="advice-module">
              <h3><i class="fa-solid fa-lightbulb"></i> 心理辅导建议</h3>
              <p v-if="currentAdvice">{{ currentAdvice }}</p>
              <p v-else class="advice-placeholder">
                随着对话进行，将在这里提供针对性的认知行为疗法干预建议。
              </p>
            </div>

            <!-- 危机干预热线 -->
            <div class="sos-btn" @click="openCrisisHelp">
              <i class="fa-solid fa-phone-volume"></i> 危机干预热线
            </div>
          </div>
        </aside>

        <CheckInCard />
        <div class="resource-quick-card glass-panel" @click="jumpToMindful">
          <div class="rq-icon"><i class="fa-solid fa-book-open"></i></div>
          <div class="rq-info">
            <div class="rq-title">心理资源库</div>
            <div class="rq-desc">点击查看专业干预资源</div>
          </div>
          <i class="fa-solid fa-chevron-right rq-arrow"></i>
        </div>
        <ProfileTag
          :stressLevel="profileStressLevel"
          :emotionalTrend="profileTrend"
          @jump-report="jumpToReport"
        />
      </div>
    </div>

    <!-- 历史会话抽屉 -->
    <transition name="drawer-slide">
      <div v-if="showHistory" class="history-drawer">
        <div class="drawer-header">
          <h3>历史会话</h3>
          <button class="drawer-close" @click="showHistory = false">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
        <div class="drawer-content">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['history-item', { active: session.id === currentSessionId }]"
            @click="selectSession(session.id)"
          >
            <i class="fa-regular fa-comments"></i>
            <div class="history-info">
              <div class="history-title">{{ session.title }}</div>
              <div class="history-time">{{ formatTime(session.createTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 危机干预对话框 -->
    <transition name="modal-fade">
      <div v-if="crisisDialogVisible" class="crisis-modal" @click.self="crisisDialogVisible = false">
        <div class="crisis-modal-content">
          <div class="crisis-modal-icon">
            <i class="fa-solid fa-hand-holding-heart"></i>
          </div>
          <h2>你并不孤单</h2>
          <p>
            如果你现在感到痛苦或有伤害自己的想法，请知道有人愿意倾听和支持你。
          </p>
          <div class="crisis-hotlines">
            <h3>全国心理援助热线</h3>
            <div class="hotline-item">
              <span class="hotline-name">全国心理危机干预热线</span>
              <span class="hotline-number">400-161-9995</span>
            </div>
            <div class="hotline-item">
              <span class="hotline-name">北京心理危机研究与干预中心</span>
              <span class="hotline-number">010-82951332</span>
            </div>
            <div class="hotline-item">
              <span class="hotline-name">生命热线</span>
              <span class="hotline-number">400-821-1215</span>
            </div>
          </div>
          <button class="crisis-modal-close" @click="crisisDialogVisible = false">
            我知道了
          </button>
        </div>
      </div>
    </transition>

    <!-- 认知投票弹窗 -->
    <CognitiveVoting
      :visible="showVoting"
      :emotionType="votingEmotionType"
      :recentVotingType="recentVotingType"
      @close="showVoting = false"
      @submitted="onVotingSubmitted"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSessionList, createSession, getChatHistory } from '@/api/chat'
import { getEmotionTrend, getLatestEmotion, getConversationSummary } from '@/api/emotion'
import { getAllResources } from '@/api/resource'
import { getCheckinStats } from '@/api/checkin'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { BASE_URL } from '@/api/index'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import CheckInCard from '@/components/CheckInCard.vue'
import ProfileTag from '@/components/ProfileTag.vue'
import CognitiveVoting from '@/components/CognitiveVoting.vue'
import { checkShouldTrigger } from '@/api/voting'

use([CanvasRenderer, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// DOM refs
const chatBodyRef = ref(null)
const inputRef = ref(null)

// 跨函数共享的 SSE 连接引用（用于 onUnmounted 清理）
let currentEventSource = null
let currentEventSourceClosed = false

// 状态
const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const inputText = ref('')
const isSending = ref(false)
const isTyping = ref(false)
const showCrisisAlert = ref(false)
const crisisDialogVisible = ref(false)
const showHistory = ref(false)

// 情绪数据
const emotionData = reactive({
  calm: 70,
  anxious: 40,
  sad: 20
})

// 用户画像数据
const profileStressLevel = ref('medium')
const profileTrend = ref('stable')

// 认知投票弹窗状态
const showVoting = ref(false)
const votingEmotionType = ref('neutral')
const recentVotingType = ref('')

// 对话摘要
const conversationSummary = ref([])
const currentAdvice = ref('')

// 导航
const currentNav = ref('chat')
const panelTitle = computed(() => {
  const titles = {
    chat: 'AI 心理咨询师 - 晓风',
    report: '情绪报告',
    mindful: '正念空间',
    archive: '咨询档案'
  }
  return titles[currentNav.value] || 'AI 心理咨询师 - 晓风'
})

const navItems = [
  { id: 'chat', label: '深度对话', icon: 'fa-regular fa-comments' },
  { id: 'report', label: '情绪报告', icon: 'fa-solid fa-chart-pie' },
  { id: 'mindful', label: '正念空间', icon: 'fa-solid fa-headphones' },
  { id: 'archive', label: '咨询档案', icon: 'fa-regular fa-folder-open' }
]

// 周打卡数据由 CheckInCard 组件通过 API 动态获取，此处不再使用
// const weekDays = reactive([...])

// 用户下拉菜单
const showUserMenu = ref(false)
const toggleDropdown = () => {
  showUserMenu.value = !showUserMenu.value
}
const closeDropdown = () => {
  showUserMenu.value = false
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// 生命周期
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await loadSessions()
})

// 组件销毁时关闭未完成的 SSE 连接，防止内存泄漏
onUnmounted(() => {
  if (currentEventSource) {
    currentEventSource.close()
    currentEventSource = null
    currentEventSourceClosed = true
  }
})

// 加载会话列表
const loadSessions = async () => {
  try {
    const res = await getSessionList()
    sessions.value = res.data || []
    if (sessions.value.length > 0) {
      await selectSession(sessions.value[0].id)
    }
  } catch (err) {
    console.error('加载会话失败', err)
  }
}

// 创建新会话
const createNewSession = async () => {
  // 如果当前是新会话（没有消息），不做任何操作
  if (messages.value.length === 0) {
    return
  }
  try {
    // 清空当前会话状态
    currentSessionId.value = null
    messages.value = []
    // 关闭历史记录面板
    showHistory.value = false
    // 调用后端创建新会话
    const res = await createSession()
    sessions.value.unshift(res.data)
    await selectSession(res.data.id)
  } catch (err) {
    console.error('创建会话失败', err)
  }
}

// 选择会话
const selectSession = async (id) => {
  currentSessionId.value = id
  showHistory.value = false
  await loadMessages(id)
}

// 加载消息
const loadMessages = async (id) => {
  try {
    const res = await getChatHistory(id)
    messages.value = res.data || []
    scrollToBottom()
  } catch (err) {
    console.error('加载消息失败', err)
  }
}

// 导航点击
const handleNavClick = (item) => {
  currentNav.value = item.id
  if (item.id === 'report') loadReportData()
  if (item.id === 'archive') loadArchiveData()
  if (item.id === 'mindful') loadMindfulData()
}

// 填充输入
const fillInput = (text) => {
  inputText.value = text
  inputRef.value?.focus()
}

// 发送消息
const handleSendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isSending.value) return

  if (!currentSessionId.value) {
    await createNewSession()
  }

  const content = text
  const sessionId = currentSessionId.value
  inputText.value = ''

  // 用户消息入列表
  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: content,
    createTime: new Date().toISOString()
  }
  messages.value.push(userMsg)
  scrollToBottom()

  // AI 占位
  isTyping.value = true
  isSending.value = true

  // 先创建占位 AI 消息
  const aiMsgId = Date.now() + 1
  messages.value.push({
    id: aiMsgId,
    role: 'assistant',
    content: '',
    createTime: new Date().toISOString()
  })

  try {
    const modelCode = appStore.currentModel.code || 'deepseek'
    const url = `${BASE_URL}/chat/stream?sessionId=${sessionId}&content=${encodeURIComponent(content)}&modelCode=${modelCode}&token=${userStore.token}`

    // 使用 EventSource 原生 SSE 解析，不丢事件、不乱序
    const eventSource = new EventSource(url)
    currentEventSource = eventSource
    currentEventSourceClosed = false
    let fullResponse = ''

    eventSource.addEventListener('done', (e) => {
      if (currentEventSourceClosed) return
      eventSource.close()
      currentEventSourceClosed = true
      isTyping.value = false
      isSending.value = false
      // 更新最终完整消息
      const aiMsg = messages.value.find(m => m.id === aiMsgId)
      if (aiMsg) {
        aiMsg.content = fullResponse
      }
      // 等 Vue 渲染完 DOM 后再滚动
      nextTick(() => {
        updateEmotionAnalysis(sessionId)
        scrollToBottom()
      })
    })

    eventSource.addEventListener('chunk', (e) => {
      if (currentEventSourceClosed) return
      // 命名 chunk 事件：数据原样透传，绕过 SSE 内置 data: 行解析，彻底避免 SyntaxError
      fullResponse += e.data
      const aiMsg = messages.value.find(m => m.id === aiMsgId)
      if (aiMsg) {
        aiMsg.content = fullResponse
      }
    })

    eventSource.addEventListener('error', () => {
      if (currentEventSourceClosed) return
      // EventSource.CONNECTING=0  正在连接
      // EventSource.OPEN=1        连接正常
      // EventSource.CLOSED=2      连接已关闭（正常结束或异常断开）
      // 如果 readyState === CLOSED，说明后端已主动关闭连接（正常结束），不弹错误
      if (eventSource.readyState === EventSource.CLOSED) {
        eventSource.close()
        currentEventSourceClosed = true
        isTyping.value = false
        isSending.value = false
        return
      }
      // 否则是真正的网络错误
      eventSource.close()
      currentEventSourceClosed = true
      isTyping.value = false
      isSending.value = false
      const idx = messages.value.findIndex(m => m.id === aiMsgId)
      if (idx !== -1 && !messages.value[idx].content) {
        messages.value.splice(idx, 1)
      }
      ElMessage.error('连接断开，请检查网络后重试')
      nextTick(() => scrollToBottom())
    })

  } catch (err) {
    console.error('发送消息失败', err)
    isTyping.value = false
    isSending.value = false
    ElMessage.error('消息发送失败，请稍后重试')
  }
}

// 更新实时情绪分析（调用后端真实分析结果）
const updateEmotionAnalysis = async (sessionId) => {
  if (!sessionId) return
  try {
    const [emotionRes, summaryRes] = await Promise.all([
      getLatestEmotion(sessionId),
      getConversationSummary(sessionId)
    ])

    const record = emotionRes.data
    const summary = summaryRes.data

    let detectedType = 'neutral'
    let detectedScore = 0.5

    if (record) {
      // 用后端真实数据更新情绪条
      const valence = record.valence ?? 0
      const arousal = record.arousal ?? 0.5
      detectedScore = record.emotionScore ?? 0.5
      detectedType = record.emotionType || 'neutral'

      // 平静度：高 valence 且低 arousal 时最高
      emotionData.calm = Math.round(Math.max(0, Math.min(100,
        ((valence + 1) / 2) * 100 - arousal * 15 + detectedScore * 20
      )))
      // 焦虑度：结合 arousal 和情绪类型
      emotionData.anxious = Math.round(Math.min(90, Math.max(0,
        arousal * 80 + (detectedType === 'anxiety' ? 15 : 0)
      )))
      // 低落度：低 valence + 低 arousal + depression 类型
      emotionData.sad = Math.round(Math.min(80, Math.max(0,
        (1 - (valence + 1) / 2) * 70 + (detectedType === 'depression' ? 20 : 0)
      )))

      // 更新画像
      profileStressLevel.value = detectedScore > 0.6 ? 'low' : detectedScore < 0.35 ? 'high' : 'medium'
    }

    if (summary) {
      // 更新对话摘要列表（基于后端统计的主题和高频关键词）
      const { themes = [], keywords = [], emotionTrend = 'stable', dominantEmotion = '' } = summary
      const prevTrend = profileTrend.value

      if (emotionTrend === 'improving') profileTrend.value = 'rising'
      else if (emotionTrend === 'worsening') profileTrend.value = 'falling'
      else profileTrend.value = 'stable'

      // 生成精准建议
      currentAdvice.value = generateAdvice(dominantEmotion, summary, prevTrend)

      // 推送主题词条目
      conversationSummary.value = []
      themes.forEach(theme => {
        conversationSummary.value.push(`💬 关注主题：${theme}`)
      })
      if (keywords.length > 0) {
        conversationSummary.value.push(`🔑 关键词：${keywords.slice(0, 4).join('、')}`)
      }
    }

    // 检查是否触发认知投票（基于真实情绪数据）
    try {
      const votingRes = await checkShouldTrigger(detectedType, detectedScore)
      if (votingRes.data?.shouldTrigger) {
        votingEmotionType.value = detectedType
        showVoting.value = true
      }
    } catch (err) {}
  } catch (err) {
    console.error('更新情绪分析失败', err)
  }
}

// 基于情绪数据生成精准辅导建议
const generateAdvice = (dominantEmotion, summary, prevTrend) => {
  const trend = summary.emotionTrend || 'stable'
  const score = summary.totalRecords > 0 ? (summary.keywords?.length || 0) : 0

  // 情绪改善趋势
  if (trend === 'improving' || prevTrend === 'rising') {
    return '继续保持！您的情绪状态正在好转，可以尝试记录每天的小确幸，巩固积极体验。'
  }
  // 情绪恶化趋势
  if (trend === 'worsening' || prevTrend === 'falling') {
    return '建议尝试5分钟深呼吸或渐进式肌肉放松，帮助缓解当前的不适感。'
  }

  switch (dominantEmotion) {
    case 'anxiety':
      return '检测到明显的焦虑情绪。建议通过腹式呼吸（4秒吸气、7秒屏气、8秒呼气）平复神经系统。'
    case 'depression':
      return '情绪低落时，可以尝试做一件小事（整理房间、散步），行动往往能带动情绪改善。'
    case 'anger':
      return '愤怒是一种有能量的情绪。先离开让你烦躁的场景，尝试做10次深呼吸再回来处理。'
    case 'positive':
      return '您的情绪状态积极。继续关注让自己开心的事物，保持这份好心情！'
    case 'negative':
      return '当前情绪较为消极，建议记录下负面想法并尝试换个角度思考，或与信任的人聊聊。'
    default:
      return '随着对话进行，将为您提供针对性的认知行为疗法干预建议。'
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  })
}

// 格式化消息（简单处理换行）
const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 打开危机帮助
const openCrisisHelp = () => {
  showCrisisAlert.value = false
  crisisDialogVisible.value = true
}

// 认知投票提交回调
const onVotingSubmitted = ({ type, option }) => {
  recentVotingType.value = type
  showVoting.value = false
}

// ================= 情绪报告面板 =================
const reportDays = ref(7)
const loadingReport = ref(false)
const reportTrendData = ref([])
const reportRecords = ref([])

const emotionStats = reactive({
  avgScore: '0.00',
  totalRecords: 0,
  totalSessions: 0
})

watch(reportDays, () => loadReportData())

const loadReportData = async () => {
  loadingReport.value = true
  try {
    const [emotionRes, sessionRes] = await Promise.all([
      getEmotionTrend(reportDays.value),
      getSessionList()
    ])
    reportRecords.value = emotionRes.data || []
    reportTrendData.value = buildReportTrend(reportRecords.value)
    emotionStats.totalRecords = reportRecords.value.length
    emotionStats.totalSessions = (sessionRes.data || []).length
    if (reportRecords.value.length > 0) {
      const total = reportRecords.value.reduce((s, r) => s + (r.emotionScore || 0), 0)
      emotionStats.avgScore = (total / reportRecords.value.length).toFixed(2)
    }
  } catch (err) {
    console.error('加载情绪报告失败', err)
  } finally {
    loadingReport.value = false
  }
}

const buildReportTrend = (records) => {
  const map = {}
  records.forEach(r => {
    const d = new Date(r.analysisTime)
    const date = `${d.getMonth() + 1}-${d.getDate()}`
    if (!map[date]) map[date] = []
    map[date].push(r.emotionScore || 0)
  })
  return Object.entries(map).sort(([a], [b]) => a.localeCompare(b))
    .map(([date, scores]) => ({ date, score: +(scores.reduce((s, v) => s + v, 0) / scores.length).toFixed(2) }))
}

const reportDistData = computed(() => {
  if (!reportRecords.value.length) return []
  const counts = {}
  reportRecords.value.forEach(r => {
    const type = r.emotionType || 'neutral'
    counts[type] = (counts[type] || 0) + 1
  })
  const total = reportRecords.value.length
  return Object.entries(counts).map(([type, count]) => ({ type, count }))
})

const reportTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: reportTrendData.value.map(d => d.date), axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8', fontSize: 11 } },
  yAxis: { type: 'value', min: 0, max: 1, axisLine: { show: false }, axisLabel: { color: '#94a3b8', fontSize: 11 }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [{
    type: 'line', smooth: true, data: reportTrendData.value.map(d => d.score),
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(107, 144, 128, 0.3)' }, { offset: 1, color: 'rgba(107, 144, 128, 0.02)' }] } },
    lineStyle: { color: '#6b9080', width: 2 },
    itemStyle: { color: '#6b9080' }, symbol: 'circle', symbolSize: 5
  }]
}))

const reportDistOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
  series: [{
    type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'],
    label: { show: false }, emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold' } },
    data: reportDistData.value.map(item => ({
      name: { positive: '积极', negative: '消极', anxiety: '焦虑', depression: '抑郁', anger: '愤怒', neutral: '中性' }[item.type] || item.type,
      value: item.count,
      itemStyle: { color: { positive: '#2ecc71', negative: '#e74c3c', anxiety: '#f39c12', depression: '#3498db', anger: '#c0392b', neutral: '#95a5a6' }[item.type] || '#95a5a6' }
    }))
  }]
}))

// ================= 正念空间面板 =================
const mindfulApiData = ref([])
const activeMindfulCat = ref('breathe')
const playingId = ref(null)
const playingExercise = ref(null)
const isPlaying = ref(false)
const playProgress = ref(0)
const playTime = ref(0)
let playTimer = null

const mindfulCatMap = {
  breathe: '呼吸放松',
  meditate: '正念冥想',
  visual: '意象放松',
  body: '身体放松'
}

const mindfulCats = [
  { id: 'breathe', label: '呼吸放松', icon: 'fa-solid fa-wind' },
  { id: 'meditate', label: '正念冥想', icon: 'fa-solid fa-om' },
  { id: 'visual', label: '意象放松', icon: 'fa-solid fa-water' },
  { id: 'body', label: '身体放松', icon: 'fa-solid fa-person' }
]

const exerciseColors = [
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #d299c2, #fef9d7)',
  'linear-gradient(135deg, #89f7fe, #66a6ff)',
  'linear-gradient(135deg, #f5af19, #f12711)',
  'linear-gradient(135deg, #c471f5, #fa71cd)',
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #11998e, #38ef7d)',
  'linear-gradient(135deg, #ff9a9e, #fecfef)'
]

const getExerciseColor = (id) => {
  const idx = Number(id) % exerciseColors.length
  return exerciseColors[idx] || exerciseColors[0]
}

const mindfulExercises = computed(() => mindfulApiData.value)

const currentExercises = computed(() => mindfulExercises.value.filter(ex => ex.subCategory === activeMindfulCat.value))

const loadMindfulData = async () => {
  try {
    const res = await getAllResources('mindfulness')
    mindfulApiData.value = res.data || []
    if (mindfulApiData.value.length > 0 && !mindfulApiData.value.find(ex => ex.subCategory === activeMindfulCat.value)) {
      activeMindfulCat.value = mindfulApiData.value[0].subCategory || 'breathe'
    }
  } catch (err) {
    console.error('加载正念数据失败', err)
  }
}

const toggleExercise = (ex) => {
  if (playingId.value === ex.id) {
    if (isPlaying.value) {
      isPlaying.value = false
      clearInterval(playTimer)
    } else {
      isPlaying.value = true
      startPlayTimer()
    }
  } else {
    playingId.value = ex.id
    playingExercise.value = ex
    playTime.value = 0
    playProgress.value = 0
    isPlaying.value = true
    clearInterval(playTimer)
    startPlayTimer()
  }
}

const startPlayTimer = () => {
  playTimer = setInterval(() => {
    if (!playingExercise.value) return
    playTime.value++
    playProgress.value = Math.min(100, (playTime.value / ((playingExercise.value.durationMinutes || 5) * 60)) * 100)
    if (playTime.value >= (playingExercise.value.durationMinutes || 5) * 60) {
      clearInterval(playTimer)
      isPlaying.value = false
      playProgress.value = 100
    }
  }, 1000)
}

const togglePlay = () => {
  if (isPlaying.value) {
    isPlaying.value = false
    clearInterval(playTimer)
  } else {
    isPlaying.value = true
    startPlayTimer()
  }
}

const stopExercise = () => {
  clearInterval(playTimer)
  playingId.value = null
  playingExercise.value = null
  isPlaying.value = false
  playProgress.value = 0
  playTime.value = 0
}

const formatPlayTime = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

// ================= 咨询档案面板 =================
const loadingArchive = ref(false)
const archiveStats = reactive({ totalSessions: 0, totalRecords: 0, totalCheckinDays: 0 })

const loadArchiveData = async () => {
  loadingArchive.value = true
  try {
    const [sessionRes, emotionRes, checkinRes] = await Promise.all([getSessionList(), getEmotionTrend(365), getCheckinStats(365)])
    sessions.value = sessionRes.data || []
    const emotions = emotionRes.data || []
    archiveStats.totalSessions = sessions.value.length
    archiveStats.totalRecords = emotions.length
    const checkinAll = checkinRes.data || {}
    archiveStats.totalCheckinDays = Object.values(checkinAll.stats || {}).reduce((a, b) => a + b, 0)
  } catch (err) {
    console.error('加载档案失败', err)
  } finally {
    loadingArchive.value = false
  }
}

const viewSession = async (session) => {
  currentNav.value = 'chat'
  await selectSession(session.id)
}

const jumpToReport = () => {
  currentNav.value = 'report'
  loadReportData()
}

const jumpToMindful = () => {
  currentNav.value = 'mindful'
  loadMindfulData()
}

const formatDate = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
}
</script>

<style scoped>
/* 容器布局 */
.home-container {
  height: 100vh;
  padding: 10px 12px;
}

.layout-container {
  display: grid;
  grid-template-columns: 220px 1fr 300px;
  grid-template-rows: 1fr;
  width: 100%;
  height: 100%;
  gap: 10px;
}

/* ================= 右侧三栏：AI 分析面板 + 打卡 + 画像（统一管理） ================= */
.right-sidebar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow-y: auto;
  padding-right: 2px;
}

.right-sidebar-wrapper::-webkit-scrollbar {
  width: 0px;
  background: transparent;
}

/* 心理资源快捷卡片 */
.resource-quick-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.resource-quick-card:hover {
  border-color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.rq-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, rgba(107, 144, 128, 0.15), rgba(107, 144, 128, 0.05));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 18px;
  flex-shrink: 0;
}

.rq-info {
  flex: 1;
  min-width: 0;
}

.rq-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 2px;
}

.rq-desc {
  font-size: 11px;
  color: var(--color-text-light);
}

.rq-arrow {
  color: var(--color-text-light);
  font-size: 12px;
  flex-shrink: 0;
}

.resource-quick-card:hover .rq-arrow {
  color: var(--color-primary);
}

/* ================= 右侧：分析面板 ================= */
.analysis-panel {
  display: flex;
  flex-direction: column;
  flex-shrink: 0; /* 防止在 flex 容器中被压缩 */
}
.sidebar {
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
}

.sidebar-content {
  flex: 1;
}

.brand {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
  padding: 0 10px;
}

.nav-menu {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  padding: 12px 16px;
  border-radius: var(--radius-md);
  color: var(--color-text-light);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-item:hover {
  background: var(--color-accent);
  color: var(--color-primary);
}

.nav-item.active {
  background: var(--color-primary);
  color: white;
}

.nav-item i {
  font-size: 15px;
  width: 18px;
  text-align: center;
}

/* ================= 中间：聊天区 ================= */
.chat-section {
  display: flex;
  flex-direction: column;
  position: relative;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-title {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-text);
}

.pulse {
  width: 8px;
  height: 8px;
  background: #4cd137;
  border-radius: 50%;
  box-shadow: 0 0 0 rgba(76, 209, 55, 0.4);
  animation: pulse 2s infinite;
}

.header-tools {
  display: flex;
  gap: 8px;
}

/* 用户下拉菜单 */
.user-dropdown {
  position: relative;
}

/* 模型切换按钮 */
.model-switch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: var(--color-text);
}

.model-switch-btn:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.model-switch-btn i:first-child {
  color: var(--color-primary);
  font-size: 12px;
}

.model-switch-label {
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-switch-btn .fa-caret-down {
  font-size: 10px;
  color: var(--color-text-light);
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: var(--color-text);
}

.user-btn:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.user-name {
  font-weight: 500;
}

.user-btn .fa-chevron-down {
  font-size: 10px;
  color: var(--color-text-light);
  transition: transform 0.2s;
}

.user-btn .fa-chevron-down.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 200px;
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  z-index: 100;
  overflow: hidden;
}

.dropdown-header {
  padding: 16px;
  background: var(--color-accent);
}

.dropdown-name {
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
}

.dropdown-role {
  display: flex;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.role-badge.admin {
  background: var(--color-primary);
  color: white;
}

.role-badge.user {
  background: rgba(107, 144, 128, 0.2);
  color: var(--color-primary);
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: var(--color-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.dropdown-item:hover {
  background: var(--color-accent);
  color: var(--color-primary);
}

.dropdown-item i {
  width: 16px;
  color: var(--color-text-light);
}

.dropdown-item:hover i {
  color: var(--color-primary);
}

.dropdown-item.logout {
  color: var(--color-crisis);
}

.dropdown-item.logout i {
  color: var(--color-crisis);
}

.dropdown-item.logout:hover {
  background: var(--color-crisis-bg);
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: all 0.2s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.btn-outline {
  border: 1px solid var(--color-primary-light);
  background: transparent;
  color: var(--color-primary);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-outline:hover {
  background: var(--color-primary-light);
  color: white;
}

/* 欢迎区 */
.welcome-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: var(--color-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: var(--color-primary);
  margin-bottom: 24px;
}

.welcome-area h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.welcome-area p {
  color: var(--color-text-light);
  margin-bottom: 32px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.quick-btn {
  padding: 12px 20px;
  border-radius: 24px;
  border: 1px solid var(--color-border);
  background: white;
  color: var(--color-text);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.quick-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

/* 聊天消息 */
.chat-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message {
  display: flex;
  gap: 16px;
  max-width: 85%;
  animation: fadeIn 0.4s ease;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bot .avatar {
  background: var(--color-accent);
  color: var(--color-primary);
}

.user .avatar {
  background: var(--color-primary);
  color: white;
}

.bubble {
  padding: 16px 20px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
}

.bot .bubble {
  background: #ffffff;
  border-top-left-radius: 4px;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.user .bubble {
  background: var(--color-primary);
  color: white;
  border-top-right-radius: 4px;
}

/* 打字指示器 */
.typing {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing span {
  width: 6px;
  height: 6px;
  background: var(--color-primary-light);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

/* 危机预警 */
.crisis-banner {
  position: absolute;
  bottom: 80px;
  left: 24px;
  right: 24px;
  background: var(--color-crisis-bg);
  border: 1px dashed var(--color-crisis-border);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.crisis-content {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-crisis);
  font-weight: 500;
}

.crisis-btn {
  background: var(--color-crisis);
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 16px;
  cursor: pointer;
  font-weight: 500;
}

.crisis-close {
  background: transparent;
  border: none;
  color: var(--color-crisis);
  cursor: pointer;
  padding: 4px 8px;
}

/* 输入区 */
.chat-input-area {
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.5);
  border-top: 1px solid var(--color-border);
}

.input-box {
  display: flex;
  align-items: center;
  background: white;
  padding: 8px 8px 8px 20px;
  border-radius: 24px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.input-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  background: transparent;
  color: var(--color-text);
}

.input-box input::placeholder {
  color: var(--color-text-light);
}

.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--color-text-light);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: 0.2s;
}

.icon-btn:hover {
  color: var(--color-primary);
  background: var(--color-accent);
}

.icon-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-send {
  background: var(--color-primary);
  color: white;
}

.btn-send:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: scale(1.05);
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header .badge {
  background: var(--color-primary-light);
  color: white;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.panel-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 情绪模块 */
.emotion-module h3 {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.emotion-bar-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.emotion-item {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.emotion-label {
  width: 40px;
  color: var(--color-text);
  font-weight: 500;
}

.bar-bg {
  flex: 1;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  margin: 0 10px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.bar-calm { background: var(--color-positive); }
.bar-anxious { background: var(--color-anxiety); }
.bar-sad { background: var(--color-depression); }

.emotion-value {
  width: 40px;
  font-size: 12px;
  color: var(--color-text-light);
  text-align: right;
}

/* 摘要模块 */
.summary-module,
.advice-module {
  background: white;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.summary-module h3,
.advice-module h3 {
  font-size: 13px;
  color: var(--color-text-light);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.advice-module h3 {
  color: #d35400;
}

.summary-list {
  padding-left: 20px;
  color: var(--color-text);
  font-size: 13px;
  line-height: 1.8;
}

.summary-placeholder,
.advice-placeholder {
  color: var(--color-text-muted);
  font-style: italic;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.advice-module p {
  color: var(--color-text-light);
  font-size: 13px;
  line-height: 1.6;
}

/* SOS 按钮 */
.sos-btn {
  margin-top: auto;
  background: var(--color-crisis-bg);
  color: var(--color-crisis);
  border: 1px dashed var(--color-crisis-border);
  padding: 14px;
  border-radius: var(--radius-md);
  text-align: center;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
  transition: 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.sos-btn:hover {
  background: #ffe4e4;
}

/* 历史抽屉 */
.history-drawer {
  position: fixed;
  top: 0;
  left: 0;
  width: 320px;
  height: 100vh;
  background: white;
  box-shadow: var(--shadow-lg);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.drawer-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer-header h3 {
  font-size: 16px;
  color: var(--color-text);
}

.drawer-close {
  background: transparent;
  border: none;
  color: var(--color-text-light);
  cursor: pointer;
  padding: 4px;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: 0.2s;
}

.history-item:hover {
  background: var(--color-accent);
}

.history-item.active {
  background: var(--color-primary-light);
  color: white;
}

.history-item i {
  font-size: 18px;
  color: var(--color-text-light);
}

.history-item.active i {
  color: white;
}

.history-info {
  flex: 1;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.history-time {
  font-size: 12px;
  color: var(--color-text-light);
}

.history-item.active .history-time {
  color: rgba(255, 255, 255, 0.8);
}

/* 危机对话框 */
.crisis-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.crisis-modal-content {
  background: white;
  border-radius: var(--radius-lg);
  padding: 40px;
  max-width: 420px;
  text-align: center;
}

.crisis-modal-icon {
  width: 80px;
  height: 80px;
  background: var(--color-crisis-bg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: var(--color-crisis);
  margin: 0 auto 24px;
}

.crisis-modal-content h2 {
  font-size: 24px;
  color: var(--color-text);
  margin-bottom: 16px;
}

.crisis-modal-content > p {
  color: var(--color-text-light);
  line-height: 1.6;
  margin-bottom: 24px;
}

.crisis-hotlines {
  background: var(--color-accent);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
}

.crisis-hotlines h3 {
  font-size: 14px;
  color: var(--color-text);
  margin-bottom: 16px;
}

.hotline-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.hotline-item:last-child {
  border-bottom: none;
}

.hotline-name {
  font-size: 13px;
  color: var(--color-text-light);
}

.hotline-number {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}

.crisis-modal-close {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: 0.2s;
}

.crisis-modal-close:hover {
  background: var(--color-primary-dark);
}

/* 过渡动画 */
.crisis-slide-enter-active,
.crisis-slide-leave-active {
  transition: all 0.3s ease;
}

.crisis-slide-enter-from,
.crisis-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.drawer-slide-enter-active,
.drawer-slide-leave-active {
  transition: transform 0.3s ease;
}

.drawer-slide-enter-from,
.drawer-slide-leave-to {
  transform: translateX(-100%);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* ========== 通用面板内容区 ========== */
.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== 情绪报告面板 ========== */
.report-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.report-stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-val {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-dark);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-light);
}

.report-chart,
.report-dist {
  padding: 20px;
}

.report-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.report-chart-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
}

.report-dist h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
}

.chart-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--color-text-muted);
  font-size: 13px;
}

.chart-empty i {
  font-size: 28px;
  opacity: 0.4;
}

/* ========== 正念空间面板 ========== */
.mindful-body {
  align-items: center;
  gap: 28px;
}

.mindful-header {
  text-align: center;
}

.mindful-icon-wrap {
  width: 64px;
  height: 64px;
  background: var(--color-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--color-primary);
  margin: 0 auto 16px;
}

.mindful-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-dark);
  margin-bottom: 6px;
}

.mindful-header p {
  font-size: 13px;
  color: var(--color-text-light);
}

.mindful-categories {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.mindful-cat {
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  background: white;
  color: var(--color-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.mindful-cat:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.mindful-cat.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.mindful-exercises {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  width: 100%;
}

.exercise-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.2s;
}

.exercise-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.exercise-card.playing {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(107, 144, 128, 0.2);
}

.exercise-cover {
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  position: relative;
}

.exercise-cover i {
  opacity: 0.9;
}

.playing-indicator {
  position: absolute;
  bottom: 8px;
  right: 10px;
  display: flex;
  gap: 3px;
  align-items: flex-end;
  height: 14px;
}

.playing-indicator span {
  width: 3px;
  background: white;
  border-radius: 2px;
  animation: bounce 0.8s infinite ease-in-out both;
}

.playing-indicator span:nth-child(1) { animation-delay: -0.2s; height: 8px; }
.playing-indicator span:nth-child(2) { animation-delay: -0.4s; height: 14px; }
.playing-indicator span:nth-child(3) { animation-delay: -0.6s; height: 10px; }

.exercise-info {
  padding: 12px;
}

.exercise-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 6px;
}

.exercise-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-light);
}

.exercise-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 正念播放器 */
.mindful-player {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.player-cover {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  flex-shrink: 0;
}

.player-info {
  flex: 1;
  min-width: 0;
}

.player-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.player-sub {
  font-size: 12px;
  color: var(--color-text-light);
  margin-bottom: 8px;
}

.player-progress {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-bar {
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  transition: width 1s linear;
}

.progress-time {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-text-light);
}

.player-controls {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.ctrl-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--color-primary);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s;
}

.ctrl-btn:hover {
  background: var(--color-primary-dark);
  transform: scale(1.05);
}

.player-slide-enter-active,
.player-slide-leave-active {
  transition: all 0.3s ease;
}

.player-slide-enter-from,
.player-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* ========== 咨询档案面板 ========== */
.archive-body {
  gap: 20px;
}

.archive-header {
  text-align: center;
}

.archive-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 6px;
}

.archive-header h2 i {
  color: var(--color-primary);
}

.archive-header p {
  font-size: 13px;
  color: var(--color-text-light);
}

.archive-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.archive-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px;
  text-align: center;
}

.archive-stat.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.archive-stat.clickable:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.archive-stat i {
  font-size: 20px;
  color: var(--color-primary);
}

.astat-val {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-dark);
  line-height: 1;
}

.astat-label {
  font-size: 12px;
  color: var(--color-text-light);
}

.archive-section {
  padding: 20px;
}

.archive-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.archive-section-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  display: flex;
  align-items: center;
  gap: 8px;
}

.archive-section-header h3 i {
  color: var(--color-primary);
  font-size: 13px;
}

.badge {
  padding: 2px 10px;
  background: var(--color-primary);
  color: white;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.list-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 40px 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.list-empty i {
  font-size: 32px;
  opacity: 0.4;
}

.session-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.2s;
}

.session-card:hover {
  background: white;
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.session-icon {
  width: 40px;
  height: 40px;
  background: var(--color-accent);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 16px;
  flex-shrink: 0;
}

.session-detail {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.session-date {
  font-size: 12px;
  color: var(--color-text-light);
}

.session-arrow {
  color: var(--color-text-light);
  font-size: 12px;
}

.session-card:hover .session-arrow {
  color: var(--color-primary);
}
</style>
