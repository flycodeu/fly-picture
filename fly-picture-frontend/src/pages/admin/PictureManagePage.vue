<template>
  <div id="pictureManager">
    <div style="padding-bottom: 20px">
      <a-flex justify="space-between">
        <h2>图片管理</h2>
        <a-space>
          <a-button type="primary" href="/add_picture" target="_blank">+创建图片</a-button>
          <a-button type="primary" href="/add_picture/batch" target="_blank" ghost
            >+批量创建图片
          </a-button>
        </a-space>
      </a-flex>
      <div style="margin-bottom: 20px"></div>

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
        <a-form-item label="审核状态" name="reviewStatus">
          <a-select
            v-model:value="searchParams.reviewStatus"
            :options="PIC_REVIEW_STATUS_OPTIONS"
            placeholder="请输入审核状态"
            style="min-width: 180px"
            allow-clear
          />
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
        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewMessages'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
          <div>{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/add_picture?id=${record.id}`">编辑</a-button>
            <a-button type="link" danger @click="deletePic(record.id)">删除</a-button>
            <a-button
              type="link"
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              @click="handlePreview(record.id, PIC_REVIEW_STATUS_ENUM.PASS)"
              >通过
            </a-button>
            <a-button
              type="link"
              danger
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              @click="handlePreview(record.id, PIC_REVIEW_STATUS_ENUM.REJECT)"
              >拒绝
            </a-button>
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
import {
  deletePictureByIdUsingPost,
  doPictureReviewUsingPost,
  getPicturePageUsingPost,
} from '@/api/pictureController.ts'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '@/constant/picture.ts'

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
    width: 80,
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
    width: 120,
  },

  {
    title: '用户id',
    dataIndex: 'userId',
  },

  {
    title: '审核信息',
    dataIndex: 'reviewMessages',
    width: 130,
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
  const res = await getPicturePageUsingPost({ ...searchParams, nullSpaceId: true })
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

const handlePreview = async (id: number, reviewStatus: number) => {
  if (!id) {
    return
  }
  const reviewMessage =
    reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员审核通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: id,
    reviewStatus: reviewStatus,
    reviewMessage: reviewMessage,
  })
  if (res.data.code === 0) {
    await fetchPictureList()
    message.success('审核成功')
  } else {
    message.error(res.data.message)
  }
}
</script>
