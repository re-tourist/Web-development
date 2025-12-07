<template>
  <div class="auth-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>注册</span>
      </div>
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码" prop="initialPassword">
          <el-input v-model="form.initialPassword" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">注册</el-button>
          <el-button type="text" @click="goLogin">去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { register } from '@/api/auth'

export default {
  name: 'RegisterView',
  data() {
    return {
      form: { username: '', initialPassword: '', confirmPassword: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        initialPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    onSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        register(this.form.username, this.form.initialPassword, this.form.confirmPassword)
          .then(() => {
            this.$message.success('注册成功，请登录')
            this.$router.replace('/login')
          })
          .catch(err => {
            const msg = (err && err.response && err.response.data && err.response.data.error) || '注册失败'
            this.$message.error(msg)
          })
      })
    },
    goLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.auth-container {
  max-width: 520px;
  margin: 40px auto;
}
</style>

