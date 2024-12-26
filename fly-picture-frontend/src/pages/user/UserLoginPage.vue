<template>
  <div id="userLoginPage">
    <h2 class="title">飞云云图库-用户登录</h2>
    <h4 class="desc">飞云云图库-用户登录</h4>
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
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import router from '@/router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

const formState = reactive<API.UserLoginDto>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()
const handleSubmit = async (values: any) => {
  try {
    const res = await userLoginUsingPost({
      userAccount: values.userAccount,
      userPassword: values.userPassword,
    })
    if (res.data.code === 0) {
      // 写入stores中
      await loginUserStore.getLoginUser()
      // const loginUserStore = useLoginUserStore()
      // loginUserStore.setLoginUser(res.data.data)
      message.success('登录成功')
      // 防止用户回退到登录界面
      await router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error(res.data.message)
    }
    console.log('Success:', values)
  } catch (err) {
    message.error('登录失败')
  }
}

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}
</script>

<style scoped>
#userLoginPage {
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
