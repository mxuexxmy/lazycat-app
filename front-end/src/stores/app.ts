import { defineStore } from 'pinia'
import axios from 'axios'
import type { Category } from '../types'

export const useAppStore = defineStore('app', {
  state: () => ({
    categories: [] as Category[],
    loading: false,
    error: null as string | null
  }),

  actions: {
    async fetchCategories() {
      try {
        this.loading = true
        const response = await axios.get('/api/categories')
        if (Array.isArray(response.data)) {
          this.categories = response.data.sort((a: Category, b: Category) => a.index - b.index)
        } else {
          throw new Error('获取分类失败')
        }
      } catch (error) {
        console.error('Failed to fetch categories:', error)
        this.error = '获取分类失败'
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})