<template>
  <div>
    <el-form :inline="true" :model="filters" class="toolbar">
      <el-form-item label="题目关键词">
        <el-input v-model="filters.keyword" placeholder="输入关键词" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="query">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" icon="el-icon-plus" @click="openAdd">添加题目</el-button>
        <el-button type="primary" plain icon="el-icon-download" @click="exportQuestions">导出题目</el-button>
        <el-button type="warning" plain icon="el-icon-upload" @click="importQuestions">导入题目</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="paged" border stripe style="width: 100%;">
      <el-table-column label="序号" width="80" prop="id" />
      <el-table-column label="问题" min-width="240">
        <template slot-scope="scope">
          <span>{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="选项" min-width="280">
        <template slot-scope="scope">
          <el-tag v-for="(opt,i) in scope.row.options" :key="i" type="info" style="margin-right:6px">{{ opt }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="答案" width="140">
        <template slot-scope="scope">
          <el-tag type="success">{{ scope.row.answer }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="filtered.length"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPageChange" />
    </div>

    <el-dialog :title="isEdit ? '编辑题目' : '添加题目'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="qForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="题目" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="选项" prop="options">
          <div class="option-tags">
            <el-tag v-for="(opt,i) in form.options" :key="i" closable @close="removeOption(i)" style="margin:4px">{{ opt }}</el-tag>
          </div>
          <div class="option-input">
            <el-input v-model="optionInput" placeholder="输入选项后回车添加" @keyup.enter.native="addOption" />
            <el-button style="margin-left:8px" @click="addOption">添加选项</el-button>
          </div>
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <el-select v-model="form.answer" placeholder="请选择" style="width: 100%">
            <el-option v-for="(opt,i) in form.options" :key="i" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'QuestionView',
  data() {
    return {
      questions: [],
      filters: { keyword: '' },
      filtered: [],
      page: 1,
      pageSize: 10,
      dialogVisible: false,
      isEdit: false,
      form: { id: null, title: '', options: [], answer: '' },
      optionInput: '',
      rules: {
        title: [{ required: true, message: '请输入题目', trigger: 'blur' }],
        options: [{ validator: (rule, val, cb) => Array.isArray(val) && val.length >= 2 ? cb() : cb(new Error('至少添加两个选项')), trigger: 'change' }],
        answer: [{ required: true, message: '请选择答案', trigger: 'change' }]
      }
    }
  },
  computed: {
    paged() {
      const start = (this.page - 1) * this.pageSize
      return this.filtered.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.seed(400)
    this.query()
  },
  methods: {
    seed(n) {
      const optsList = [
        ['巴黎', '伦敦', '柏林', '罗马'],
        ['北京', '上海', '广州', '深圳'],
        ['红色', '蓝色', '绿色', '黄色']
      ]
      const arr = []
      for (let i = 1; i <= n; i++) {
        const opts = optsList[i % optsList.length]
        arr.push({ id: i, title: '法国的首都是什么？', options: opts, answer: opts[0] })
      }
      this.questions = arr
    },
    matches(q) {
      const k = (this.filters.keyword || '').trim()
      return !k || q.title.includes(k)
    },
    query() {
      this.filtered = this.questions.filter(q => this.matches(q))
      this.page = 1
    },
    reset() {
      this.filters.keyword = ''
      this.query()
    },
    onPageChange(p) { this.page = p },
    openAdd() {
      this.isEdit = false
      this.form = { id: null, title: '', options: [], answer: '' }
      this.optionInput = ''
      this.dialogVisible = true
    },
    openEdit(row) {
      this.isEdit = true
      this.form = { id: row.id, title: row.title, options: [...row.options], answer: row.answer }
      this.optionInput = ''
      this.dialogVisible = true
    },
    remove(row) {
      this.$confirm(`确定删除题目 ${row.title} 吗?`, '提示', { type: 'warning' })
        .then(() => {
          this.questions = this.questions.filter(q => q.id !== row.id)
          this.query()
          this.$message.success('删除成功')
        })
        .catch(() => {})
    },
    addOption() {
      const v = (this.optionInput || '').trim()
      if (!v) return
      if (!this.form.options.includes(v)) this.form.options.push(v)
      if (!this.form.answer) this.form.answer = v
      this.optionInput = ''
    },
    removeOption(i) {
      const removed = this.form.options.splice(i, 1)[0]
      if (this.form.answer === removed) this.form.answer = ''
    },
    submit() {
      this.$refs.qForm.validate(ok => {
        if (!ok) return
        if (!this.form.options.includes(this.form.answer)) {
          this.$message.error('答案必须在选项中')
          return
        }
        if (this.isEdit && this.form.id != null) {
          const idx = this.questions.findIndex(q => q.id === this.form.id)
          if (idx !== -1) this.$set(this.questions, idx, { ...this.form })
        } else {
          const nextId = (this.questions[this.questions.length - 1]?.id || 0) + 1
          this.questions.unshift({ ...this.form, id: nextId })
        }
        this.dialogVisible = false
        this.query()
        this.$message.success(this.isEdit ? '更新成功' : '添加成功')
      })
    },
    exportQuestions() {
      const data = JSON.stringify(this.questions, null, 2)
      const blob = new Blob([data], { type: 'application/json;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'questions.json'
      a.click()
      URL.revokeObjectURL(url)
    },
    importQuestions() {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.json,application/json'
      input.onchange = e => {
        const file = e.target.files[0]
        if (!file) return
        const reader = new FileReader()
        reader.onload = () => {
          try {
            const arr = JSON.parse(reader.result)
            if (Array.isArray(arr)) {
              this.questions = arr.map((q, i) => ({ id: q.id ?? (i + 1), title: q.title || '', options: Array.isArray(q.options) ? q.options : [], answer: q.answer || '' }))
              this.query()
              this.$message.success('导入成功')
            } else {
              this.$message.error('文件格式不正确')
            }
          } catch (err) {
            this.$message.error('解析失败')
          }
        }
        reader.readAsText(file)
      }
      input.click()
    }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.pagination-bar { display:flex; justify-content:flex-end; margin-top: 12px; }
.option-tags { margin-bottom: 8px; }
.option-input { display:flex; }
</style>