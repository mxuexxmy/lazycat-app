<template>
  <div class="rankings-container">
    <n-card title="应用代码仓库" class="repositories-card">
      <template #header>
        <div class="header-content">
          <div class="title">应用代码仓库</div>
          <div class="description">
            这里展示了所有移植开源的应用程序，你可以查看和学习它们的源代码
          </div>
        </div>
      </template>

      <n-spin :show="loading">
        <n-empty
          v-if="!loading && (!apps || apps.length === 0)"
          description="暂无开源应用"
        />

        <div v-else class="apps-grid">
          <n-card
            v-for="app in apps"
            :key="app.pkgId"
            class="app-card"
            :class="{ hoverable: true }"
            @click="handleAppClick(app)"
          >
            <div class="app-info">
              <div class="app-icon">
                <n-image
                  :src="getAppIcon(app)"
                  :fallback-src="defaultIcon"
                  preview-disabled
                  object-fit="contain"
                  class="icon-image"
                />
              </div>
              <div class="app-details">
                <n-ellipsis class="app-name" :tooltip="false">
                  {{ app.name }}
                </n-ellipsis>
                <div class="app-meta">
                  <n-tag size="small" :bordered="false">
                    <template #icon>
                      <n-icon><download-outline /></n-icon>
                    </template>
                    {{ formatDownloads(app.downloadCount) }}
                  </n-tag>
                  <n-tag size="small" type="success" :bordered="false">
                    <template #icon>
                      <n-icon><logo-github /></n-icon>
                    </template>
                    <a
                      :href="app.source"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="repo-link"
                      @click.stop
                    >
                      源码
                    </a>
                  </n-tag>
                </div>
              </div>
            </div>
            <n-tooltip placement="top" trigger="hover">
              <template #trigger>
                <n-ellipsis class="app-description" :line-clamp="2">
                  {{ app.description }}
                </n-ellipsis>
              </template>
              {{ app.description }}
            </n-tooltip>
          </n-card>
        </div>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  NCard,
  NEmpty,
  NSpin,
  NImage,
  NEllipsis,
  NIcon,
  NTag,
  NTooltip,
} from "naive-ui";
import { DownloadOutline, LogoGithub } from "@vicons/ionicons5";

interface AppData {
  packageName: string;
  name: string;
  description: string;
  iconPath: string;
  downloadCount: number | null;
  source: string;
}

// Format downloads to display in 万 units
const formatDownloads = (count: number | null) => {
  if (count === null || count === undefined) return "0";
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`;
  }
  return count.toString();
};

// Get app icon URL
const getAppIcon = (app: AppData) => {
  if (!app.iconPath) {
    return `https://dl.lazycatmicroserver.com` + app.iconPath;
  }
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.packageName}/icon.png`;
};

const defaultIcon = "/app-icon.png";
const router = useRouter();
const loading = ref(true);
const apps = ref<AppData[]>([]);

const fetchApps = async () => {
  try {
    const response = await fetch("/api/app/repositories");
    const data = await response.json();
    apps.value = data;
  } catch (error) {
    console.error("Failed to fetch apps:", error);
  } finally {
    loading.value = false;
  }
};

const handleAppClick = (app: AppData) => {
  router.push({
    name: "AppDetail",
    params: { pkgId: app.packageName },
  });
};

onMounted(() => {
  fetchApps();
});
</script>

<style scoped>
.rankings-container {
  width: 100%;
}

.repositories-card {
  width: 100%;
}

.header-content {
  padding: 16px 0;
}

.title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.description {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.app-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.app-card.hoverable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
}

.app-info {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.app-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.app-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.app-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.app-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.app-icon {
  width: 48px;
  height: 48px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid #f0f0f0;
}

.icon-image {
  width: 100%;
  height: 100%;
  padding: 4px;
}

:deep(.n-card-header) {
  /* padding: 0 !important; */
}

:deep(.n-card) {
  transition: all 0.3s ease;
}

:deep(.n-tag) {
  transition: all 0.3s ease;
}

:deep(.n-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.repo-link {
  color: inherit;
  text-decoration: none;
}

.repo-link:hover {
  text-decoration: underline;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .header-content {
    padding: 12px 16px;
  }

  .title {
    font-size: 18px;
    margin-bottom: 6px;
  }

  .description {
    font-size: 13px;
  }

  .apps-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    margin: 0;
    padding: 16px;
  }

  .app-card {
    border-radius: 12px;
    margin-bottom: 0;
  }

  .app-info {
    gap: 10px;
    margin-bottom: 10px;
  }

  .app-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
  }

  .app-name {
    font-size: 15px;
    margin-bottom: 6px;
  }

  .app-description {
    font-size: 13px;
    line-height: 1.5;
  }

  :deep(.n-tag) {
    font-size: 12px;
    padding: 0 8px;
  }
}

@media screen and (max-width: 480px) {
  .header-content {
    padding: 10px 12px;
  }

  .title {
    font-size: 16px;
  }

  .description {
    font-size: 12px;
  }

  .apps-grid {
    padding: 12px;
    gap: 10px;
  }

  .app-card {
    border-radius: 10px;
  }

  .app-info {
    gap: 8px;
  }

  .app-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
  }

  .app-name {
    font-size: 14px;
    margin-bottom: 4px;
  }

  .app-description {
    font-size: 12px;
  }

  :deep(.n-tag) {
    font-size: 11px;
    padding: 0 6px;
  }
}
</style>
