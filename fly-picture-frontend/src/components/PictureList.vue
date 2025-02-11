<template>
  <div class="picture-list">
    <!--图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 4, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                style="height: 180px; object-fit: cover"
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
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
            <template #actions v-if="showOption">
              <a-space>
                <EditOutlined key="edit" @click="(e) => doEdit(e, picture)"> 编辑</EditOutlined>
              </a-space>
              <a-space>
                <DeleteOutlined key="del" @click="(e) => doDel(e, picture)"> 删除</DeleteOutlined>
              </a-space>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import {
  deletePictureByIdUsingPost,
  getPictureVoPageUsingPost,
  getPictureVoPageWithCacheUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import router from '@/router'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'

interface Props {
  dataList?: API.PictureVo[]
  loading?: boolean
  showOption?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOption: false,
})

const doClickPicture = async (picture: API.PictureVo) => {
  await router.push({
    path: `/picture/${picture.id}`,
  })
}

const doEdit = async (e, picture) => {
  // 阻止冒泡,防止与card的点击事件冲突
  e.stopPropagation()
  await router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

const doDel = async (e, picture) => {
  // 阻止冒泡
  e.stopPropagation()
  if (!picture.id) {
    return
  }
  const res = await deletePictureByIdUsingPost({
    id: picture.id,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败，' + res.data.message)
  }
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
