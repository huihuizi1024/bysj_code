<template>
  <div class="login-container">
    <div class="login-background">
      <div class="bg-shape shape1"></div>
      <div class="bg-shape shape2"></div>
      <div class="bg-shape shape3"></div>
    </div>

    <div class="login-content">
      <div class="login-left">
        <div class="brand">
          <span class="brand-icon">🌿</span>
          <h1>心灵对话</h1>
          <p>AI 心理陪伴，倾听你的心声</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">💭</span>
            <span>安全倾诉，守护隐私</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🤗</span>
            <span>共情陪伴，温暖回应</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🛡️</span>
            <span>危机预警，专业保障</span>
          </div>
        </div>
      </div>

      <el-card class="login-card">
        <h2>欢迎回来</h2>
        <p class="subtitle">登录以开始你的心灵之旅</p>

        <el-form :model="loginForm" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              style="width: 100%"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="footer-link">
          <span>还没有账号？</span>
          <el-link type="primary" href="/register">去注册</el-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/api/index'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await request.post('/user/login', loginForm.value)
    userStore.setLoginData({
      token: res.data.token,
      userInfo: res.data.userInfo
    })
    ElMessage.success('登录成功！')
    router.push('/home')
  } catch (err) {
    console.error('登录失败', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.shape1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(124, 156, 181, 0.2), rgba(168, 213, 186, 0.1));
  top: -100px;
  right: -100px;
  animation: float 6s ease-in-out infinite;
}

.shape2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(244, 199, 171, 0.2), rgba(232, 180, 184, 0.1));
  bottom: -50px;
  left: -50px;
  animation: float 8s ease-in-out infinite reverse;
}

.shape3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(168, 213, 186, 0.15), rgba(124, 156, 181, 0.1));
  top: 50%;
  left: 10%;
  animation: float 7s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

.login-content {
  display: flex;
  align-items: center;
  gap: 60px;
  padding: 40px;
  z-index: 1;
}

.login-left {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.brand {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.brand-icon {
  font-size: 48px;
}

.brand h1 {
  font-size: 36px;
  font-weight: 600;
  color: var(--color-text-dark);
  letter-spacing: 4px;
}

.brand p {
  font-size: 16px;
  color: var(--color-text-light);
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--color-text);
}

.feature-icon {
  font-size: 20px;
}

.login-card {
  width: 380px;
  padding: 8px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
}

.login-card h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-dark);
  text-align: center;
  margin-bottom: 4px;
}

.subtitle {
  text-align: center;
  color: var(--color-text-light);
  margin-bottom: 24px;
  font-size: 14px;
}

.footer-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  margin-top: 16px;
  font-size: 14px;
  color: var(--color-text-light);
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
}
</style>
