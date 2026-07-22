<template>
  <div id="userLoginPage">
    <h2 class="title">Code Craft Platform - 用户登录</h2>
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
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogin } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  background: #1a1a2e;
  max-width: 720px;
  padding: 24px;
  margin: 24px auto;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.title {
  text-align: center;
  margin-bottom: 16px;
  color: #e0e0e0;
}

.desc {
  text-align: center;
  color: #6a6a8a;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #6a6a8a;
  font-size: 13px;
  text-align: right;
}

:deep(.tips a) {
  color: #D4AF37;
}

:deep(.tips a:hover) {
  color: #E5C158;
}

/* 输入框深灰色背景、浅灰色文字 */
:deep(.ant-input) {
  background: #2a2a4a !important;
  color: #c0c0c0 !important;
  border-color: #3a3a5a !important;
}

:deep(.ant-input::placeholder) {
  color: #888 !important;
}

/* 密码输入框深灰色背景 */
:deep(.ant-input-password) {
  background: #2a2a4a !important;
  border-color: #3a3a5a !important;
}

:deep(.ant-input-password input) {
  background: #2a2a4a !important;
  color: #c0c0c0 !important;
}

:deep(.ant-input-password-icon) {
  color: #888 !important;
}

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  #userLoginPage {
    max-width: 100%;
    margin: 12px;
    padding: 20px 16px;
    border-radius: 12px;
  }

  .title {
    font-size: 20px;
    margin-bottom: 12px;
  }

  .desc {
    font-size: 14px;
    margin-bottom: 20px;
  }
}
</style>
