<template>
  <div id="app">
    <a-config-provider :getPopupContainer="getPopupContainer">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layout/BasicLayout.vue'
import { testOkUsingGet } from '@/api/testController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

testOkUsingGet().then((res) => {
  console.log(res.data.data)
})

const loginUserStore = useLoginUserStore()
loginUserStore.getLoginUser()

const  getPopupContainer = (el:any, dialogContext:any) =>{
  if (dialogContext) {
    return dialogContext.getDialogWrap();
  } else {
    return document.body;
  }
}
</script>

<style scoped></style>
