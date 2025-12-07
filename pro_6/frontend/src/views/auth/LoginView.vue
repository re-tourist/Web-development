<template>
  <div class="auth-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>登录</span>
      </div>
      <el-form :model="form" :rules="rules" ref="form" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">登录</el-button>
          <el-button type="text" @click="goRegister">去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'LoginView',
  data() {
    return {
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        login(this.form.username, this.form.password)
          .then(({ data }) => {
            localStorage.setItem('username', data.username)
            localStorage.setItem('role', String(data.role))
            // 简单会话标识（非生产级）
            localStorage.setItem('token', 'logged')
            if (String(data.role) === '1') {
              this.$message.success('管理员登录成功')
              this.$router.replace('/admin/users')
            } else {
              this.$message.success('登录成功')
              this.$router.replace('/')
            }
          })
          .catch(err => {
            const msg = (err && err.response && err.response.data && err.response.data.error) || '登录失败'
            this.$message.error(msg)
          })
      })
    },
    goRegister() {
      this.$router.push('/register')
    }
  }
}
</script>

<style scoped>
.auth-container {
  max-width: 420px;
  margin: 40px auto;
}
</style>

