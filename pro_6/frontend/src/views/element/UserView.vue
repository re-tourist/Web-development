<template>
  <div>
    <!-- 过滤表单 -->
    <el-form :inline="true" :model="filters" class="filter-form">
      <el-form-item label="用户名">
        <el-input v-model="filters.username" placeholder="输入用户名" clearable />
      </el-form-item>
      <el-form-item label="日期区间">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          unlink-panels
          :clearable="true" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="queryUsers">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="success" @click="openAdd">添加用户</el-button>
      </el-form-item>
    </el-form>

    <!-- 用户表格 -->
    <el-table :data="pagedUsers" border stripe style="width: 100%;">
      <el-table-column label="日期" width="180">
        <template slot-scope="scope">
          <i class="el-icon-time" style="margin-right:6px;"></i>
          <span>{{ scope.row.date }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户名" width="200">
        <template slot-scope="scope">
          <el-tag type="primary">{{ scope.row.username }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="password" label="密码" />
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
          <el-button type="text" @click="removeUser(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="filteredUsers.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="onPageChange" />
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <el-dialog :title="isEdit ? '编辑用户' : '添加用户'" :visible.sync="dialogVisible" width="500px">
      <el-form ref="userForm" :model="form" :rules="isEdit ? rulesEdit : rulesAdd" label-width="90px">
        <el-form-item label="日期" prop="date">
          <el-date-picker v-model="form.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <!-- 编辑模式：仅一个密码 -->
        <el-form-item v-if="isEdit" label="密码" prop="password">
          <el-input v-model="form.password" />
        </el-form-item>
        <!-- 添加模式：初始密码 + 确认密码（用 template 承载 v-else） -->
        <template v-else>
          <el-form-item label="初始密码" prop="initialPassword">
            <el-input v-model="form.initialPassword" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" />
          </el-form-item>
        </template>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
  
</template>

<script>
export default {
  name: 'UserView',
  data() {
    return {
      // 原始数据
      users: [],
      // 过滤与分页
      filters: { username: '', dateRange: [] },
      filteredUsers: [],
      currentPage: 1,
      pageSize: 10,
      // 弹窗
      dialogVisible: false,
      isEdit: false,
      form: { id: null, date: '', username: '', password: '', initialPassword: '', confirmPassword: '' },
      rulesAdd: {
        date: [{ required: true, message: '请选择日期', trigger: 'change' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        initialPassword: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: (rule, value, cb) => value === (this.form.initialPassword || '') ? cb() : cb(new Error('两次输入的密码不一致')), trigger: 'blur' }
        ]
      },
      rulesEdit: {
        date: [{ required: true, message: '请选择日期', trigger: 'change' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  computed: {
    pagedUsers() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredUsers.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.seedUsers(400)
    this.queryUsers()
  },
  methods: {
    seedUsers(n) {
      const pad = x => (x < 10 ? '0' + x : '' + x)
      const arr = []
      for (let i = 1; i <= n; i++) {
        const y = 2024 + Math.floor((i % 6) / 3)
        const m = pad((i % 12) + 1)
        const d = pad((i % 28) + 1)
        arr.push({ id: i, date: `${y}-${m}-${d}`, username: `user_${i}`, password: `p${pad(i)}` })
      }
      this.users = arr
    },
    matchesFilters(u) {
      const nameOk = !this.filters.username || u.username.includes(this.filters.username)
      let dateOk = true
      const r = this.filters.dateRange
      if (r && r.length === 2) {
        const from = r[0]
        const to = r[1]
        dateOk = (!from || u.date >= from) && (!to || u.date <= to)
      }
      return nameOk && dateOk
    },
    queryUsers() {
      this.filteredUsers = this.users.filter(u => this.matchesFilters(u))
      this.currentPage = 1
    },
    resetFilters() {
      this.filters.username = ''
      this.filters.dateRange = []
      this.queryUsers()
    },
    onPageChange(p) {
      this.currentPage = p
    },
    openAdd() {
      this.isEdit = false
      this.form = { id: null, date: '', username: '', password: '', initialPassword: '', confirmPassword: '' }
      this.dialogVisible = true
    },
    openEdit(row) {
      this.isEdit = true
      this.form = { id: row.id, date: row.date, username: row.username, password: row.password, initialPassword: '', confirmPassword: '' }
      this.dialogVisible = true
    },
    removeUser(row) {
      this.$confirm(`确定删除用户 ${row.username} 吗?`, '提示', { type: 'warning' })
        .then(() => {
          this.users = this.users.filter(u => u.id !== row.id)
          this.queryUsers()
          this.$message.success('删除成功')
        })
        .catch(() => {})
    },
    submitForm() {
      this.$refs.userForm.validate(valid => {
        if (!valid) return
        if (this.isEdit && this.form.id != null) {
          const idx = this.users.findIndex(u => u.id === this.form.id)
          if (idx !== -1) this.$set(this.users, idx, { id: this.form.id, date: this.form.date, username: this.form.username, password: this.form.password })
        } else {
          // 添加模式：使用初始密码作为最终密码
          const nextId = (this.users[this.users.length - 1]?.id || 0) + 1
          const newUser = { id: nextId, date: this.form.date, username: this.form.username, password: this.form.initialPassword }
          this.users.unshift(newUser)
        }
        this.dialogVisible = false
        this.queryUsers()
        this.$message.success(this.isEdit ? '更新成功' : '添加成功')
      })
    }
  }
}
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
.pagination-bar { display:flex; justify-content:flex-end; margin-top: 12px; }
</style>