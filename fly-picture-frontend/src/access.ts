import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'

// 第一次进入界面就记录状态
let firstFetchLoginUser = true
/**
 * 每次切换的时候都会执行判断用户权限
 */
router.beforeEach(async (to, from, next) => {
  // 获取用户登录信息
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 确保页面刷新，能够等待后端返回权限
  if (firstFetchLoginUser) {
    await loginUserStore.getLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const toUrl = to.fullPath
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole != 'admin') {
      message.error('无权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }

  // 校验通过，放行
  next()
})
