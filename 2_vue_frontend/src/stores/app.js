import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 抽屉状态
  const emotionDrawerVisible = ref(false)
  const resourceDrawerVisible = ref(false)

  // 弹窗状态
  const modelDialogVisible = ref(false)

  // 当前会话 ID
  const currentSessionId = ref(null)

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

  // 更新当前会话
  const setCurrentSession = (sessionId) => {
    currentSessionId.value = sessionId
  }

  // 更新当前情绪
  const setCurrentEmotion = (type, score) => {
    currentEmotion.value = { type, score }
  }

  return {
    emotionDrawerVisible,
    resourceDrawerVisible,
    modelDialogVisible,
    currentSessionId,
    currentEmotion,
    openEmotionDrawer,
    closeEmotionDrawer,
    openResourceDrawer,
    closeResourceDrawer,
    openModelDialog,
    closeModelDialog,
    setCurrentSession,
    setCurrentEmotion
  }
})
