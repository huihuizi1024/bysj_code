<template>
  <el-drawer
    v-model="appStore.resourceDrawerVisible"
    title="心灵资源库"
    direction="rtl"
    size="500px"
  >
    <template #header>
      <div class="drawer-header">
        <span class="title">📚 心灵资源库</span>
      </div>
    </template>

    <div class="resource-content">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索资源..."
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- 分类标签 -->
      <div class="category-tabs">
        <el-radio-group v-model="activeCategory" size="default" @change="loadResources">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="crisis">⚠️ 危机热线</el-radio-button>
          <el-radio-button value="selfhelp">🌿 自我练习</el-radio-button>
          <el-radio-button value="counseling">💬 心理咨询</el-radio-button>
          <el-radio-button value="mindfulness">🧘 正念冥想</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 加载状态 -->
      <div v-loading="loading" class="resource-list">
        <!-- 危机热线（始终置顶） -->
        <template v-if="activeCategory === '' || activeCategory === 'crisis'">
          <div
            v-for="item in crisisResources"
            :key="item.id"
            class="resource-card crisis-card"
            @click="showDetail(item)"
          >
            <div class="card-header">
              <span class="card-icon">📞</span>
              <span class="card-category">危机热线</span>
            </div>
            <h4 class="card-title">{{ item.title }}</h4>
            <p class="card-desc">{{ item.content?.substring(0, 50) }}...</p>
            <div class="card-actions">
              <el-button type="primary" size="small" @click.stop="copyPhone(item)">
                复制号码
              </el-button>
            </div>
          </div>
        </template>

        <!-- 普通资源列表 -->
        <div
          v-for="item in normalResources"
          :key="item.id"
          class="resource-card"
          @click="showDetail(item)"
        >
          <div class="card-header">
            <span class="card-icon">{{ categoryIcons[item.category] || '📖' }}</span>
            <span class="card-category">{{ categoryLabels[item.category] || item.category }}</span>
            <el-tag
              v-if="item.triggerEmotion"
              size="small"
              :type="getEmotionType(item.triggerEmotion)"
            >
              {{ emotionLabels[item.triggerEmotion] || item.triggerEmotion }}
            </el-tag>
          </div>
          <h4 class="card-title">{{ item.title }}</h4>
          <p class="card-desc">{{ item.applicableScene || item.content?.substring(0, 60) }}...</p>
          <div class="card-footer">
            <span class="card-priority" v-if="item.priority < 50">推荐</span>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!loading && normalResources.length === 0 && crisisResources.length === 0"
          description="暂无相关资源"
          :image-size="80"
        />
      </div>
    </div>

    <!-- 资源详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentResource?.title"
      width="450px"
      :close-on-click-modal="true"
    >
      <div class="detail-content" v-if="currentResource">
        <div class="detail-meta">
          <el-tag>{{ categoryLabels[currentResource.category] || currentResource.category }}</el-tag>
          <el-tag
            v-if="currentResource.resourceType"
            type="info"
          >
            {{ currentResource.resourceType }}
          </el-tag>
        </div>

        <div class="detail-body" v-html="formatContent(currentResource.content)"></div>

        <div class="detail-scene" v-if="currentResource.applicableScene">
          <h4>适用场景</h4>
          <p>{{ currentResource.applicableScene }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="useResource">使用这个方法</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { getAllResources } from '@/api/resource'

const appStore = useAppStore()

const loading = ref(false)
const keyword = ref('')
const activeCategory = ref('')
const resources = ref([])
const detailVisible = ref(false)
const currentResource = ref(null)

const categoryLabels = {
  crisis: '危机热线',
  selfhelp: '自我练习',
  counseling: '心理咨询',
  mindfulness: '正念冥想',
  tips: '自助技巧'
}

const categoryIcons = {
  crisis: '📞',
  selfhelp: '🌿',
  counseling: '💬',
  mindfulness: '🧘',
  tips: '💡'
}

const emotionLabels = {
  anxiety: '焦虑',
  depression: '抑郁',
  anger: '愤怒',
  all: '全部'
}

const emotionTypeMap = {
  anxiety: 'warning',
  depression: 'info',
  anger: 'danger',
  all: ''
}

// 危机热线（始终置顶）
const crisisResources = computed(() => {
  return resources.value.filter(r => r.category === 'crisis')
})

// 普通资源
const normalResources = computed(() => {
  let list = resources.value.filter(r => r.category !== 'crisis')

  if (activeCategory.value) {
    list = list.filter(r => r.category === activeCategory.value)
  }

  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(r =>
      r.title?.toLowerCase().includes(kw) ||
      r.content?.toLowerCase().includes(kw)
    )
  }

  return list
})

const getEmotionType = (emotion) => {
  return emotionTypeMap[emotion] || ''
}

const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 加载资源
const loadResources = async () => {
  loading.value = true
  try {
    const res = await getAllResources()
    resources.value = res.data || []
  } catch (err) {
    console.error('加载资源失败', err)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 搜索会在 computed 中自动处理
}

// 显示详情
const showDetail = (item) => {
  currentResource.value = item
  detailVisible.value = true
}

// 复制号码
const copyPhone = async (item) => {
  const phone = item.content?.match(/\d{3,4}[-\s]?\d{7,8}|\d{11}/)?.[0]
  if (phone) {
    await navigator.clipboard.writeText(phone)
    ElMessage.success(`已复制: ${phone}`)
  } else {
    ElMessage.warning('未找到电话号码')
  }
}

// 使用资源
const useResource = () => {
  ElMessage.success('祝您使用愉快！')
  detailVisible.value = false
}

// 抽屉打开时加载数据
watch(() => appStore.resourceDrawerVisible, (visible) => {
  if (visible) {
    loadResources()
  }
})
</script>

<style scoped>
.resource-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.drawer-header .title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-dark);
}

.search-box {
  padding: 0 4px;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 4px;
}

.category-tabs :deep(.el-radio-button__inner) {
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  margin-right: 8px;
  margin-bottom: 8px;
}

.resource-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 4px;
  min-height: 300px;
}

.resource-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.resource-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.crisis-card {
  background: var(--color-crisis-bg);
  border-color: var(--color-crisis);
}

.crisis-card:hover {
  box-shadow: 0 4px 16px rgba(229, 115, 115, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.card-icon {
  font-size: 18px;
}

.card-category {
  font-size: 12px;
  color: var(--color-text-light);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 6px;
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.5;
  margin-bottom: 8px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.card-priority {
  font-size: 11px;
  color: #67C23A;
  background: rgba(103, 194, 58, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

/* 详情弹窗 */
.detail-content {
  padding: 8px 0;
}

.detail-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-body {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text);
  margin-bottom: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.detail-scene {
  background: var(--color-bg);
  padding: 12px;
  border-radius: var(--radius-sm);
}

.detail-scene h4 {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 6px;
}

.detail-scene p {
  font-size: 13px;
  color: var(--color-text-light);
}
</style>
