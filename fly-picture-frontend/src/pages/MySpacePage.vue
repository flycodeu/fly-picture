<template>
  <div id="MySpacePage">
    <p>正在加载中，请稍后</p>
  </div>
</template>
<script lang="ts" setup>
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import { getSpacePageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const checkUserSpace = async () => {
  const loginUser = loginUserStore.loginUser
  // 用户未登录，跳转登录界面
  if (!loginUser) {
    await router.replace('/user/login')
    return
  }
  // 用户登录，会获取已创建的空间
  const res = await getSpacePageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
  })
  // 有，进入第一个空间
  if (res.data.code === 0) {
    if (res.data.data?.records?.length > 0) {
      const space = res.data.data?.records[0]
      await router.push(`/space/${space?.id}`)
    } else {
      // 没有，进入创建空间界面
      await router.push('/add_space')
      message.warn('请先创建空间')
    }
  } else {
    message.error(res.data.message)
  }
}

onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped>
#MySpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
