<template>
  <div id="addPicturePage">
    <h2 style="padding-bottom: 10px">{{ route.query?.id ? '修改图片' : '创建图片' }}</h2>
    <a-tabs v-model:activeKey="uploadTab">
      <a-tab-pane key="file" tab="文件上传">
        <PictureUpload :picture="picture" :on-success="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="url上传" force-render>
        <UrlPictureUpload :picture="picture" :on-success="onSuccess" />
      </a-tab-pane>
    </a-tabs>

    <a-form
      :model="pictureForm"
      v-if="picture"
      name="basic"
      layout="vertical"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item label="名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称"></a-input>
      </a-form-item>
      <a-form-item label="描述" name="introduction">
        <a-textarea
          v-model:value="pictureForm.introduction"
          :auto-size="{ minRows: 2, maxRows: 4 }"
          allow-clear
          placeholder="请输入简介"
        ></a-textarea>
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        ></a-auto-complete>
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
        ></a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%"
          >{{ route.query?.id ? '修改' : '创建' }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import PictureUpload from '@/components/PictureUpload.vue'
import { onMounted, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'

const picture = ref<API.PictureVo>()
const onSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
  pictureForm.value.name = picture.value.name
}

const pictureForm = ref<API.PictureEditDto>({})
const uploadTab = ref<'file' | 'url'>('file')
const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('上传图片信息成功')
    // 跳转详情页
    await router.push({
      path: `picture/${pictureId}`,
    })
  } else {
    message.error(res.data.message)
  }
}

const onFinishFailed = () => {
  message.error('保存图片信息失败')
}

//const categoryOptions = ref<String[]>([])
//const tagOptions = ref<String[]>([])
const tagOptions = ref<{ value: string; label: string }[]>([])
const categoryOptions = ref<{ value: string; label: string }[]>([])

const getCategoryAndTags = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  }
}

onMounted(() => {
  getCategoryAndTags()
  getOldPicture()
})

const route = useRoute()

const getOldPicture = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getPictureUsingGet({
      id: String(id),
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = <API.PictureVo>data
      console.log(data)
      pictureForm.value.name = data.name
      pictureForm.value.id = data.id
      pictureForm.value.url = data.url
      pictureForm.value.introduction = data.introduction
      pictureForm.value.tags = data.tags ? JSON.parse(data.tags) : []
      pictureForm.value.category = data.category
    }
  }
}
</script>
<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
