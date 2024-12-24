// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** testOk GET /api/test/health */
export async function testOkUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/test/health', {
    method: 'GET',
    ...(options || {}),
  })
}
