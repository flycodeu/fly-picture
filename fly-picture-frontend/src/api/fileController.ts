// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** downLoad GET /api/file/download */
export async function downLoadUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.downLoadUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/download', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** upload POST /api/file/test */
export async function uploadUsingPost(body: {}, file?: File, options?: { [key: string]: any }) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, JSON.stringify(item))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString_>('/api/file/test', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}
