<template>
  <div class="pictureDetail">
    <a-row :gutter="[16, 16]">
      <!--图片预览-->
      <a-col :sm="24" :md="16">
        <a-card title="图片预览">
          <a-image :src="picture.url" style="max-height: 600px; object-fit: contain"></a-image>
        </a-card>
      </a-col>

      <!--图片详情-->
      <a-col :sm="24" :md="8">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="picture.user?.userAvatar"></a-avatar>
                <div>{{ picture.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="图片名称"
              >{{ picture.name ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="图片介绍"
              >{{ picture.introduction ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="分类">{{ picture.category ?? '默认' }}</a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in picture.tags ?? []" :key="tag" color="green">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="格式">
              {{ picture.picFormat }}
            </a-descriptions-item>
            <a-descriptions-item label="宽度">
              {{ picture.picWidth }}
            </a-descriptions-item>
            <a-descriptions-item label="高度">
              {{ picture.picHeight }}
            </a-descriptions-item>
            <a-descriptions-item label="宽高比">
              {{ picture.picScale }}
            </a-descriptions-item>
            <a-descriptions-item label="大小">
              {{ formatSize(picture.picSize) }}
            </a-descriptions-item>
          </a-descriptions>
          <a-space wrap>
            <a-button type="primary" @click="doDownload">
              下载
              <template #icon>
                <DownloadOutlined />
              </template>
            </a-button>
            <a-button v-if="canEdit" type="default" @click="doEdit">
              编辑
              <template #icon>
                <EditOutlined />
              </template>
            </a-button>
            <a-button v-if="canEdit" danger @click="doDelete">
              删除
              <template #icon>
                <DeleteOutlined />
              </template>
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import {
  deletePictureByIdUsingPost,
  getPictureVoUsingGet,
} from '@/api/pictureController.ts'
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { downloadImage, formatSize } from '@/utils'
import { EditOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import router from '@/router'

interface Props {
  id: number | string
}

const props = defineProps<Props>()
const picture = ref<API.PictureVo>({})

onMounted(() => {
  getPictureDetail()
})

const getPictureDetail = async () => {
  try {
    const res = await getPictureVoUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = <API.PictureVo>data
    } else {
      message.error('获取图片失败')
    }
  } catch (exception: any) {
    message.error('获取图片失败')
  }
}

const loginUserStore = useLoginUserStore()

const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return false
  }
  const user = picture.value.user
  return loginUser.id === user?.id && loginUser.userRole === 'admin'
})

const doEdit = async () => {
  await router.push('/add_picture?id=' + picture.value.id)
}
const doDelete = async () => {
  if (!picture.value.id) {
    return
  }
  const res = await deletePictureByIdUsingPost({
    id: picture.value.id,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    await router.push('/')
  } else {
    message.error('删除失败，' + res.data.message)
  }
}

const doDownload = () => {
  downloadImage(picture.value.url);
}

</script>

<style scoped></style>
