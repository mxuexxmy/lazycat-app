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

export const getFiveStarApps = (limit = 10) => {
  return request.get<FiveStarApp[]>("/apps/statistics/apps/five-star", {
    params: { limit }
  })
} 