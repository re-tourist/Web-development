<template>
  <div class="quiz-play">
    <el-card v-if="loaded">
      <div class="header">
        <div class="progress">
          <span>题目 {{ currentIndex + 1 }} / {{ questions.length }}</span>
          <el-progress :percentage="progress" :stroke-width="8" :show-text="false" />
        </div>
        <div>
          <el-button size="mini" @click="prev" :disabled="currentIndex===0">上一题</el-button>
          <el-button size="mini" @click="next" :disabled="currentIndex>=questions.length-1">下一题</el-button>
        </div>
      </div>
      <div class="body">
        <h3>{{ current.title }}</h3>
        <el-radio-group v-model="answers[currentIndex]">
          <el-radio v-for="opt in current.options" :key="opt" :label="opt">{{ opt }}</el-radio>
        </el-radio-group>
      </div>
      <div class="footer">
        <el-button type="primary" @click="submit" :disabled="submitted">提交答卷</el-button>
        <span v-if="submitted" class="result">得分：{{ score }} / {{ questions.length }}</span>
        <el-button v-if="submitted" @click="goHome">继续答题</el-button>
      </div>
    </el-card>
    <el-empty v-else description="正在加载题目..." />
  </div>
</template>

<script>
import client from '@/api/client'

export default {
  name: 'QuizPlay',
  data() {
    return {
      questions: [],
      answers: {},
      currentIndex: 0,
      submitted: false,
      score: 0,
      loaded: false
    }
  },
  computed: {
    current() {
      return this.questions[this.currentIndex] || { title: '', options: [] }
    },
    progress() {
      if (!this.questions.length) return 0
      return Math.round(((this.currentIndex + 1) / this.questions.length) * 100)
    }
  },
  created() {
    const count = Number(this.$route.query.count || 10)
    client.get('/getQuestion', { params: { count } })
      .then(({ data }) => {
        this.questions = data || []
        this.loaded = true
      })
      .catch(err => {
        console.error(err)
        this.$message.error('题目加载失败')
        this.loaded = true
      })
  },
  methods: {
    prev() { if (this.currentIndex > 0) this.currentIndex-- },
    next() { if (this.currentIndex < this.questions.length - 1) this.currentIndex++ },
    submit() {
      this.submitted = true
      let s = 0
      this.questions.forEach((q, idx) => {
        if (this.answers[idx] && this.answers[idx] === q.answer) s++
      })
      this.score = s
      this.$alert(`你的得分：${s} / ${this.questions.length}`, '答题结果', { type: 'success' })
    },
    goHome() {
      const count = Number(this.$route.query.count || this.questions.length || 10)
      this.$router.push({ path: '/', query: { count } })
    }
  }
}
</script>

<style scoped>
.quiz-play { max-width: 860px; margin: 24px auto; }
.header { display:flex; justify-content:space-between; margin-bottom: 12px; }
.progress { display:flex; flex-direction:column; gap:6px; width: 60%; }
.body { margin: 16px 0; }
.footer { display:flex; align-items:center; gap: 12px; }
.result { margin-left: 12px; font-weight: bold; }
</style>
