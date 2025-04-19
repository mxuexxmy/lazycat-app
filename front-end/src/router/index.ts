import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import DeveloperCommunity from '@/views/developers/DeveloperCommunity.vue'
import GitHubAchievements from '../views/GitHubAchievements.vue'

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
      }
    ]
  },
  {
    path: '/developers',
    name: 'Developers',
    component: () => import('@/views/Developers.vue'),
    children: [
      {
        path: 'ranking',
        name: 'DeveloperRanking',
        component: () => import('@/views/developers/DeveloperRanking.vue')
      },
      {
        path: 'repositories',
        name: 'AppRepositories',
        component: () => import('@/views/developers/AppRepositories.vue')
      },
      {
        path: 'apps/:creatorId',
        name: 'DeveloperApps',
        component: () => import('@/views/developers/DeveloperApps.vue')
      },
      {
        path: 'community',
        name: 'DeveloperCommunity',
        component: DeveloperCommunity
      },
      {
        path: 'achievements',
        name: 'GitHubAchievements',
        component: GitHubAchievements,
        meta: {
          title: 'GitHub 成就'
        }
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