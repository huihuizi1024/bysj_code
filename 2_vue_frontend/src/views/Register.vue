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
          <p>开启你的心灵探索之旅</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">🔒</span>
            <span>匿名使用，保护隐私</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💝</span>
            <span>温暖共情，陪伴成长</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🆓</span>
            <span>随时随地，免费使用</span>
          </div>
        </div>
      </div>

      <el-card class="login-card">
        <h2>创建账号</h2>
        <p class="subtitle">加入我们，开始心灵对话</p>

        <el-form :model="registerForm" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
              size="large"
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              style="width: 100%"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="footer-link">
          <span>已有账号？</span>
          <el-link type="primary" href="/login">去登录</el-link>
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

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, message: '用户名至少4个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await request.post('/user/register', {
      username: registerForm.value.username,
      password: registerForm.value.password
    })
    ElMessage.success('注册成功！请登录。')
    router.push('/login')
  } catch (err) {
    console.error('注册失败', err)
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
