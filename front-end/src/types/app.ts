export interface App {
  name: string
  pkgId: string
  description: string
  brief: string
  category: string[]
  iconPath: string
  keywords: string
  version: string
  creator: string
  creatorId: string
  author: string
  updateId: number
  downloads: number
  categoryId: number
  osDependence: string
}

export interface Category {
  id: number
  name: string
  icon: string
  index: number
} 