import { defineStore } from 'pinia'

export interface UserState {
  id: string | null
  username: string | null
  avatar: string | null
  isLoggedIn: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: null,
    username: null,
    avatar: null,
    isLoggedIn: false
  }),
  
  actions: {
    setUser(user: Partial<UserState>) {
      if (user.id) this.id = user.id
      if (user.username) this.username = user.username
      if (user.avatar) this.avatar = user.avatar
      this.isLoggedIn = true
    },
    
    clearUser() {
      this.id = null
      this.username = null
      this.avatar = null
      this.isLoggedIn = false
    }
  }
}) 