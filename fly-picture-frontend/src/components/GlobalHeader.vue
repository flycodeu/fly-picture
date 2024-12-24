<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">飞云图库</div>
          </div>
        </router-link>
      </a-col>

      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>

      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import { MenuProps } from 'ant-design-vue'
import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.flycode.icu', target: '_blank' }, '飞云编程'),
    title: '飞云编程',
  },
])

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

// 高亮的菜单
const current = ref<string[]>([])
// 需要高亮的路由
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

const loginUserStore = useLoginUserStore()
</script>

<style scoped>
#globalHeader .title-bar {
  align-items: center;
  display: flex;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 20px;
}

.logo {
  width: 2rem; /* 使用相对单位 */
  height: auto; /* 保持宽高比 */
}
</style>
