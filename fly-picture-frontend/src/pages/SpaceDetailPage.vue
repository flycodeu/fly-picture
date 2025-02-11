<template>
  <div class="space">
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}(私有空间)</h2>
      <a-space size="middle">
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank"
          >+创建图片
        </a-button>
        <a-tooltip :title="`占用空间${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`">
          <a-progress
            :size="42"
            type="circle"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          ></a-progress>
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"></div>
    <PictureList
      :on-reload="fetchPictureVoList"
      :show-option="true"
      :data-list="dataList"
      :loading="loading"
    ></PictureList>
    <a-pagination
      style="text-align: center"
      v-model:current="searchParams.current"
      v-model:page-size="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    ></a-pagination>
  </div>
</template>
<script lang="ts" setup>
import { deleteSpaceByIdUsingPost, getSpaceVoUsingGet } from '@/api/spaceController.ts'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { downloadImage, formatSize } from '@/utils'
import { EditOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import router from '@/router'
import {
  getPictureVoPageUsingPost,
  getPictureVoPageWithCacheUsingPost,
} from '@/api/pictureController.ts'
import PictureList from '@/components/PictureList.vue'

interface Props {
  id: number | string
}

const props = defineProps<Props>()
const space = ref<API.SpaceVo>({})
const dataList = ref<API.PictureVo[]>()
const loading = ref(true)
const total = ref<number>(0)
const searchParams = reactive<API.PictureQueryDto>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'ascend',
})
const tagList = ref<string[]>([])
const categoryList = ref<string[]>([])
const selectCategory = ref<string>('all')
const selectTags = ref<String[]>([])

const onPageChange = (page: any, pageSize: any) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchPictureVoList()
}

const fetchPictureVoList = async () => {
  loading.value = true
  const params = {
    spaceId: props.id,
    ...searchParams,
  }
  if (selectCategory.value !== 'all') {
    params.category = selectCategory.value
  }

  selectTags.value.forEach((userTag, index) => {
    if (userTag) {
      params.tags?.push(tagList.value[index])
    }
  })

  const res = await getPictureVoPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = Number(res.data.data.total) ?? 0
  } else {
    message.error('获取图片数据失败，' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  getSpace()
  fetchPictureVoList()
})

const getSpace = async () => {
  try {
    const res = await getSpaceVoUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = <API.SpaceVo>data
    } else {
      message.error('获取空间失败')
    }
  } catch (exception: any) {
    message.error('获取空间失败')
  }
}

const loginUserStore = useLoginUserStore()

const canEdit = computed(() => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    return false
  }
  const user = space.value.user
  return loginUser.id === user?.id && loginUser.userRole === 'admin'
})

const doEdit = async () => {
  await router.push('/add_space?id=' + space.value.id)
}
const doDelete = async () => {
  if (!space.value.id) {
    return
  }
  const res = await deleteSpaceByIdUsingPost({
    id: space.value.id,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    await router.push('/')
  } else {
    message.error('删除失败，' + res.data.message)
  }
}

const doDownload = () => {
  downloadImage(space.value.url)
}
</script>

<style scoped></style>
