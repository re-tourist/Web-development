<template>
  <div>
    <el-container class="admin-container">
      <el-aside width="220px" class="aside">
        <div class="brand">Quiz Admin</div>
        <el-menu :default-active="active" class="menu" router>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/questions">题目管理</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <div class="content">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  computed: {
    active() {
      if (this.$route.path.includes('/admin/users')) return '/admin/users'
      if (this.$route.path.includes('/admin/questions')) return '/admin/questions'
      return '/admin/users'
    }
  }
}
</script>

<style scoped>
.admin-container { min-height: 100vh; }
.aside { background:#fff; border-right:1px solid #ebeef5; display:flex; flex-direction:column; }
.brand { font-weight:600; font-size:16px; padding:16px; border-bottom:1px solid #f0f2f5; }
.menu { border:none; }
.main { background:#f5f7fa; }
.content { max-width: 960px; margin:24px auto; }
</style>
