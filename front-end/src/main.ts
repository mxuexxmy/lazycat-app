import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import naive from 'naive-ui'
import * as ionicons5 from '@vicons/ionicons5'
import { zhCN, dateZhCN } from 'naive-ui'

const app = createApp(App)

// Register icons
app.component('Search', ionicons5.Search)

// Use plugins
app.use(createPinia())
app.use(router)
app.use(naive)

// Configure Naive UI
const naiveConfig = {
  locale: zhCN,
  dateLocale: dateZhCN
}

app.provide('naiveConfig', naiveConfig)

app.mount('#app') 