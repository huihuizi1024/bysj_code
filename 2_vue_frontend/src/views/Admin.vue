<template>
  <div class="admin-container">
    <!-- 顶部导航 -->
    <TopNav />

    <!-- 主内容 -->
    <main class="admin-main">
      <div class="admin-header">
        <h1>管理后台</h1>
        <p>欢迎回来，{{ userStore.username }}</p>
      </div>

      <!-- 功能切换 -->
      <div class="admin-tabs">
        <el-radio-group v-model="activeTab" size="large">
          <el-radio-button value="crisis">
            <el-badge :value="pendingCount" :hidden="pendingCount === 0" type="danger">
              危机预警
            </el-badge>
          </el-radio-button>
          <el-radio-button value="resource">资源管理</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 危机预警面板 -->
      <div v-if="activeTab === 'crisis'" class="panel crisis-panel">
        <div class="panel-header">
          <h2>🔴 危机预警</h2>
          <el-button @click="loadAlerts">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <!-- 状态筛选 -->
        <div class="alert-filter">
          <el-radio-group v-model="alertStatus" size="default">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="pending">待处理</el-radio-button>
            <el-radio-button value="handled">已处理</el-radio-button>
          </el-radio-group>
        </div>

        <div v-loading="loadingAlerts" class="alert-list">
          <el-empty
            v-if="!loadingAlerts && alerts.length === 0"
            description="暂无危机预警"
            :image-size="80"
          />

          <div
            v-for="alert in alerts"
            :key="alert.id"
            :class="['alert-card', `level-${alert.alertLevel}`]"
          >
            <div class="alert-header">
              <span class="alert-level">
                {{ levelLabels[alert.alertLevel] }}
              </span>
              <span :class="['alert-status', `status-${alert.status}`]">
                {{ statusLabels[alert.status] }}
              </span>
              <span class="alert-time">{{ formatTime(alert.createdAt) }}</span>
            </div>
            <div class="alert-content">
              <p><strong>用户:</strong> {{ alert.username || `用户${alert.userId}` }}</p>
              <p><strong>类型:</strong> {{ alert.alertType }}</p>
              <p><strong>触发内容:</strong> {{ alert.keywords }}</p>
            </div>
            <div class="alert-actions">
              <el-button type="primary" @click="showAlertDetail(alert)">
                查看详情
              </el-button>
              <el-button
                v-if="alert.status === 'pending'"
                type="success"
                @click="handleAlert(alert)"
              >
                标记已处理
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 资源管理面板 -->
      <div v-if="activeTab === 'resource'" class="panel resource-panel">
        <div class="panel-header">
          <h2>📚 资源管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="showResourceForm()">
              <el-icon><Plus /></el-icon>
              新增资源
            </el-button>
          </div>
        </div>

        <!-- 搜索筛选 -->
        <div class="filter-bar">
          <el-select v-model="filterCategory" placeholder="分类筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="危机热线" value="crisis" />
            <el-option label="自我练习" value="selfhelp" />
            <el-option label="心理咨询" value="counseling" />
            <el-option label="正念冥想" value="mindfulness" />
            <el-option label="自助技巧" value="tips" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索资源..."
            :prefix-icon="Search"
            clearable
            style="width: 200px;"
          />
        </div>

        <!-- 资源列表 -->
        <div v-loading="loadingResources" class="resource-table">
          <el-table :data="filteredResources" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="标题" min-width="150" />
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{ row }">
                {{ categoryLabels[row.category] || row.category }}
              </template>
            </el-table-column>
            <el-table-column prop="resourceType" label="类型" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="showResourceForm(row)">
                  编辑
                </el-button>
                <el-button link type="danger" @click="deleteResource(row)">
                  删除
                </el-button>
                <el-button link @click="toggleResource(row)">
                  {{ row.enabled ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </main>

    <!-- 资源表单弹窗 -->
    <el-dialog
      v-model="resourceFormVisible"
      :title="editingResource?.id ? '编辑资源' : '新增资源'"
      width="600px"
    >
      <el-form :model="resourceForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="resourceForm.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="resourceForm.category" placeholder="请选择分类">
            <el-option label="危机热线" value="crisis" />
            <el-option label="自我练习" value="selfhelp" />
            <el-option label="心理咨询" value="counseling" />
            <el-option label="正念冥想" value="mindfulness" />
            <el-option label="自助技巧" value="tips" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="resourceForm.resourceType" placeholder="如: hotline, exercise, tips" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="resourceForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入资源内容"
          />
        </el-form-item>
        <el-form-item label="适用场景">
          <el-input v-model="resourceForm.applicableScene" placeholder="如: 焦虑失眠时" />
        </el-form-item>
        <el-form-item label="触发情绪">
          <el-select v-model="resourceForm.triggerEmotion" placeholder="选择触发的情绪类型" clearable>
            <el-option label="焦虑" value="anxiety" />
            <el-option label="抑郁" value="depression" />
            <el-option label="愤怒" value="anger" />
            <el-option label="全部" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="resourceForm.priority" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="resourceForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resourceFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResource">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预警详情弹窗 -->
    <el-dialog
      v-model="alertDetailVisible"
      title="预警详情"
      width="500px"
    >
      <div v-if="currentAlert" class="alert-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户">{{ currentAlert.username || `用户${currentAlert.userId}` }}</el-descriptions-item>
          <el-descriptions-item label="预警级别">
            <el-tag :type="getLevelType(currentAlert.alertLevel)">
              {{ levelLabels[currentAlert.alertLevel] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预警类型">{{ currentAlert.alertType }}</el-descriptions-item>
          <el-descriptions-item label="触发关键词">{{ currentAlert.keywords }}</el-descriptions-item>
          <el-descriptions-item label="会话ID">{{ currentAlert.sessionId }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(currentAlert.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag>{{ statusLabels[currentAlert.status] }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="alertDetailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleAlert(currentAlert)">
          标记已处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import TopNav from '@/components/TopNav.vue'
import { useUserStore } from '@/stores/user'
import { getPendingAlerts, getAllAlerts, handleCrisisAlert } from '@/api/crisis'
import {
  getAdminResourceList,
  addResource,
  updateResource,
  deleteResource as deleteResourceApi,
  toggleResource as toggleResourceApi
} from '@/api/resource'

const router = useRouter()
const userStore = useUserStore()

// 状态
const activeTab = ref('crisis')
const loadingAlerts = ref(false)
const loadingResources = ref(false)
const alerts = ref([])
const resources = ref([])
const pendingCount = ref(0)
const alertStatus = ref('pending') // 默认显示待处理

// 资源管理
const filterCategory = ref('')
const searchKeyword = ref('')
const resourceFormVisible = ref(false)
const editingResource = ref(null)
const resourceForm = ref({
  title: '',
  category: '',
  resourceType: '',
  content: '',
  applicableScene: '',
  triggerEmotion: '',
  priority: 50,
  enabled: true
})

// 预警详情
const alertDetailVisible = ref(false)
const currentAlert = ref(null)

const categoryLabels = {
  crisis: '危机热线',
  selfhelp: '自我练习',
  counseling: '心理咨询',
  mindfulness: '正念冥想',
  tips: '自助技巧'
}

const levelLabels = {
  high: '🔴 高危',
  medium: '🟡 中危',
  low: '🟢 低危'
}

const statusLabels = {
  pending: '待处理',
  handled: '已处理',
  resolved: '已化解'
}

// 计算属性
const filteredResources = computed(() => {
  let list = resources.value
  if (filterCategory.value) {
    list = list.filter(r => r.category === filterCategory.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r =>
      r.title?.toLowerCase().includes(kw) ||
      r.content?.toLowerCase().includes(kw)
    )
  }
  return list
})

// 生命周期
onMounted(() => {
  if (!userStore.isAdmin) {
    ElMessage.warning('您没有管理员权限')
    router.push('/home')
    return
  }
  loadAlerts()
  loadResources()
})

// 监听状态筛选变化
watch(alertStatus, () => {
  loadAlerts()
})

// 加载危机预警
const loadAlerts = async () => {
  loadingAlerts.value = true
  try {
    // 获取所有预警，然后筛选待处理的用于显示徽标
    const res = await getAllAlerts(null)
    const allAlerts = res.data || []
    pendingCount.value = allAlerts.filter(a => a.status === 'pending').length
    // 根据状态筛选显示
    alerts.value = alertStatus.value
      ? allAlerts.filter(a => a.status === alertStatus.value)
      : allAlerts
  } catch (err) {
    console.error('加载预警失败', err)
  } finally {
    loadingAlerts.value = false
  }
}

// 加载资源列表
const loadResources = async () => {
  loadingResources.value = true
  try {
    const res = await getAdminResourceList({ pageNum: 1, pageSize: 100 })
    resources.value = res.data?.records || res.data || []
  } catch (err) {
    console.error('加载资源失败', err)
  } finally {
    loadingResources.value = false
  }
}

// 处理预警
const handleAlert = async (alert) => {
  try {
    await ElMessageBox.prompt('请输入处理备注（可选）', '处理危机预警', {
      confirmButtonText: '确认处理',
      cancelButtonText: '取消'
    })
    const { value } = await ElMessageBox.prompt('请输入处理备注（可选）', '处理危机预警', {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })

    await handleCrisisAlert(alert.id, value || '')
    ElMessage.success('已标记为已处理')
    alertDetailVisible.value = false
    loadAlerts()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('处理失败', err)
    }
  }
}

// 显示预警详情
const showAlertDetail = (alert) => {
  currentAlert.value = alert
  alertDetailVisible.value = true
}

// 显示资源表单
const showResourceForm = (resource = null) => {
  editingResource.value = resource
  if (resource) {
    resourceForm.value = { ...resource }
  } else {
    resourceForm.value = {
      title: '',
      category: '',
      resourceType: '',
      content: '',
      applicableScene: '',
      triggerEmotion: '',
      priority: 50,
      enabled: true
    }
  }
  resourceFormVisible.value = true
}

// 保存资源
const saveResource = async () => {
  if (!resourceForm.value.title || !resourceForm.value.category) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    if (editingResource.value?.id) {
      await updateResource(editingResource.value.id, resourceForm.value)
      ElMessage.success('更新成功')
    } else {
      await addResource(resourceForm.value)
      ElMessage.success('新增成功')
    }
    resourceFormVisible.value = false
    loadResources()
  } catch (err) {
    console.error('保存失败', err)
  }
}

// 删除资源
const deleteResource = async (resource) => {
  try {
    await ElMessageBox.confirm('确定要删除这个资源吗？', '删除确认', {
      type: 'warning'
    })
    await deleteResourceApi(resource.id)
    ElMessage.success('删除成功')
    loadResources()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败', err)
    }
  }
}

// 切换启用状态
const toggleResource = async (resource) => {
  try {
    await toggleResourceApi(resource.id, resource.enabled ? 0 : 1)
    resource.enabled = resource.enabled ? 0 : 1
    ElMessage.success(resource.enabled ? '已启用' : '已禁用')
  } catch (err) {
    console.error('切换失败', err)
  }
}

// 获取级别类型
const getLevelType = (level) => {
  const map = { high: 'danger', medium: 'warning', low: 'success' }
  return map[level] || 'info'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: var(--color-bg);
}

.admin-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.admin-header {
  margin-bottom: 24px;
}

.admin-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 4px;
}

.admin-header p {
  color: var(--color-text-light);
}

.admin-tabs {
  margin-bottom: 24px;
}

.panel {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-dark);
}

/* 危机预警 */
.alert-filter {
  margin-bottom: 16px;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.alert-card {
  border-radius: var(--radius-md);
  padding: 16px;
  border-left: 4px solid;
}

.alert-card.level-high {
  background: var(--color-crisis-bg);
  border-color: var(--color-crisis);
}

.alert-card.level-medium {
  background: #FFFBF0;
  border-color: #E6A23C;
}

.alert-card.level-low {
  background: #F0FDF4;
  border-color: #67C23A;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.alert-level {
  font-weight: 600;
  font-size: 15px;
}

.alert-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 8px;
}

.alert-status.status-pending {
  background: #FEF2F2;
  color: #DC2626;
}

.alert-status.status-handled {
  background: #F0FDF4;
  color: #16A34A;
}

.alert-time {
  font-size: 12px;
  color: var(--color-text-light);
}

.alert-content p {
  margin: 4px 0;
  font-size: 14px;
  color: var(--color-text);
}

.alert-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

/* 资源管理 */
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.resource-table {
  min-height: 200px;
}
</style>
