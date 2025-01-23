<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view/>
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://www.flycode.icu" target="_blank">by飞云</a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">

import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<style scoped>
#basicLayout .footer{
  background: white;
  padding: 8px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 20px;
}

#basicLayout .content{
  padding: 20px;
  background:linear-gradient(to right,#fefefe,#fff);
  margin-bottom: 20px;
  font-size: 20px;

}

#basicLayout .header{
  background: white;
  color: unset;
  padding-bottom: 16px;
  padding-inline: 20px;
  font-size: 20px;
}

</style>
