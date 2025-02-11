<template>
  <div id="addSpacePage">
    <h2 style="padding-bottom: 10px">{{ route.query?.id ? '修改空间' : '创建空间' }}</h2>
    <a-form
      :model="spaceForm"
      name="basic"
      layout="vertical"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称"></a-input>
      </a-form-item>
      <a-form-item label="空间级别" name="reviewStatus">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          :options="SPACE_REVIEW_STATUS_OPTIONS"
          placeholder="请输入空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%"
          >{{ route.query?.id ? '修改' : '创建' }}
        </a-button>
      </a-form-item>
    </a-form>
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持开通普通版，如需升级空间，请联系
        <a href="https://codefather.cn" target="_blank">程序员飞云</a>。
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}： 大小 {{ formatSize(spaceLevel.maxSize) }}， 数量
        {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  addSpaceUsingPost,
  editSpaceUsingPost,
  getSpaceLevelUsingGet,
  getSpaceUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import { SPACE_REVIEW_STATUS_OPTIONS } from '@/constant/space.ts'
import { formatSize } from '../utils'

const space = ref<API.SpaceVo>()
const loading = ref(false)

const spaceForm = reactive<API.SpaceAddDto | API.SpaceUpdateDto>({})
const handleSubmit = async (values: any) => {
  loading.value = true
  const spaceId = space.value?.id
  let res
  if (spaceId) {
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
  } else {
    res = await addSpaceUsingPost({
      ...spaceForm,
    })
  }
  if (res.data.code === 0 && res.data.data) {
    message.success('上传空间信息成功')
    // 跳转详情页
    await router.push({
      path: `space/${res.data.data}`,
    })
  } else {
    message.error(res.data.message)
  }
  loading.value = false
}

const onFinishFailed = () => {
  message.error('保存空间信息失败')
}

onMounted(() => {
  getOldSpace()
})

const spaceLevelList = ref<API.SpaceLevelDto[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await getSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('加载空间级别失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

const route = useRoute()

const getOldSpace = async () => {
  const id = route.query?.id
  if (id) {
    const res = await getSpaceUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = <API.SpaceVo>data
      console.log(data)
      // spaceForm.value.spaceName = data.spaceName
      // spaceForm.value.spaceLevel = data.spaceLevel
    }
  }
}
</script>
<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
