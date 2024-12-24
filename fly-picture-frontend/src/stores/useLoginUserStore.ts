import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 存储用户登录状态
 */
export const useLoginUserStore = defineStore('counter', () => {
  /**
   * 登录默认值
   */
  const loginUser = ref<any>({
    userName: '未登录',
  })

  /**
   * 设置登录状态
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser.value
  }

  function getLoginUser() {
    setTimeout(() => {
      loginUser.value = { id: 1, userName: '飞云' }
    }, 3000)
    // 请求后端
  }

  return { loginUser, setLoginUser, getLoginUser }
})
