import { defineStore } from 'pinia'
import axios from 'axios'
import type { App, Category } from '../types'

export const useAppStore = defineStore('app', {
  state: () => ({
    apps: [] as App[],
    categories: [] as Category[],
    loading: false,
    error: null as string | null
  }),

  actions: {
    async fetchCategories() {
      try {
        this.loading = true
        const response = await axios.get('/api/categories')
        if (response.data.success) {
          this.categories = response.data.data.sort((a: Category, b: Category) => a.index - b.index)
        } else {
          throw new Error(response.data.message || '获取分类失败')
        }
      } catch (error) {
        console.error('Failed to fetch categories:', error)
        this.error = '获取分类失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchAppsByCategory(categoryId: number) {
      try {
        this.loading = true
        const response = await axios.get(`https://appstore.api.lazycat.cloud/api/app/list?categories=${categoryId}`)
        if (response.data.success) {
          this.apps = response.data.data
        } else {
          throw new Error(response.data.message || '获取应用列表失败')
        }
      } catch (error) {
        console.error('Failed to fetch apps:', error)
        this.error = '获取应用列表失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchAppInfo(pkgId: string): Promise<App | null> {
      try {
        this.loading = true
        const response = await axios.post('https://appstore.api.lazycat.cloud/api/app/info', {
          pkgIds: [pkgId]
        })
        if (response.data.success && response.data.data.length > 0) {
          return response.data.data[0]
        }
        throw new Error('应用信息不存在')
      } catch (error) {
        console.error('Failed to fetch app info:', error)
        this.error = '获取应用信息失败'
        throw error
      } finally {
        this.loading = false
      }
    }
  }
}) 