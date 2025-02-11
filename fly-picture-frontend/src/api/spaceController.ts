// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** addSpace POST /api/space/add */
export async function addSpaceUsingPost(body: API.SpaceAddDto, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/api/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteSpaceById POST /api/space/delete */
export async function deleteSpaceByIdUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editSpace POST /api/space/edit */
export async function editSpaceUsingPost(body: API.SpaceEditDto, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/space/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpace GET /api/space/get */
export async function getSpaceUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpace_>('/api/space/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceVo GET /api/space/get/vo */
export async function getSpaceVoUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVoUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpaceVo_>('/api/space/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpacePage POST /api/space/list/page */
export async function getSpacePageUsingPost(
  body: API.SpaceQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSpace_>('/api/space/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceVoPage POST /api/space/list/page/vo */
export async function getSpaceVoPageUsingPost(
  body: API.SpaceQueryDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSpaceVo_>('/api/space/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceLevel GET /api/space/list/spaceLevel */
export async function getSpaceLevelUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceLevelDto_>('/api/space/list/spaceLevel', {
    method: 'GET',
    ...(options || {}),
  })
}

/** updateSpace POST /api/space/update */
export async function updateSpaceUsingPost(
  body: API.SpaceUpdateDto,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
