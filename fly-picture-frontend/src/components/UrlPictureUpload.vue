<template>
  <div class="url-picture-uploader">
    <a-input-group compact style="margin-bottom: 16px">
      <a-input
        v-model:value="fileUrl"
        style="width: calc(100% - 120px)"
        placeholder="请输入图片 URL"
      />
      <a-button type="primary" :loading="loading" @click="handleUpload" style="width: 120px"
        >提交
      </a-button>
    </a-input-group>
    <div class="img-wrapper">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVo
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVo) => void
}

const loading = ref<boolean>(false)
const props = defineProps<Props>()
const fileUrl = ref<string>()

const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadDto = { fileUrl: fileUrl.value }
    params.spaceId = props.spaceId
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('上传图片成功')
      props.onSuccess?.(res.data.data)
    } else {
      message.error('上传图片失败')
    }
  } catch (e) {
    message.error('上传图片失败,' + e.message)
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.url-picture-uploader {
  margin-bottom: 16px;
}

.picture-uploader img {
  max-width: 100%;
  max-height: 480px;
}

.url-picture-uploader .img-wrapper {
  text-align: center;
  margin-top: 16px;
}
</style>
