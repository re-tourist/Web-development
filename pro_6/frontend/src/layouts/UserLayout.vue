<template>
  <el-container class="layout">
    <el-header height="60px" class="header">
      <div class="brand" @click="$router.push('/')">Quiz</div>
      <div class="spacer"></div>
      <div class="user">
        <span>{{ username || '游客' }}</span>
        <el-button type="text" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <div class="content">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<script>
export default {
  name: 'UserLayout',
  computed: {
    username() {
      return localStorage.getItem('username')
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('token')
      this.$router.replace('/login')
    }
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.header { display: flex; align-items: center; padding: 0 16px; background: #ffffff; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.brand { font-weight: 600; font-size: 18px; cursor: pointer; }
.spacer { flex: 1; }
.user { display: flex; align-items: center; gap: 8px; }
.main { background: #f5f7fa; }
.content { max-width: 960px; margin: 24px auto; }
</style>
