<template>
  <div class="github-achievements">
    <n-card title="开发者 GitHub 成就" :bordered="false">
      <n-spin :show="loading">
        <div v-if="achievementsList.length > 0" class="achievements-list">
          <n-grid :cols="3" :x-gap="12" :y-gap="12" responsive="screen">
            <n-grid-item
              v-for="achievements in achievementsList"
              :key="achievements.user.id"
            >
              <n-card class="achievement-card">
                <!-- 用户信息 -->
                <div class="user-info">
                  <n-avatar
                    v-if="achievements.user.avatar"
                    :src="achievements.user.avatar"
                    :size="48"
                    round
                  />
                  <n-avatar v-else :size="48" round>
                    {{ achievements.user.nickname.charAt(0).toUpperCase() }}
                  </n-avatar>
                  <div class="user-details">
                    <h3>{{ achievements.user.nickname }}</h3>
                    <n-tag
                      type="info"
                      size="small"
                      @click="
                        openGitHubProfile(achievements.user.githubUsername)
                      "
                      style="cursor: pointer"
                    >
                      <template #icon>
                        <n-icon><logo-github /></n-icon>
                      </template>
                      {{ achievements.user.githubUsername }}
                    </n-tag>
                  </div>
                </div>

                <!-- 贡献统计 -->
                <div class="contributions">
                  <n-grid :cols="2" :x-gap="8" :y-gap="8">
                    <n-grid-item>
                      <n-statistic
                        label="提交"
                        :value="achievements.contributions.totalCommits"
                      >
                        <template #prefix>
                          <n-icon><git-branch-outline /></n-icon>
                        </template>
                      </n-statistic>
                    </n-grid-item>
                    <n-grid-item>
                      <n-statistic
                        label="PR"
                        :value="achievements.contributions.totalPRs"
                      >
                        <template #prefix>
                          <n-icon><git-pull-request-outline /></n-icon>
                        </template>
                      </n-statistic>
                    </n-grid-item>
                    <n-grid-item>
                      <n-statistic
                        label="Issues"
                        :value="achievements.contributions.totalIssues"
                      >
                        <template #prefix>
                          <n-icon><alert-circle-outline /></n-icon>
                        </template>
                      </n-statistic>
                    </n-grid-item>
                    <n-grid-item>
                      <n-statistic
                        label="贡献项目"
                        :value="achievements.contributions.contributedTo"
                      >
                        <template #prefix>
                          <n-icon><code-outline /></n-icon>
                        </template>
                      </n-statistic>
                    </n-grid-item>
                  </n-grid>
                </div>

                <!-- 排名信息 -->
                <div class="rank-info">
                  <n-tag
                    :type="getRankType(achievements.rank.level)"
                    size="large"
                  >
                    {{ achievements.rank.level }}
                  </n-tag>
                  <n-progress
                    type="circle"
                    :percentage="Math.round(achievements.rank.score)"
                    :stroke-width="8"
                    :size="80"
                    :show-indicator="false"
                  >
                    {{ Math.round(achievements.rank.score) }}分
                  </n-progress>
                </div>

                <!-- 编程语言 -->
                <div class="languages">
                  <div
                    v-for="(lang, name) in achievements.languages"
                    :key="name"
                    class="language-item"
                    :style="{ '--color': lang.color }"
                  >
                    <div class="language-name">{{ lang.name }}</div>
                    <div class="language-size">{{ formatSize(lang.size) }}</div>
                  </div>
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </div>
        <div v-else-if="!loading" class="empty-state">
          <n-empty description="暂无 GitHub 成就数据" />
        </div>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import {
  NCard,
  NButton,
  NIcon,
  NSpin,
  NAvatar,
  NTag,
  NGrid,
  NGridItem,
  NStatistic,
  NProgress,
  NEmpty,
} from "naive-ui";
import {
  RefreshOutline,
  LogoGithub,
  GitBranchOutline,
  GitPullRequestOutline,
  AlertCircleOutline,
  CodeOutline,
} from "@vicons/ionicons5";

interface Language {
  color: string;
  size: number;
  name: string;
}

interface Contributions {
  totalCommits: number;
  totalIssues: number;
  totalPRs: number;
  contributedTo: number;
}

interface Rank {
  score: number;
  level: string;
}

interface User {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  description: string;
  githubUsername: string;
}

interface Achievement {
  user: User;
  contributions: Contributions;
  languages: Record<string, Language>;
  rank: Rank;
}

const loading = ref(true);
const achievementsList = ref<Achievement[]>([]);

const fetchData = async () => {
  try {
    loading.value = true;
    const response = await fetch("/api/github/achievements");
    if (!response.ok) throw new Error("获取数据失败");
    achievementsList.value = await response.json();
  } catch (error) {
    console.error("获取 GitHub 成就数据失败:", error);
  } finally {
    loading.value = false;
  }
};

const refreshData = () => {
  fetchData();
};

const getRankType = (level: string) => {
  switch (level) {
    case "A+":
      return "success";
    case "A":
      return "success";
    case "B":
      return "warning";
    case "C":
      return "error";
    default:
      return "default";
  }
};

const formatSize = (size: number) => {
  if (size >= 1000000) {
    return `${(size / 1000000).toFixed(1)}M`;
  }
  if (size >= 1000) {
    return `${(size / 1000).toFixed(1)}K`;
  }
  return size.toString();
};

const openGitHubProfile = (githubUsername: string) => {
  window.open(`https://github.com/${githubUsername}`, "_blank");
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.github-achievements {
  max-width: 1200px;
  margin: 0 auto;
}

.achievements-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.achievement-card {
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-details {
  flex: 1;
}

.user-details h3 {
  margin: 0 0 4px;
  font-size: 16px;
}

.contributions {
  margin: 16px 0;
}

.rank-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 16px 0;
}

.progress-text {
  font-size: 14px;
  font-weight: bold;
}

.languages {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
  margin-top: 16px;
  max-height: 200px;
  overflow-y: auto;
}

.language-item {
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  border-left: 3px solid var(--color);
}

.language-name {
  font-weight: bold;
  margin-bottom: 2px;
  font-size: 14px;
}

.language-size {
  color: #666;
  font-size: 12px;
}

.empty-state {
  padding: 48px 0;
}

@media screen and (max-width: 768px) {
  .github-achievements {
    padding: 16px;
  }

  .languages {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
}
</style>
