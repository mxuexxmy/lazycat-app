<template>
  <div class="developers-view">
    <router-view></router-view>
  </div>
</template>

<script setup lang="ts">
// Component logic here if needed
</script>

<style scoped>
.developers-view {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
  box-sizing: border-box;
}

@media screen and (max-width: 768px) {
  .developers-view {
    padding: 12px;
  }
}

@media screen and (max-width: 480px) {
  .developers-view {
    padding: 8px;
  }
}
</style> 