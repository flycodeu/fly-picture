<template>
  <div id="app">
    <a-config-provider :getPopupContainer="getPopupContainer" :locale="locale">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layout/BasicLayout.vue'
import { testOkUsingGet } from '@/api/testController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { ref } from 'vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN';

// testOkUsingGet().then((res) => {
//   console.log(res.data.data)
// })

const loginUserStore = useLoginUserStore()
loginUserStore.getLoginUser()

const locale = ref(zhCN)

const getPopupContainer = (el: any, dialogContext: any) => {
  if (dialogContext) {
    return dialogContext.getDialogWrap()
  } else {
    return document.body
  }
}
</script>

<style scoped></style>
