<template>
  <el-dialog
    :model-value="visible"
    title="危机帮助"
    width="500px"
    :close-on-click-modal="false"
    @close="$emit('close')"
  >
    <div class="crisis-content">
      <div class="crisis-header">
        <div class="crisis-icon">⚠️</div>
        <h3>如果您正处于困境中</h3>
      </div>

      <p class="crisis-intro">
        我们注意到您可能正在经历困难的时刻。请记住，<strong>您不是一个人</strong>，
        有很多人关心您，愿意帮助您度过难关。
      </p>

      <div class="hotline-section">
        <h4>📞 全国心理援助热线</h4>
        <div class="hotline-card">
          <div class="hotline-item">
            <span class="hotline-name">全国心理危机干预热线</span>
            <span class="hotline-number">400-161-9995</span>
            <el-button type="primary" size="small" @click="copyNumber('400-161-9995')">
              复制
            </el-button>
          </div>
          <div class="hotline-item">
            <span class="hotline-name">北京心理危机研究与干预中心</span>
            <span class="hotline-number">010-82951332</span>
            <el-button type="primary" size="small" @click="copyNumber('010-82951332')">
              复制
            </el-button>
          </div>
          <div class="hotline-item">
            <span class="hotline-name">生命热线</span>
            <span class="hotline-number">400-821-1215</span>
            <el-button type="primary" size="small" @click="copyNumber('400-821-1215')">
              复制
            </el-button>
          </div>
        </div>
      </div>

      <div class="tips-section">
        <h4>💡 如果您现在很安全，可以尝试</h4>
        <ul>
          <li>找一个安静的地方，深呼吸</li>
          <li>给信任的朋友或家人打电话</li>
          <li>写下您的感受</li>
          <li>进行一些简单的放松练习</li>
        </ul>
      </div>

      <div class="crisis-footer">
        <p>记住，寻求帮助是勇敢的表现。您值得被关心和帮助。</p>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('close')">关闭</el-button>
      <el-button type="primary" @click="openResource">
        查看更多资源
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  alertData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])
const appStore = useAppStore()

const copyNumber = async (number) => {
  try {
    await navigator.clipboard.writeText(number)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

const openResource = () => {
  emit('close')
  appStore.openResourceDrawer()
}
</script>

<style scoped>
.crisis-content {
  padding: 8px 0;
}

.crisis-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.crisis-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.crisis-header h3 {
  font-size: 18px;
  color: var(--color-text-dark);
}

.crisis-intro {
  text-align: center;
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.8;
  margin-bottom: 24px;
}

.crisis-intro strong {
  color: var(--color-crisis);
}

.hotline-section {
  margin-bottom: 24px;
}

.hotline-section h4 {
  font-size: 14px;
  color: var(--color-text-dark);
  margin-bottom: 12px;
}

.hotline-card {
  background: var(--color-crisis-bg);
  border-radius: var(--radius-md);
  padding: 12px;
}

.hotline-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(229, 115, 115, 0.3);
}

.hotline-item:last-child {
  border-bottom: none;
}

.hotline-name {
  flex: 1;
  font-size: 13px;
  color: var(--color-text);
}

.hotline-number {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-crisis);
  font-family: 'Consolas', monospace;
}

.tips-section {
  margin-bottom: 24px;
}

.tips-section h4 {
  font-size: 14px;
  color: var(--color-text-dark);
  margin-bottom: 12px;
}

.tips-section ul {
  padding-left: 20px;
  margin: 0;
}

.tips-section li {
  font-size: 13px;
  color: var(--color-text);
  line-height: 2;
}

.crisis-footer {
  background: var(--color-bg);
  padding: 12px;
  border-radius: var(--radius-sm);
  text-align: center;
}

.crisis-footer p {
  font-size: 13px;
  color: var(--color-text-light);
  margin: 0;
}
</style>
