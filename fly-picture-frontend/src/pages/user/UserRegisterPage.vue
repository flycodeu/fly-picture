<template>
  <div id="userRegisterPage">
    <h2 class="title">飞云云图库-用户注册</h2>
    <h4 class="desc">飞云云图库-用户注册</h4>
    <a-form
      :model="formState"
      name="basic"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item
        name="userAccount"
        :rules="[
          { required: true, message: '输入账号!' },
          { min: 4, message: '长度不能小于4位' },
          { max: 8, message: '长度不能超过8位' },
        ]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 4, message: '长度不能小于4位' },
          { max: 8, message: '长度不能超过8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 4, message: '长度不能小于4位' },
          { max: 8, message: '长度不能超过8位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请咋次输入密码" />
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
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost, userRegisterUsingPost } from '@/api/userController.ts'
import router from '@/router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

const formState = reactive<API.UserRegisterDto>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const handleSubmit = async (values: any) => {
  const res = await userRegisterUsingPost({
    userAccount: values.userAccount,
    userPassword: values.userPassword,
    checkPassword: values.checkPassword,
  })
  if (res.data.code === 0) {
    await router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error(res.data.message)
  }
  console.log('Success:', values)
}

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  color: #bbb;
  margin-bottom: 16px;
  text-align: center;
}

.tips {
  text-align: right;
  color: #bbbbbb;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
