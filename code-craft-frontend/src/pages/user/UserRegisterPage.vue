<template>
  <div id="userRegisterPage">
    <h2 class="title">Code Craft Platform - 用户注册</h2>
    <div class="desc">语落成章，网站即现</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '密码不能小于 8 位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
      </a-form-item>
      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  background: var(--bg);
  max-width: 480px;
  padding: 48px 40px;
  margin: 80px auto;
  border: 1px solid var(--line);
  border-radius: 0;
  box-shadow: 0 20px 50px -24px rgba(10, 10, 10, 0.18);
}

.title {
  position: relative;
  text-align: center;
  margin-bottom: 16px;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-style: italic;
  font-weight: 300;
  font-size: 32px;
  color: var(--ink);
}

.title::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 10px;
  background: var(--steel);
  vertical-align: middle;
}

.desc {
  text-align: center;
  margin-bottom: 32px;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-style: italic;
  color: var(--ink-3);
}

.tips {
  margin-bottom: 16px;
  color: var(--ink-3);
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-align: right;
}

.tips a {
  color: var(--steel);
  transition: color 0.2s ease;
}

.tips a:hover {
  color: var(--steel-d);
}

/* 输入框：白底由全局 Antd 覆盖提供，此处仅保留聚焦钢青 */
:deep(.ant-input:focus),
:deep(.ant-input-focused,
.ant-input-affix-wrapper-focused) {
  border-color: var(--steel) !important;
  box-shadow: 0 0 0 2px rgba(71, 85, 105, 0.18) !important;
}

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  #userRegisterPage {
    max-width: 100%;
    margin: 16px;
    padding: 28px 20px;
  }

  .title {
    font-size: 24px;
    margin-bottom: 12px;
  }

  .desc {
    font-size: 14px;
    margin-bottom: 24px;
  }
}
</style>
