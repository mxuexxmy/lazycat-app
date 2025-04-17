import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/rankings',
    name: 'Rankings',
    children: [
      {
        path: 'most-popular',
        name: 'MostPopular',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'monthly-new',
        name: 'MonthlyNew',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'latest-release',
        name: 'LatestRelease',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'recent-updates',
        name: 'RecentUpdates',
        component: () => import('@/views/Home.vue')
      }
    ]
  },
  {
    path: '/explore',
    name: 'Explore',
    children: [
      {
        path: 'recent-updates',
        name: 'ExploreRecentUpdates',
        component: () => import('@/views/Home.vue')
      }
    ]
  },
  {
    path: '/statistics',
    name: 'Statistics',
    children: [
      {
        path: 'trend-analysis',
        name: 'TrendAnalysis',
        component: () => import('@/views/Home.vue')
      }
    ]
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue')
  },
  {
    path: '/app/:pkgId',
    name: 'AppDetail',
    component: () => import('@/views/AppDetail.vue')
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: () => import('@/views/Category.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 