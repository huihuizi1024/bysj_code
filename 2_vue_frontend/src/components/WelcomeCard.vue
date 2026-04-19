<template>
  <div class="welcome-card fade-in">
    <div class="welcome-icon">🧠</div>
    <h1 class="welcome-title">欢迎来到心灵对话</h1>
    <p class="welcome-desc">
      我是你的 AI 心理陪伴者，在这里，你可以倾诉心声，<br>
      我会用温暖和共情来陪伴你。
    </p>

    <div class="welcome-actions">
      <el-button type="primary" size="large" @click="$emit('start-chat')">
        <el-icon><ChatDotRound /></el-icon>
        开始倾诉
      </el-button>
      <el-button size="large" @click="$emit('view-emotion')">
        <el-icon><DataLine /></el-icon>
        查看情绪趋势
      </el-button>
    </div>

    <div class="welcome-tips">
      <div class="tip-item">
        <div class="tip-icon">💭</div>
        <div class="tip-content">
          <h4>倾诉心事</h4>
          <p>分享你的感受和困惑</p>
        </div>
      </div>
      <div class="tip-item">
        <div class="tip-icon">🤗</div>
        <div class="tip-content">
          <h4>获得共情</h4>
          <p>AI 会理解并回应你的情绪</p>
        </div>
      </div>
      <div class="tip-item">
        <div class="tip-icon">🌱</div>
        <div class="tip-content">
          <h4>自我成长</h4>
          <p>一起探索更好的自己</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound, DataLine } from '@element-plus/icons-vue'

defineEmits(['start-chat', 'view-emotion'])
</script>

<style scoped>
.welcome-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px 20px;
  text-align: center;
}

.welcome-icon {
  font-size: 72px;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.welcome-desc {
  font-size: 16px;
  color: var(--color-text-light);
  line-height: 1.8;
  max-width: 400px;
  margin-bottom: 32px;
}

.welcome-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}

.welcome-actions .el-button {
  padding: 16px 32px;
  font-size: 16px;
  border-radius: var(--radius-xl);
}

.welcome-tips {
  display: flex;
  gap: 32px;
  max-width: 700px;
}

.tip-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex: 1;
  padding: 20px;
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: transform 0.3s, box-shadow 0.3s;
}

.tip-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.tip-icon {
  font-size: 32px;
}

.tip-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-dark);
  margin-bottom: 4px;
}

.tip-content p {
  font-size: 12px;
  color: var(--color-text-light);
}

@media (max-width: 768px) {
  .welcome-tips {
    flex-direction: column;
    gap: 16px;
  }

  .welcome-actions {
    flex-direction: column;
    width: 100%;
  }

  .welcome-actions .el-button {
    width: 100%;
  }
}
</style>
