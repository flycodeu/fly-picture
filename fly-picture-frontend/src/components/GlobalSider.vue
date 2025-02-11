<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      class="sider"
      width="200"
      breakpoint="lg"
      collapsed-width="0"
    >
      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, PictureOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingGet } from '@/api/userController.ts'

//const items = ref<MenuProps['items']>([])
const menuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
    title: '公共图库',
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(HomeOutlined),
    title: '我的空间',
  },
  {
    key: '/add_space',
    label: '创建空间',
    title: '创建空间',
  },
]
/**
 * 根据权限过滤用户菜单
 * @param menus
 */
const filterMenu = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menus) => {
    if (menus?.key?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole != 'admin') {
        return false
      }
    }
    return true
  })
}

const items = computed(() => filterMenu(menuItems))

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
#globalSider .ant-layout-sider {
  background: none;
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
