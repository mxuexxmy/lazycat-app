import request from '@/utils/request'

export interface FiveStarApp {
  pkgId: string
  name: string
  iconPath: string
  description: string
  downloadCount: number
  score: number
  totalReviews: number
  category: string[]
  creator: string
}

const getFiveStarApps = async () => {
  try {
    const response = await fetch('/api/app/statistics/app/five-star"')
    const data = await response.json()
    categories.value = data
  } catch (error) {
    console.error('获取分类统计失败:', error)
  }
}