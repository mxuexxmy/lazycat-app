<template>
  <div class="monthly-new">
    <div class="header">
      <h1>月度新品推荐</h1>
      <p class="header-desc">本月新上架的精选应用</p>
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
                  <div class="rank-badge" v-if="index < 3">{{ index + 1 }}</div>
                  <n-avatar
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    :size="48"
                    object-fit="cover"
                    class="app-icon"
                  />
                  <span class="app-name">{{ app.name }}</span>
                  <n-tag type="success" size="small" round>新上架</n-tag>
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
import { ref, onMounted } from "vue";
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
} from "naive-ui";

interface AppInfo {
  name: string;
  packageName: string;
  description: string;
  brief: string;
  category: string[];
  iconPath: string;
  version: string;
  supportPC: boolean;
  supportMobile: boolean;
}

const loading = ref(false);
const apps = ref<AppInfo[]>([]);
const defaultIcon = "/path/to/default-icon.png";
const router = useRouter();

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) {
    return `https://dl.lazycatmicroserver.com` + app.iconPath;
  }
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.packageName}/icon.png`;
};

const fetchMonthlyNewApps = async () => {
  loading.value = true;
  try {
    const response = await fetch("/api/app/latest");
    const result = await response.json();
    if (Array.isArray(result)) {
      apps.value = result.map((app) => ({
        packageName: app.packageName,
        name: app.name,
        description: app.description,
        brief: app.brief,
        category: app.category || [],
        iconPath: app.iconPath,
        version: app.version,
        supportPC: app.supportPC,
        supportMobile: app.supportMobile,
      }));
    }
  } catch (error) {
    console.error("Failed to fetch monthly new apps:", error);
  } finally {
    loading.value = false;
  }
};

const handleAppClick = (app: AppInfo) => {
  router.push({
    name: "AppDetail",
    params: { pkgId: app.packageName },
  });
};

onMounted(() => {
  fetchMonthlyNewApps();
});
</script>

<style scoped>
.monthly-new {
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

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.app-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
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
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  .monthly-new {
    padding: 16px;
    padding-top: 100px; /* 移动端头部高度较小 */
  }

  .header {
    padding: 16px;
  }

  .header h1 {
    font-size: 20px;
  }

  .header-desc {
    font-size: 13px;
  }

  .app-name {
    font-size: 15px;
  }

  .app-desc {
    font-size: 13px;
  }
}

@media screen and (max-width: 480px) {
  .monthly-new {
    padding: 8px;
    padding-top: 90px;
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
    width: 20px;
    height: 20px;
    font-size: 12px;
  }
}
</style>
