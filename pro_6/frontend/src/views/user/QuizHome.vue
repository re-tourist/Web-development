<template>
  <div class="quiz-home">
    <el-card>
      <div class="card-header">
        <h2>欢迎来到答题页面</h2>
        <span class="username">{{ username || '游客' }}</span>
      </div>
      <el-divider></el-divider>
      <el-form :inline="true" label-width="80px" class="form">
        <el-form-item label="题目数量">
          <el-input-number
            v-model="count"
            :min="1"
            :max="maxCount"
            :step="1"
            @change="onCountChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="start">开始答题</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'QuizHome',
  data() {
    return { count: 10, maxCount: 50 }
  },
  computed: {
    username() { return localStorage.getItem('username') }
  },
  created() {
    const c = Number(this.$route.query.count || 0)
    if (c > 0) this.count = c
    // 加载题库最大数量
    import('@/api/client').then(({ default: client }) =>
      client.get('/questions', { params: { page: 1, pageSize: 1 } })
        .then(({ data }) => {
          const total = Number(data && data.total) || 50
          this.maxCount = Math.max(1, total)
          if (this.count > this.maxCount) this.count = this.maxCount
        })
        .catch(() => {})
    )
  },
  methods: {
    start() {
      this.$router.push({ path: '/play', query: { count: this.count } })
    },
    onCountChange(v) {
      const n = Math.floor(Number(v) || 1)
      this.count = Math.max(1, Math.min(this.maxCount, n))
    }
  }
}
</script>

<style scoped>
.quiz-home { max-width: 760px; margin: 24px auto; }
.card-header { display:flex; justify-content:space-between; align-items:center; }
.username { color: #909399; }
.form { margin-top: 8px; }
</style>
