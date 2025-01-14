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
            <a-dropdown>
              <ASpace>
                <a-avatar :src="loginUserStore.loginUser.userAvatar"></a-avatar>
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </ASpace>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="userLogOut">
                    <LoginOutlined />
                    退出登录
                  </a-menu-item>
                  <a-menu-item @click="doUserCenter">
                    <UserOutlined />
                    个人设置
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
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
import { computed, h, ref } from 'vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingGet } from '@/api/userController.ts'

//const items = ref<MenuProps['items']>([])
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
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

const items = computed(() => filterMenu(originItems))

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

const userLogOut = async () => {
  const res = await userLogoutUsingGet()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出成功')
    await router.push('/user/login')
  } else {
    message.error('退出失败' + res.data.message)
  }
}

const doUserCenter = async () => {}
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
