<template>
  <div class="most-popular" ref="mostPopularRef">
    <div class="header" ref="headerRef">
      <h1>最受欢迎</h1>
      <div class="filter-section">
        <n-select
          v-model:value="selectedLimit"
          :options="limitOptions"
          size="small"
          style="width: 120px;"
          @update:value="handleLimitChange"
        />
      </div>
      <p class="header-desc">按下载量排序的热门应用</p>
    </div>
    <n-card class="content-card">
      <n-spin :show="loading">
        <n-empty
          v-if="!loading && (!apps || apps.length === 0)"
          description="暂无数据"
        />
        <n-list v-else>
          <n-list-item
            v-for="(app, index) in apps"
            :key="app.pkgId"
            class="app-item"
            @click="handleAppClick(app)"
          >
            <n-thing>
              <template #header>
                <n-space align="center">
                  <div class="rank-badge">{{ index + 1 }}</div>
                  <n-avatar
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    :size="48"
                    object-fit="cover"
                    class="app-icon"
                  />
                  <span class="app-name">{{ app.name }}</span>
                  <n-tag type="success" size="small" round>
                    <template #icon>
                      <n-icon><download-outline /></n-icon>
                    </template>
                    {{ formatDownloads(app.downloads) }}
                  </n-tag>
                </n-space>
              </template>
              <template #description>
                <div class="app-desc">{{ app.brief || app.description }}</div>
              </template>
              <template #footer>
                <n-space wrap :size="4">
                  <n-tag
                    v-for="cat in app.category"
                    :key="cat"
                    size="small"
                    round
                    :bordered="false"
                    type="warning"
                  >
                    {{ cat }}
                  </n-tag>
                  <n-tag size="small" round :bordered="false" type="info">
                    v{{ app.version }}
                  </n-tag>
                  <n-tag
                    v-if="app.supportPC && app.supportMobile"
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    全平台
                  </n-tag>
                  <n-tag
                    v-else-if="app.supportPC"
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    PC端
                  </n-tag>
                  <n-tag
                    v-else-if="app.supportMobile"
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    移动端
                  </n-tag>
                </n-space>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import {
  NCard,
  NList,
  NListItem,
  NThing,
  NTag,
  NEmpty,
  NSpin,
  NSpace,
  NAvatar,
  NIcon,
  NSelect,
} from "naive-ui";
import { DownloadOutline } from "@vicons/ionicons5";

const __name = "MostPopular";

interface AppInfo {
  name: string;
  pkgId: string;
  description: string;
  brief: string;
  category: string[];
  iconPath: string;
  version: string;
  downloads: number;
  supportPC: boolean;
  supportMobile: boolean;
}

const loading = ref(false);
const apps = ref<AppInfo[]>([]);
const defaultIcon = "/path/to/default-icon.png";
const router = useRouter();

const selectedLimit = ref(100);
const limitOptions = [
  { label: '10', value: 10 },
  { label: '20', value: 20 },
  { label: '30', value: 30 },
  { label: '50', value: 50 },
  { label: '100', value: 100 },
  { label: '200', value: 200 },
  { label: '300', value: 300 },
  { label: '400', value: 400 },
  { label: '500', value: 500 },
  { label: '600', value: 600 },
  { label: '800', value: 800 },
  { label: '999', value: 999 },
];

const headerRef = ref<HTMLElement | null>(null);
const mostPopularRef = ref<HTMLElement | null>(null);

function updatePaddingTop() {
  if (headerRef.value && mostPopularRef.value) {
    mostPopularRef.value.style.paddingTop = headerRef.value.offsetHeight + 24 + "px";
  }
}

onMounted(() => {
  nextTick(() => {
    updatePaddingTop();
    window.addEventListener("resize", updatePaddingTop);
  });
  fetchMostPopularApps();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", updatePaddingTop);
});

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon;
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`;
};

const formatDownloads = (downloads: number) => {
  if (downloads >= 10000) {
    return `${(downloads / 10000).toFixed(1)}万`;
  }
  return downloads.toString();
};

const fetchMostPopularApps = async () => {
  loading.value = true;
  try {
    const response = await fetch(`/api/apps/popular?limit=${selectedLimit.value}`);
    const result = await response.json();
    if (Array.isArray(result)) {
      apps.value = result.map((app) => ({
        pkgId: app.pkgId,
        name: app.name,
        description: app.description,
        brief: app.brief,
        category: app.category || [],
        iconPath: app.iconPath,
        version: app.version,
        downloads: app.downloadCount || 0,
        supportPC: app.supportPC,
        supportMobile: app.supportMobile,
      }));
    }
  } catch (error) {
    console.error("Failed to fetch most popular apps:", error);
  } finally {
    loading.value = false;
  }
};

const handleAppClick = (app: AppInfo) => {
  router.push({
    name: "AppDetail",
    params: { pkgId: app.pkgId },
  });
};

const handleLimitChange = () => {
  fetchMostPopularApps();
};
</script>

<style scoped>
.most-popular {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  padding-top: 120px; /* 为固定头部留出空间 */
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  text-align: center;
  background: #fff;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header h1 {
  font-size: 28px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.header-desc {
  font-size: 14px;
  color: #666;
  margin: 8px 0 0;
}

.filter-section {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.app-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  padding: 12px 16px; /* 减小内边距 */
}

.app-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.app-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.app-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.app-icon {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #fff;
  /* padding: 4px; */
}

.rank-badge {
  min-width: 24px;
  height: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 6px;
  white-space: nowrap;
}

.app-item:nth-child(1) .rank-badge {
  background: linear-gradient(120deg, #f6d365 0%, #fda085 100%);
}

.app-item:nth-child(2) .rank-badge {
  background: linear-gradient(120deg, #89f7fe 0%, #66a6ff 100%);
}

.app-item:nth-child(3) .rank-badge {
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .most-popular {
    padding: 16px;
    padding-top: 140px; /* 增加头部空间，为选择器留出位置 */
  }

  .header {
    padding: 16px;
  }

  .header h1 {
    font-size: 20px;
  }

  .header-desc {
    margin-top: 16px;
    margin-bottom: 0;
    text-align: center;
    order: 3;
  }

  .filter-section {
    order: 2;
  }

  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .app-name {
    font-size: 15px;
  }

  .app-desc {
    font-size: 13px;
  }
}

@media screen and (max-width: 480px) {
  .most-popular {
    padding: 8px;
    padding-top: 130px; /* 小屏幕也需要增加头部空间 */
  }

  .header {
    padding: 12px;
  }

  .header h1 {
    font-size: 18px;
  }

  .header-desc {
    font-size: 12px;
  }

  .rank-badge {
    min-width: 20px;
    height: 20px;
    font-size: 10px;
    border-radius: 10px;
    padding: 0 4px;
  }
}
</style>
