<template>
  <div class="rankings-container">
    <n-card title="开发者排行榜" class="developer-ranking">
      <!-- 排序选项 -->
      <div class="sort-options">
        <n-radio-group v-model:value="currentSort" @update:value="handleSortChange">
          <n-radio-button value="appCount">
            <template #icon>
              <n-icon><AppsOutline /></n-icon>
            </template>
            应用数
          </n-radio-button>
          <n-radio-button value="guidelineCounts">
            <template #icon>
              <n-icon><DocumentTextOutline /></n-icon>
            </template>
            攻略数
          </n-radio-button>
          <n-radio-button value="totalDownloads">
            <template #icon>
              <n-icon><TrendingUpOutline /></n-icon>
            </template>
            下载数
          </n-radio-button>
          <n-radio-button value="appGuidelineCount">
            <template #icon>
              <n-icon><CodeSlashOutline /></n-icon>
            </template>
            应用和攻略数
          </n-radio-button>
        </n-radio-group>
      </div>

      <n-spin :show="loading">
        <n-empty
          v-if="!loading && (!developers || developers.length === 0)"
          description="暂无数据"
        />
        <div v-else class="ranking-list">
          <n-list>
            <n-list-item
              v-for="(developer, index) in sortedDevelopers"
              :key="developer.creatorId"
            >
              <div
                class="developer-item"
                @click="handleDeveloperClick(developer)"
              >
                <div class="rank" :class="getRankClass(index + 1)">
                  {{ index + 1 }}
                </div>
                <div class="avatar-wrapper">
                  <img
                    v-if="developer.avatar"
                    :src="developer.avatar"
                    :alt="developer.name"
                    class="avatar-img"
                  />
                  <div
                    v-else
                    class="avatar-placeholder"
                    :style="{ background: getAvatarColor(developer.name) }"
                  >
                    {{ getFirstChar(developer.name) }}
                  </div>
                </div>
                <div class="developer-info">
                  <div class="developer-name">{{ developer.name }}</div>
                  <div class="developer-stats">
                    <span>应用数: {{ developer.appCount }}</span>
                    <n-divider vertical />
                    <span>攻略数: {{ developer.guidelineCounts }}</span>
                    <n-divider vertical />
                    <span>应用和攻略数: {{ developer.appCount + developer.guidelineCounts }}</span>
                    <n-divider vertical />
                    <span>总下载:
                      {{ formatDownloads(developer.totalDownloads) }}</span
                    >
                    <n-divider vertical />
                    <span v-if="developer.lastUpdateDate">更新时间:
                      {{ developer.lastUpdateDate }}</span
                    >
                  </div>
                  <div class="app-list">
                    <n-tag
                      v-for="app in developer.apps.slice(0, 3)"
                      :key="app.pkgId"
                      size="small"
                      :bordered="false"
                      @click="handleAppClick(app)"
                    >
                      {{ app.name }}
                    </n-tag>
                    <n-tag
                      v-if="developer.apps.length > 3"
                      size="small"
                      :bordered="false"
                    >
                      等 {{ developer.apps.length }} 个应用
                    </n-tag>
                  </div>
                </div>
              </div>
            </n-list-item>
          </n-list>
        </div>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from "vue";
import { useRouter } from "vue-router";
import {
  NCard,
  NSpin,
  NEmpty,
  NList,
  NListItem,
  NDivider,
  NTag,
  NRadioGroup,
  NRadioButton,
  NIcon,
} from "naive-ui";
import {
  AppsOutline,
  DocumentTextOutline,
  TrendingUpOutline,
  CodeSlashOutline,
} from "@vicons/ionicons5";

interface App {
  pkgId: string;
  name: string;
  updateDate: string;
  downloads?: number;
}

interface Developer {
  creatorId: number;
  name: string;
  avatar?: string;
  apps: App[];
  appCount: number;
  guidelineCounts: number,
  totalDownloads: number;
  lastUpdateDate: string;
}

interface DeveloperRankingResponse {
  success: boolean;
  data: Array<{
    id: number;
    nickName: string;
    avatar?: string;
    apps: Array<{
      pkgId: string;
      name: string;
      updateDate: string;
    }>;
    appCount: number;
    guidelineCounts: number;
    totalDownloads: number;
    lastUpdateDate: string;
  }>;
}

type SortType = 'appCount' | 'guidelineCounts' | 'totalDownloads' | 'appGuidelineCount';

const router = useRouter();
const loading = ref(true);
const developers = ref<Developer[]>([]);
const currentSort = ref<SortType>('appCount');

// 头像背景色列表
const avatarColors = [
  "#1677ff",
  "#13c2c2",
  "#52c41a",
  "#faad14",
  "#eb2f96",
  "#722ed1",
  "#2f54eb",
  "#fa8c16",
  "#fadb14",
  "#a0d911",
];

const getAvatarColor = (name: string | undefined) => {
  if (!name) return avatarColors[0]; // 默认返回第一个颜色
  // 使用名字的 charCode 来选择颜色
  const charSum = name
    .split("")
    .reduce((sum, char) => sum + char.charCodeAt(0), 0);
  return avatarColors[charSum % avatarColors.length];
};

const getFirstChar = (name: string | undefined) => {
  if (!name) return "?"; // 返回问号作为默认字符
  return name.charAt(0);
};

const sortedDevelopers = computed(() => {
  if (!developers.value) return [];
  
  return [...developers.value].sort((a, b) => {
    let aValue: number;
    let bValue: number;
    
    switch (currentSort.value) {
      case 'appCount':
        aValue = a.appCount;
        bValue = b.appCount;
        break;
      case 'guidelineCounts':
        aValue = a.guidelineCounts;
        bValue = b.guidelineCounts;
        break;
      case 'totalDownloads':
        aValue = a.totalDownloads;
        bValue = b.totalDownloads;
        break;
      case 'appGuidelineCount':
        // 应用攻略数 = 应用数 + 攻略数
        aValue = a.appCount + a.guidelineCounts;
        bValue = b.appCount + b.guidelineCounts;
        break;
      default:
        aValue = a.appCount;
        bValue = b.appCount;
    }
    
    // 降序排列
    if (bValue !== aValue) {
      return bValue - aValue;
    }
    
    // 如果主要排序值相同，按应用数作为次要排序
    return b.appCount - a.appCount;
  });
});

const handleSortChange = (value: SortType) => {
  currentSort.value = value;
  console.log('排序方式已更改为:', value);
};

const getRankClass = (rank: number) => {
  if (rank === 1) return "rank-gold";
  if (rank === 2) return "rank-silver";
  if (rank === 3) return "rank-bronze";
  return "";
};

const formatDownloads = (downloads: number) => {
  if (downloads >= 10000) {
    return `${(downloads / 10000).toFixed(1)}万`;
  }
  return downloads.toString();
};

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

const handleAppClick = (app: App) => {
  router.push({
    name: "AppDetail",
    params: { pkgId: app.pkgId },
  });
};

const handleDeveloperClick = (developer: Developer) => {
  if (!developer || typeof developer.creatorId !== "number") {
    console.error("Invalid developer data:", developer);
    return;
  }
  router.push({
    name: "DeveloperApps",
    params: { creatorId: developer.creatorId.toString() },
  });
};

const fetchAllApps = async () => {
  try {
    loading.value = true;
    const response = await fetch("/api/app/developers/ranking");
    const result = (await response.json()) as DeveloperRankingResponse;

    if (result.success && result.data) {
      developers.value = result.data.map((developer) => ({
        creatorId: developer.id,
        name: developer.nickName || "未知开发者",
        avatar: developer.avatar,
        apps: developer.apps || [],
        appCount: developer.appCount || 0,
        guidelineCounts: developer.guidelineCounts || 0,
        totalDownloads: developer.totalDownloads || 0,
        lastUpdateDate: developer.lastUpdateDate || "",
      }));
    } else {
      developers.value = [];
    }
  } catch (error) {
    console.error("Failed to fetch developer ranking:", error);
    developers.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAllApps();
});
</script>

<style scoped>
.rankings-container {
  width: 100%;
}

.developer-ranking {
  width: 100%;
}

.sort-options {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.sort-options .n-radio-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.sort-options .n-radio-button {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 80px;
  justify-content: center;
}

.ranking-list {
  margin-top: 16px;
}

.developer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
}

.rank {
  font-weight: bold;
  color: #666;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.rank-gold {
  background-color: #ffd700;
  color: #fff;
}

.rank-silver {
  background-color: #c0c0c0;
  color: #fff;
}

.rank-bronze {
  background-color: #cd7f32;
  color: #fff;
}

.avatar-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.avatar-wrapper:hover {
  transform: rotate(360deg);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.avatar-img:hover {
  transform: scale(1.1);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
}

.developer-info {
  flex: 1;
}

.developer-name {
  font-size: 16px;
  font-weight: bold;
}

.developer-stats {
  margin-top: 4px;
  color: #666;
}

.app-list {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.app-list n-tag {
  margin-right: 4px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .sort-options {
    margin-bottom: 16px;
  }
  
  .sort-options .n-radio-group {
    gap: 4px;
  }
  
  .sort-options .n-radio-button {
    min-width: 70px;
    font-size: 12px;
  }

  .developer-item {
    padding: 8px;
  }

  .avatar-wrapper {
    width: 36px;
    height: 36px;
  }

  .avatar-placeholder {
    font-size: 16px;
  }

  .developer-name {
    font-size: 14px;
  }
}

@media screen and (max-width: 480px) {
  .sort-options .n-radio-group {
    flex-direction: column;
    align-items: center;
  }
  
  .sort-options .n-radio-button {
    min-width: 120px;
  }

  .developer-item {
    padding: 6px;
  }

  .avatar-wrapper {
    width: 32px;
    height: 32px;
  }

  .avatar-placeholder {
    font-size: 14px;
  }

  .developer-name {
    font-size: 13px;
  }
}
</style>
