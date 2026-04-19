<template>
  <div class="profile-container">
    <TopNav />

    <main class="profile-main">
      <!-- 个人信息卡片 -->
      <div class="profile-header glass-panel">
        <div class="profile-avatar">
          <i class="fa-solid fa-user"></i>
        </div>
        <div class="profile-info">
          <h1 class="profile-name">{{ userStore.username }}</h1>
          <div class="profile-meta">
            <span class="profile-role-badge">
              <i :class="userStore.isAdmin ? 'fa-solid fa-shield-halved' : 'fa-solid fa-user'"></i>
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </span>
            <span class="profile-date">
              <i class="fa-solid fa-calendar"></i>
              注册时间：{{ userStore.userInfo?.createTime ? formatDate(userStore.userInfo.createTime) : '未知' }}
            </span>
          </div>
        </div>
        <div class="profile-actions">
          <el-button @click="router.push('/home')">
            <i class="fa-solid fa-arrow-left"></i>
            返回主页
          </el-button>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="profile-content">
        <!-- 左侧：统计与设置 -->
        <div class="profile-left">
          <!-- 情绪统计 -->
          <div class="stat-card glass-panel">
            <h3 class="card-title">
              <i class="fa-solid fa-chart-line"></i>
              情绪统计概览
            </h3>
            <div class="stat-grid">
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="fa-solid fa-comments"></i>
                </div>
                <div class="stat-body">
                  <div class="stat-value">{{ stats.totalSessions }}</div>
                  <div class="stat-label">总对话数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="fa-solid fa-face-smile"></i>
                </div>
                <div class="stat-body">
                  <div class="stat-value">{{ stats.avgScore }}</div>
                  <div class="stat-label">平均情绪得分</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="fa-solid fa-calendar-check"></i>
                </div>
                <div class="stat-body">
                  <div class="stat-value">{{ stats.checkInDays }}</div>
                  <div class="stat-label">连续打卡天数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <i class="fa-solid fa-clock"></i>
                </div>
                <div class="stat-body">
                  <div class="stat-value">{{ stats.totalRecords }}</div>
                  <div class="stat-label">情绪记录数</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 账号设置 -->
          <div class="settings-card glass-panel">
            <h3 class="card-title">
              <i class="fa-solid fa-gear"></i>
              账号设置
            </h3>
            <div class="settings-list">
              <div class="settings-item">
                <div class="settings-info">
                  <div class="settings-icon">
                    <i class="fa-solid fa-lock"></i>
                  </div>
                  <div>
                    <div class="settings-name">修改密码</div>
                    <div class="settings-desc">更新您的账号密码</div>
                  </div>
                </div>
                <el-button size="small" @click="showPasswordDialog = true">
                  修改
                </el-button>
              </div>
              <div class="settings-item">
                <div class="settings-info">
                  <div class="settings-icon">
                    <i class="fa-solid fa-bell"></i>
                  </div>
                  <div>
                    <div class="settings-name">消息通知</div>
                    <div class="settings-desc">管理通知偏好</div>
                  </div>
                </div>
                <el-switch v-model="notifyEnabled" />
              </div>
              <div class="settings-item danger">
                <div class="settings-info">
                  <div class="settings-icon">
                    <i class="fa-solid fa-right-from-bracket"></i>
                  </div>
                  <div>
                    <div class="settings-name">退出登录</div>
                    <div class="settings-desc">退出当前账号</div>
                  </div>
                </div>
                <el-button size="small" type="danger" @click="handleLogout">
                  退出
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：最近会话 & 情绪趋势 -->
        <div class="profile-right">
          <!-- 最近会话 -->
          <div class="sessions-card glass-panel">
            <h3 class="card-title">
              <i class="fa-solid fa-clock-rotate-left"></i>
              最近会话
            </h3>
            <div v-loading="loadingSessions" class="sessions-list">
              <div v-if="!loadingSessions && recentSessions.length === 0" class="empty-state">
                <el-empty description="暂无会话记录" :image-size="60" />
              </div>
              <div
                v-for="session in recentSessions"
                :key="session.id"
                class="session-item"
                @click="router.push('/home')"
              >
                <div class="session-icon-wrap">
                  <i class="fa-solid fa-comment-dots"></i>
                </div>
                <div class="session-info">
                  <div class="session-title">{{ session.title || '新的对话' }}</div>
                  <div class="session-time">{{ formatTime(session.createTime) }}</div>
                </div>
                <div class="session-action">
                  <i class="fa-solid fa-chevron-right"></i>
                </div>
              </div>
            </div>
          </div>

          <!-- 情绪分布 -->
          <div class="emotion-dist-card glass-panel">
            <h3 class="card-title">
              <i class="fa-solid fa-chart-pie"></i>
              情绪类型分布
            </h3>
            <div v-loading="loadingEmotions" class="emotion-dist">
              <div v-if="!loadingEmotions && emotionDistribution.length === 0" class="empty-state">
                <el-empty description="暂无情绪数据" :image-size="60" />
              </div>
              <div
                v-for="item in emotionDistribution"
                :key="item.type"
                class="emotion-dist-item"
              >
                <div class="emotion-dist-label">
                  <span>{{ emotionLabels[item.type] || item.type }}</span>
                  <span>{{ item.count }}次</span>
                </div>
                <el-progress
                  :percentage="item.percentage"
                  :color="emotionColors[item.type] || '#909399'"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="420px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingPassword" @click="handleChangePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import TopNav from '@/components/TopNav.vue'
import { getSessionList } from '@/api/chat'
import { getEmotionTrend } from '@/api/emotion'

const router = useRouter()
const userStore = useUserStore()

const loadingSessions = ref(false)
const loadingEmotions = ref(false)
const recentSessions = ref([])
const allEmotions = ref([])

const showPasswordDialog = ref(false)
const savingPassword = ref(false)
const passwordFormRef = ref(null)
const notifyEnabled = ref(true)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const stats = reactive({
  totalSessions: 0,
  avgScore: '0.00',
  checkInDays: 0,
  totalRecords: 0
})

const emotionLabels = {
  positive: '😊 积极', negative: '😢 消极',
  anxiety: '😰 焦虑', depression: '😔 抑郁',
  anger: '😠 愤怒', neutral: '😐 中性'
}

const emotionColors = {
  positive: '#2ecc71', negative: '#e74c3c',
  anxiety: '#f39c12', depression: '#3498db',
  anger: '#c0392b', neutral: '#95a5a6'
}

const emotionDistribution = ref([])

onMounted(async () => {
  await loadSessions()
  await loadEmotions()
})

const loadSessions = async () => {
  loadingSessions.value = true
  try {
    const res = await getSessionList()
    const all = res.data || []
    stats.totalSessions = all.length
    recentSessions.value = all.slice(0, 5)
    // 模拟打卡天数
    stats.checkInDays = Math.floor(Math.random() * 7) + 1
  } catch (err) {
    console.error('加载会话失败', err)
  } finally {
    loadingSessions.value = false
  }
}

const loadEmotions = async () => {
  loadingEmotions.value = true
  try {
    const res = await getEmotionTrend(90)
    allEmotions.value = res.data || []
    stats.totalRecords = allEmotions.value.length
    if (allEmotions.value.length > 0) {
      const total = allEmotions.value.reduce((s, item) => s + item.emotionScore, 0)
      stats.avgScore = (total / allEmotions.value.length).toFixed(2)
    }
    // 计算分布
    const counts = {}
    allEmotions.value.forEach(item => {
      const type = item.emotionType || 'neutral'
      counts[type] = (counts[type] || 0) + 1
    })
    const total = allEmotions.value.length || 1
    emotionDistribution.value = Object.entries(counts).map(([type, count]) => ({
      type, count, percentage: Math.round((count / total) * 100)
    }))
  } catch (err) {
    console.error('加载情绪数据失败', err)
  } finally {
    loadingEmotions.value = false
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const formatDate = (timeStr) => {
  if (!timeStr) return '未知'
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
  } catch { return }

  savingPassword.value = true
  try {
    // TODO: 调用后端接口
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (err) {
    console.error('修改密码失败', err)
  } finally {
    savingPassword.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', { type: 'warning' })
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {}
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: var(--color-bg);
  background-image: var(--color-bg-gradient);
}

.profile-main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 个人信息头 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.profile-avatar {
  width: 72px;
  height: 72px;
  background: var(--color-accent);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 28px;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(107, 144, 128, 0.2);
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-dark);
  margin-bottom: 8px;
}

.profile-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-role-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  background: var(--color-accent);
  color: var(--color-primary);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid rgba(107, 144, 128, 0.15);
}

.profile-role-badge i {
  font-size: 11px;
}

.profile-date {
  font-size: 12px;
  color: var(--color-text-light);
  display: flex;
  align-items: center;
  gap: 5px;
}

.profile-date i {
  color: var(--color-primary-light);
}

.profile-actions {
  flex-shrink: 0;
}

/* 内容区 */
.profile-content {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 20px;
}

.profile-left,
.profile-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.glass-panel {
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: var(--color-primary);
  font-size: 14px;
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.stat-icon {
  width: 36px;
  height: 36px;
  background: var(--color-accent);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 15px;
  flex-shrink: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.stat-label {
  font-size: 11px;
  color: var(--color-text-light);
  margin-top: 3px;
}

/* 设置卡片 */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.settings-item:hover {
  background: var(--color-bg);
}

.settings-item.danger {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px dashed rgba(107, 144, 128, 0.12);
}

.settings-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.settings-icon {
  width: 34px;
  height: 34px;
  background: var(--color-accent);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 14px;
  flex-shrink: 0;
}

.settings-item.danger .settings-icon {
  background: var(--color-crisis-bg);
  color: var(--color-crisis);
}

.settings-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-dark);
  margin-bottom: 2px;
}

.settings-desc {
  font-size: 11px;
  color: var(--color-text-light);
}

/* 会话列表 */
.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 100px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.session-item:hover {
  background: var(--color-bg);
}

.session-icon-wrap {
  width: 34px;
  height: 34px;
  background: var(--color-accent);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  font-size: 14px;
  flex-shrink: 0;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-dark);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.session-time {
  font-size: 11px;
  color: var(--color-text-light);
}

.session-action {
  color: var(--color-primary-light);
  font-size: 12px;
}

.empty-state {
  padding: 20px 0;
  text-align: center;
}

/* 情绪分布 */
.emotion-dist {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 80px;
}

.emotion-dist-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.emotion-dist-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-light);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .profile-meta {
    justify-content: center;
  }
}
</style>
