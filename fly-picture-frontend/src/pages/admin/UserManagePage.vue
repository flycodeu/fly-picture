<template>
  <div id="userManager">
    <div style="padding-bottom: 20px">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="账号">
          <a-input allow-clear v-model:value="searchParams.userAccount" placeholder="输入账号" />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input allow-clear v-model:value="searchParams.userName" placeholder="输入用户名" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </div>

    <a-button @click="doAdd" style="padding-bottom: 20px">新增</a-button>
    <a-modal v-model:open="visiable" :confirm-loading="confirmLoading" @ok="handleOk">
      <a-form
        :model="addUserModel"
        :label-col="labelCol"
        style="padding-top: 40px; padding-right: 30px"
      >
        <a-form-item
          name="userAccount"
          label="账号"
          :rules="[{ required: true, message: '输入账号' }]"
        >
          <a-input v-model:value="addUserModel.userAccount" />
        </a-form-item>
        <a-form-item name="userName" label="昵称">
          <a-input v-model:value="addUserModel.userName" />
        </a-form-item>
        <a-form-item name="userRole" label="角色">
          <a-radio-group v-model:value="addUserModel.userRole">
            <a-radio value="user">用户</a-radio>
            <a-radio value="admin">管理员</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-avatar :src="record.userAvatar" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="default" @click="doEditUser(record.id)">编辑</a-button>
          <a-button type="primary" danger @click="deleteUser(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="editVisiable" :confirm-loading="confirmLoading" @ok="editHandleOk">
      <a-form
        :model="editUserModel"
        :label-col="labelCol"
        style="padding-top: 40px; padding-right: 30px"
      >
        <a-form-item name="userName" label="昵称">
          <a-input v-model:value="editUserModel.userName" />
        </a-form-item>
        <a-form-item name="userProfile" label="简介">
          <a-input v-model:value="editUserModel.userProfile" />
        </a-form-item>
        <a-form-item name="userRole" label="角色">
          <a-radio-group v-model:value="editUserModel.userRole">
            <a-radio value="user">用户</a-radio>
            <a-radio value="admin">管理员</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  addUserUsingPost,
  deleteUserUsingPost,
  getUserVoByIdUsingGet,
  listUserVoUsingPost,
  updateUserUsingPost,
} from '@/api/userController.ts'
import { type FormInstance, message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

const dataList = ref<API.UserVo[]>([])
const total = ref<number>(0)
const searchParams = reactive<API.UserQueryDto>({
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
    showTotal: (total) => `${total}条`,
  }
})

const fetchUserList = async () => {
  const res = await listUserVoUsingPost({ ...searchParams })
  if (res.data.code === 0 && res.data.data) {
    console.log(res.data.data)
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取用户数据失败，' + res.data.message)
  }
}

const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchUserList()
}
// 页面加载获取数据
onMounted(() => {
  fetchUserList()
})

// 搜索
const doSearch = async () => {
  // 重置页码
  searchParams.current = 1
  searchParams.pageSize = 10
  await fetchUserList()
}

// 删除
const deleteUser = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({
    id: id,
  })
  if (res.data.code === 0) {
    message.success('删除成功')
    await fetchUserList()
  } else {
    message.error('删除失败')
  }
}

/**
 * 用户新增
 */
const labelCol = { style: { width: '100px' } }

const addUserModel = reactive<API.UserAddDto>({
  userRole: 'user',
  userAccount: '',
  userName: '',
})
const visiable = ref(false)
const confirmLoading = ref<boolean>(false)
const handleOk = async () => {
  console.log(addUserModel)
  confirmLoading.value = true
  if (addUserModel.userAccount === '') {
    message.error('请输入账号')
    confirmLoading.value = false
    return
  }

  const res = await addUserUsingPost({
    ...addUserModel,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('新增成功')
    await fetchUserList()
    confirmLoading.value = false
    visiable.value = false
  } else {
    message.error(res.data.message)
    confirmLoading.value = false
    visiable.value = false
  }
}

const doAdd = () => {
  visiable.value = true
}

/**
 * 编辑用户
 */
const editUserModel = reactive<API.UserUpdateDto>({
  userRole: 'user',
  userName: '',
  userProfile: '',
})

const editVisiable = ref(false)
const editHandleOk = async () => {
  console.log(editUserModel)
  confirmLoading.value = true
  const res = await updateUserUsingPost({
    ...editUserModel,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('修改成功')
    await fetchUserList()
    confirmLoading.value = false
    editVisiable.value = false
  } else {
    message.error(res.data.message)
    confirmLoading.value = false
    editVisiable.value = false
  }
}

const doEditUser = async (id: number) => {
  console.log(id)
  if (!id) {
    message.error('对应用户不存在或已被删除')
    return
  }
  const res = await getUserVoByIdUsingGet({id})
  if (res.data.code === 0 && res.data.data) {
    editUserModel.userName = res.data.data.userName
    editUserModel.userRole = res.data.data.userRole
    editUserModel.userProfile = res.data.data.userProfile
    editUserModel.id = res.data.data.id
  }
  editVisiable.value = true
}
</script>
