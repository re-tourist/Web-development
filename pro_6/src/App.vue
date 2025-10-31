<template>
  <div id="app">
    <el-container style="height: 100vh;">
      <el-header class="app-header">
        <div class="title">Quiz后台管理</div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="app-aside">
          <div class="aside-section-title" @click="toggleSys">
            <div>
              <i class="el-icon-message" style="margin-right:6px"></i>
              系统信息管理
            </div>
            <i :class="sysOpen ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" class="toggle-icon"></i>
          </div>
          <el-menu :default-active="active">
            <template v-if="sysOpen">
              <el-menu-item index="/user">
                <router-link to="/user">用户管理</router-link>
              </el-menu-item>
              <el-menu-item index="/question">
                <router-link to="/question">题目管理</router-link>
              </el-menu-item>
            </template>
          </el-menu>
        </el-aside>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
  
</template>

<script>
export default {
  name: 'App',
  data() {
    return { active: this.$route.path, sysOpen: true }
  },
  watch: {
    '$route.path'(val) { this.active = val }
  },
  methods: {
    toggleSys() { this.sysOpen = !this.sysOpen }
  }
}
</script>

<style>
html, body, #app { height: 100%; margin: 0; }
.app-header {
  display: flex;
  align-items: center;
  background: #2c3e50;
  color: #fff;
}
.title { font-size: 18px; font-weight: 600; }
.app-aside { background: #f7f7f7; }
.aside-section-title { padding: 12px 16px; color:#606266; border-bottom: 1px solid #ebeef5; cursor: pointer; display:flex; justify-content:space-between; align-items:center; }
.toggle-icon { color:#909399; }
.app-main { padding: 16px; }
/* 去除默认 router-link 样式影响菜单 */
.app-aside a { color: inherit; text-decoration: none; display: block; }
</style>
