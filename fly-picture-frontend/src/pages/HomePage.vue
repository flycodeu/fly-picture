<template>
  <a-list
    :grid="{ gutter: 16, xs: 1, sm: 2, md: 4, lg: 4, xl: 6, xxl: 5 }"
    :data-source="dataList"
    :pagination="pagination"
  >
    <a-card hoverable style="width: 240px">
      <template #cover>
        <img alt=""  />
      </template>
      <a-card-meta title="Europe Street beat">
        <template #description>www.instagram.com</template>
      </a-card-meta>
    </a-card>
  </a-list>
</template>
<script lang="ts" setup>
import { getPictureVoPageUsingPost } from '@/api/pictureController.ts'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'

const dataList = ref<API.PictureVo[]>()

const total = ref<number>(0)
const searchParams = reactive<API.PictureQueryDto>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})

/**
 * 分页参数
 */
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    onChange: (page: any, pageSize: any) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchPictureVoList()
    },
  }
})

const fetchPictureVoList = async () => {
  const res = await getPictureVoPageUsingPost({ ...searchParams })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取图片数据失败，' + res.data.message)
  }
}

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchPictureVoList()
}
// 页面加载获取数据
onMounted(() => {
  fetchPictureVoList()
})

// 搜索
const doSearch = async () => {
  // 重置页码
  searchParams.current = 1
  searchParams.pageSize = 10
  await fetchPictureVoList()
}
</script>
