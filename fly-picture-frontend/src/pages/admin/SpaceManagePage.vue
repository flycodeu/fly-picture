<template>
  <div id="spaceManager">
    <div style="padding-bottom: 20px">
      <a-flex justify="space-between">
        <h2>空间管理</h2>
        <a-space>
          <a-button type="primary" href="/add_space" target="_blank">+创建空间</a-button>
        </a-space>
      </a-flex>
      <div style="margin-bottom: 20px"></div>

      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="空间id">
          <a-input allow-clear v-model:value="searchParams.id" placeholder="输入空间id" />
        </a-form-item>
        <a-form-item>
          <a-input allow-clear v-model:value="searchParams.userId" placeholder="输入用户id" />
        </a-form-item>
        <a-form-item label="空间名称">
          <a-input allow-clear v-model:value="searchParams.spaceName" placeholder="输入空间名称" />
        </a-form-item>

        <a-form-item label="空间级别" name="reviewStatus">
          <a-select
            v-model:value="searchParams.spaceLevel"
            :options="SPACE_REVIEW_STATUS_OPTIONS"
            placeholder="请输入空间级别"
            style="min-width: 180px"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </div>

    <a-button type="primary" :href="`/add_space`" style="padding-bottom: 20px">新增</a-button>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'spaceLevel'">
          <div>{{ SPACE_REVIEW_STATUS_MAP[record.spaceLevel]}}</div>
        </template>
        <template v-else-if="column.dataIndex === 'spaceUserInfo'">
          <div>大小: {{ formatSize(record.totalSize)}} / {{ formatSize(record.maxSize)}}</div>
          <div>数量: {{record.totalCount}} / {{ record.maxCount}}</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/add_space?id=${record.id}`">编辑</a-button>
            <a-button type="link" danger @click="deleteSpace(record.id)">删除</a-button>
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
import { SPACE_REVIEW_STATUS_MAP, SPACE_REVIEW_STATUS_OPTIONS } from '@/constant/space.ts'
import { deleteSpaceByIdUsingPost, getSpacePageUsingPost } from '@/api/spaceController.ts'
import { formatSize } from '../../utils'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUserInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
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

const dataList = ref<API.Space[]>([])
const total = ref<number>(0)
const searchParams = reactive<API.SpaceQueryDto>({
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

const fetchSpaceList = async () => {
  const res = await getSpacePageUsingPost({ ...searchParams })
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
  fetchSpaceList()
}
// 页面加载获取数据
onMounted(() => {
  fetchSpaceList()
})

// 搜索
const doSearch = async () => {
  // 重置页码
  searchParams.current = 1
  searchParams.pageSize = 10
  await fetchSpaceList()
}

// 删除
const deleteSpace = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceByIdUsingPost({
    id: id,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    await fetchSpaceList()
  } else {
    message.error('删除失败')
  }
}
</script>
