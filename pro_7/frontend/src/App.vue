<template>
  <a-config-provider :theme="{ token: { colorPrimary: '#1677ff' } }">
    <router-view />
  </a-config-provider>
</template>

<script setup>
// Root component, provide Ant Design theme and render routes
</script>

<style>
html, body, #app { height: 100%; }
body { margin: 0; background: #f7f8fa; }
</style>
