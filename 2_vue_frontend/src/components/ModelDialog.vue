<template>
  <el-dialog
    v-model="appStore.modelDialogVisible"
    title="选择 AI 模型"
    width="500px"
    :close-on-click-modal="true"
  >
    <div class="model-list" v-loading="loading">
      <div
        v-for="model in modelList"
        :key="model.code"
        :class="['model-card', { active: model.code === currentCode }]"
        @click="selectModel(model)"
      >
        <div class="model-header">
          <span class="model-icon">{{ getModelIcon(model.code) }}</span>
          <div class="model-info">
            <span class="model-name">{{ model.name }}</span>
            <span class="model-code">{{ model.code }}</span>
          </div>
          <el-tag v-if="model.code === currentCode" type="success" size="small">
            当前使用
          </el-tag>
        </div>
        <p class="model-desc">{{ model.description }}</p>
        <div class="model-meta" v-if="model.modelName">
          <span>模型: {{ model.modelName }}</span>
        </div>
      </div>

      <el-empty
        v-if="!loading && modelList.length === 0"
        description="暂无可用模型"
        :image-size="80"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { getModelList, getCurrentModel, selectModel as selectModelApi } from '@/api/model'

const appStore = useAppStore()

const loading = ref(false)
const modelList = ref([])
const currentCode = ref('')

const modelIcons = {
  'DEEPSEEK': '🧠',
  'OPENAI': '🤖',
  'KIMI': '🌙',
  'LOCAL': '💻',
  'QWEN': '🐰',
  'GLM': '📊',
  'ERNIE': '🦅',
  'SPARK': '✨',
  'default': '🔮'
}

const getModelIcon = (code) => {
  const upperCode = (code || '').toUpperCase()
  for (const key of Object.keys(modelIcons)) {
    if (upperCode.includes(key)) {
      return modelIcons[key]
    }
  }
  return modelIcons.default
}

const selectModel = async (model) => {
  if (model.code === currentCode.value) {
    appStore.modelDialogVisible = false
    return
  }

  try {
    await selectModelApi(model.code)
    currentCode.value = model.code
    ElMessage.success(`已切换到 ${model.name}`)
    appStore.modelDialogVisible = false
  } catch (err) {
    ElMessage.error('切换失败，请重试')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    // 并行加载模型列表和当前模型
    const [listRes, currentRes] = await Promise.all([
      getModelList(),
      getCurrentModel()
    ])

    modelList.value = listRes.data || []
    currentCode.value = currentRes.data?.code || ''
  } catch (err) {
    console.error('加载模型数据失败', err)
  } finally {
    loading.value = false
  }
}

watch(() => appStore.modelDialogVisible, (visible) => {
  if (visible) {
    loadData()
  }
})
</script>

<style scoped>
.model-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.model-card {
  background: var(--color-bg);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.model-card:hover {
  background: #fff;
  box-shadow: var(--shadow-sm);
}

.model-card.active {
  background: #fff;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(124, 156, 181, 0.2);
}

.model-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.model-icon {
  font-size: 32px;
}

.model-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.model-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-dark);
}

.model-code {
  font-size: 11px;
  color: var(--color-text-light);
}

.model-desc {
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.5;
  margin-bottom: 8px;
}

.model-meta {
  font-size: 11px;
  color: var(--color-text-light);
  padding-top: 8px;
  border-top: 1px dashed rgba(0,0,0,0.08);
}

.model-meta span {
  margin-right: 12px;
}
</style>
