import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentModel } from '@/api/model'

export const useAppStore = defineStore('app', () => {
  // 抽屉状态
  const emotionDrawerVisible = ref(false)
  const resourceDrawerVisible = ref(false)

  // 弹窗状态
  const modelDialogVisible = ref(false)
  const crisisAlertVisible = ref(false)
  const currentCrisisAlert = ref(null)

  // 当前会话 ID
  const currentSessionId = ref(null)

  // 当前选中的 AI 模型
  const currentModel = ref({ code: '', name: '' })

  // 当前情绪状态（用于资源推荐）
  const currentEmotion = ref({
    type: 'neutral',
    score: 0.5
  })

  // 打开/关闭抽屉
  const openEmotionDrawer = () => { emotionDrawerVisible.value = true }
  const closeEmotionDrawer = () => { emotionDrawerVisible.value = false }
  const openResourceDrawer = () => { resourceDrawerVisible.value = true }
  const closeResourceDrawer = () => { resourceDrawerVisible.value = false }

  // 打开/关闭弹窗
  const openModelDialog = () => { modelDialogVisible.value = true }
  const closeModelDialog = () => { modelDialogVisible.value = false }
  const openCrisisAlert = (alertData) => {
    currentCrisisAlert.value = alertData
    crisisAlertVisible.value = true
  }

  // 更新当前会话
  const setCurrentSession = (sessionId) => {
    currentSessionId.value = sessionId
  }

  // 更新当前情绪
  const setCurrentEmotion = (type, score) => {
    currentEmotion.value = { type, score }
  }

  // 加载当前选中的 AI 模型
  const loadCurrentModel = async () => {
    try {
      const res = await getCurrentModel()
      if (res.data?.code) {
        currentModel.value = {
          code: res.data.code,
          name: res.data.name || res.data.code
        }
      }
    } catch (err) {
      console.warn('加载当前模型失败', err)
    }
  }

  return {
    emotionDrawerVisible,
    resourceDrawerVisible,
    modelDialogVisible,
    crisisAlertVisible,
    currentCrisisAlert,
    currentSessionId,
    currentModel,
    currentEmotion,
    openEmotionDrawer,
    closeEmotionDrawer,
    openResourceDrawer,
    closeResourceDrawer,
    openModelDialog,
    closeModelDialog,
    openCrisisAlert,
    setCurrentSession,
    setCurrentEmotion,
    loadCurrentModel
  }
})
