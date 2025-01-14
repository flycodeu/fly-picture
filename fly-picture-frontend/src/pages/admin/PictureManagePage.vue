<template>
  <div id="userManager">
    <div style="padding-bottom: 20px">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="关键词">
          <a-input
            allow-clear
            v-model:value="searchParams.searchText"
            placeholder="输入名称和简介搜索"
          />
        </a-form-item>
        <a-form-item label="类型">
          <a-input allow-clear v-model:value="searchParams.category" placeholder="输入类型" />
        </a-form-item>

        <a-form-item label="标签">
          <a-select
            v-model:value="searchParams.tags"
            mode="tags"
            placeholder="请输入标签"
            style="min-width: 180px"
            allow-clear
          ></a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </div>

    <a-button type="primary" :href="`/add_picture`" style="padding-bottom: 20px">新增</a-button>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-avatar :src="record.url" :with="120" />
        </template>
        <template v-else-if="column.dataIndex === 'tags'">
          <a-space :wrap="true">
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" color="green"> {{ tag }}</a-tag>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex === 'picInfo'">
          <div>格式: {{ record.picFormat }}</div>
          <div>宽度: {{ record.picWidth }}</div>
          <div>高度: {{ record.picHeight }}</div>
          <div>宽高比: {{ record.picScale }}</div>
          <div>大小: {{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" :href="`/add_picture?id=${record.id}`">编辑</a-button>
            <a-button type="primary" danger @click="deletePic(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { deletePictureByIdUsingPost, getPicturePageUsingPost } from '@/api/pictureController.ts'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '图片地址',
    dataIndex: 'url',
    width: 120,
  },
  {
    title: '名称',
    dataIndex: 'name',
    align: 'center',
  },
  {
    title: '介绍',
    dataIndex: 'introduction',
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },

  {
    title: '用户id',
    dataIndex: 'userId',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

const dataList = ref<API.Picture[]>([])
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
    showSizeChanger: true,
    showTotal: (total: number) => `${total}条`,
  }
})

const fetchPictureList = async () => {
  const res = await getPicturePageUsingPost({ ...searchParams })
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
  fetchPictureList()
}
// 页面加载获取数据
onMounted(() => {
  fetchPictureList()
})

// 搜索
const doSearch = async () => {
  // 重置页码
  searchParams.current = 1
  searchParams.pageSize = 10
  await fetchPictureList()
}

// 删除
const deletePic = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deletePictureByIdUsingPost({
    id: id,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    await fetchPictureList()
  } else {
    message.error('删除失败')
  }
}
</script>
