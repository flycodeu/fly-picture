import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

/**
 * 存储用户登录状态
 */
export const useLoginUserStore = defineStore('counter', () => {
  /**
   * 登录默认值
   */
  const loginUser = ref<API.UserLoginVo>({
    userName: '未登录',
  })

  /**
   * 设置登录状态
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  /**
   * 获取用户登录信息
   */
  async function getLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code == 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  return { loginUser, setLoginUser, getLoginUser }
})
