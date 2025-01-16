<template>
  <div class="homePage">
    <!-- 搜索框 -->
    <div style="max-width: 480px; margin: 0 auto 16px">
      <a-input-search
        placeholder="从海量图片中搜索"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        :allowClear="true"
        @search="doSearch"
      />
    </div>

    <!--标签分类-->
    <a-tabs v-model:active-key="selectCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部"></a-tab-pane>
      <a-tab-pane v-for="category in categoryList" :tab="category" :key="category"></a-tab-pane>
    </a-tabs>

    <div class="tags-bar">
      <span style="margin-right: 8px">标签: </span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectTags[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!--图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 4, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :pagination="pagination"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                style="height: 180px; object-fit: cover"
                :alt="picture.name"
                :src="picture.url"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import {
  getPictureVoPageUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRouter } from 'vue-router'

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

/**
 * 分页参数
 */
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 12,
    total: total.value,
    onChange: (page: any, pageSize: any) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchPictureVoList()
    },
  }
})

const fetchPictureVoList = async () => {
  loading.value = true
  const params = {
    ...searchParams,
    tags: [] as string[],
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
    total.value = Number(res.data.data.records?.length) ?? 0
  } else {
    message.error('获取图片数据失败，' + res.data.message)
  }
  loading.value = false
}

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchPictureVoList()
}
// 页面加载获取数据
onMounted(() => {
  fetchPictureVoList()
  getCategoryAndTags()
})

// 搜索
const doSearch = async () => {
  // 重置页码
  searchParams.current = 1
  searchParams.pageSize = 12
  // searchParams.category = selectCategory.value === 'all' ? '' : selectCategory.value
  // selectTags.value.forEach((userTag, index) => {
  //   if (userTag) {
  //     searchParams.tags?.push(tagList.value[index])
  //     console.log(searchParams)
  //   }
  // })
  await fetchPictureVoList()
}

const getCategoryAndTags = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
  }
}

const doClickPicture = async (picture: API.PictureVo) => {
  await router.push({
    path: `picture/${picture.id}`,
  })
}
</script>

<style scoped>
.homePage {
  margin-bottom: 40px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

.tags-bar {
  margin-bottom: 16px;
}
</style>
