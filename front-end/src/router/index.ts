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
    component: () => import('@/views/Rankings.vue'),
    children: [
      {
        path: 'most-popular',
        name: 'MostPopular',
        component: () => import('@/views/rankings/MostPopular.vue')
      },
      {
        path: 'monthly-new',
        name: 'MonthlyNew',
        component: () => import('@/views/rankings/MonthlyNew.vue')
      },
      {
        path: 'latest-release',
        name: 'LatestRelease',
        component: () => import('@/views/rankings/LatestRelease.vue')
      },
      {
        path: 'recent-updates',
        name: 'RecentUpdates',
        component: () => import('@/views/rankings/RecentUpdates.vue')
      },
      {
        path: 'developer-ranking',
        name: 'DeveloperRanking',
        component: () => import('@/views/rankings/DeveloperRanking.vue')
      }
    ]
  },
  {
    path: '/explore',
    name: 'Explore',
    component: () => import('@/views/Explore.vue'),
    children: [
      {
        path: 'games',
        name: 'Games',
        component: () => import('@/views/Category.vue')
      },
      {
        path: 'dev-tools',
        name: 'DevTools',
        component: () => import('@/views/Category.vue')
      },
      {
        path: 'education',
        name: 'Education',
        component: () => import('@/views/Category.vue')
      }
    ]
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/Statistics.vue'),
    children: [
      {
        path: 'trend-analysis',
        name: 'TrendAnalysis',
        component: () => import('@/views/statistics/TrendAnalysis.vue')
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