export const SPACE_REVIEW_STATUS_ENUM = {
  Common: 0,
  PROFESSIONAL: 1,
  FLAGSHIP: 2,
}

export const SPACE_REVIEW_STATUS_MAP: { [key: string]: string } = {
  0: '普通版',
  1: '专业版',
  2: '旗舰版',
}

export const SPACE_REVIEW_STATUS_OPTIONS = Object.keys(SPACE_REVIEW_STATUS_MAP).map((key) => {
  return {
    label: SPACE_REVIEW_STATUS_MAP[key],
    value: key,
  }
})
