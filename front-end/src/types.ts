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
  creatorId: string | number
  author: string
  updateId: number
  price: number
  downloads: number
  updateContent: string
  updateDate: string
  supportPC: boolean
  supportMobile: boolean
  pcScreenshotPaths: string[]
  mobileScreenshotPaths: string[]
  unsupportedPlatforms: string[]
  changelog: string
  source: string
  osDependence: string
}

export interface Category {
  id: number
  name: string
  icon: string
  index: number
} 