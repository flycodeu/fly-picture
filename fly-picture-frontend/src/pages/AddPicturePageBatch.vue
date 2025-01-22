<template>
  <div id="addPictureBatchPage">
    <h2 style="padding-bottom: 10px">批量创建</h2>
    <a-form
      :model="formData"
      name="basic"
      layout="vertical"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item label="关键词" name="searchText">
        <a-input v-model:value="formData.searchText" placeholder="请输入名称"></a-input>
      </a-form-item>
      <a-form-item label="数量" name="count">
        <a-input-number
          v-model:value="formData.count"
          style="min-width: 180px"
          :min="1"
          :max="30"
          allow-clear
        ></a-input-number>
      </a-form-item>
      <a-form-item label="名称前缀" name="namePrefix">
        <a-input
          v-model:value="formData.namePrefix"
          placeholder="请输入名称前缀，会自动补充序号"
        ></a-input>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading"
          >批量提交
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import PictureUpload from '@/components/PictureUpload.vue'
import { onMounted, ref } from 'vue'
import {
  getPictureUsingGet,
  listPictureTagCategoryUsingGet,
  uploadPictureByBatchUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'

const formData = ref<API.PictureUploadBatchDto>({
  count: 10,
})

const loading = ref(false)
const handleSubmit = async (values: any) => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...formData.value,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success(`创建成功,共${res.data.data}条`)
    // 跳转详情页
    await router.push({
      path: `/`,
    })
  } else {
    message.error(res.data.message)
  }
  loading.value = false
}

const onFinishFailed = () => {
  message.error('保存图片信息失败')
}
</script>
<style scoped>
#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
