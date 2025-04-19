<template>
  <div class="developers-view">
    <div class="content-area">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
</script>

<style scoped>
.developers-view {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
}

.content-area {
  margin-top: 0;
}

@media screen and (max-width: 768px) {
  .developers-view {
    padding: 16px;
  }
}
</style> 