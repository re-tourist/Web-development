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
      <el-table-column label="类别" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.role === 1 ? 'danger' : 'success'">{{ scope.row.role === 1 ? '管理员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="密码">
        <template slot-scope="scope">
          <span v-if="!scope.row.showPwd">******</span>
          <span v-else class="pwd-info">明文不可显示（已加密）</span>
          <el-tooltip content="明文不可显示，请在编辑弹窗重置密码" placement="top">
            <el-button type="text" icon="el-icon-view" @click="togglePwd(scope.$index)"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
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
    :total="total"
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
        <el-form-item label="用户类别" prop="userRole">
          <el-select v-model="form.userRole" placeholder="请选择">
            <el-option :label="'普通用户'" :value="0" />
            <el-option :label="'管理员'" :value="1" />
          </el-select>
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
import client from '@/api/client'
export default {
  name: 'UserView',
  data() {
    return {
      // 数据与分页
      users: [],
      filters: { username: '', dateRange: [] },
      total: 0,
      currentPage: 1,
      pageSize: 10,
      // 弹窗
      dialogVisible: false,
      isEdit: false,
      form: { id: null, date: '', username: '', password: '', initialPassword: '', confirmPassword: '', userRole: 0 },
      rulesAdd: {
        date: [{ required: true, message: '请选择日期', trigger: 'change' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        userRole: [{ required: true, message: '请选择用户类别', trigger: 'change' }],
        initialPassword: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: (rule, value, cb) => value === (this.form.initialPassword || '') ? cb() : cb(new Error('两次输入的密码不一致')), trigger: 'blur' }
        ]
      },
      rulesEdit: {
        date: [{ required: true, message: '请选择日期', trigger: 'change' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        userRole: [{ required: true, message: '请选择用户类别', trigger: 'change' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  computed: {
    pagedUsers() {
      // 服务端分页：直接展示当前页数据
      return this.users
    }
  },
  created() {
    this.queryUsers()
  },
  methods: {
    queryUsers() {
      const params = {
        username: this.filters.username || undefined,
        from: (this.filters.dateRange && this.filters.dateRange[0]) || undefined,
        to: (this.filters.dateRange && this.filters.dateRange[1]) || undefined,
        page: this.currentPage,
        pageSize: this.pageSize
      }
      client.get('/users', { params })
        .then(({ data }) => {
          this.users = (data.items || []).map(x => ({ id: x.id, date: x.date, username: x.username, role: x.role ?? 0, showPwd: false }))
          this.total = data.total || 0
        })
        .catch(err => {
          this.$message.error('用户列表加载失败')
          console.error(err)
        })
    },
    resetFilters() {
      this.filters.username = ''
      this.filters.dateRange = []
      this.currentPage = 1
      this.queryUsers()
    },
    onPageChange(p) {
      this.currentPage = p
      this.queryUsers()
    },
    openAdd() {
      this.isEdit = false
      this.form = { id: null, date: '', username: '', password: '', initialPassword: '', confirmPassword: '', userRole: 0 }
      this.dialogVisible = true
    },
    openEdit(row) {
      this.isEdit = true
      this.form = { id: row.id, date: row.date, username: row.username, password: '', initialPassword: '', confirmPassword: '', userRole: row.role ?? 0 }
      this.dialogVisible = true
    },
    togglePwd(i) {
      const u = this.users[i]
      if (u) u.showPwd = !u.showPwd
    },
    removeUser(row) {
      this.$confirm(`确定删除用户 ${row.username} 吗?`, '提示', { type: 'warning' })
        .then(() => client.get('/delUser', { params: { id: row.id } }))
        .then(() => {
          this.$message.success('删除成功')
          this.queryUsers()
        })
        .catch(() => {})
    },
    submitForm() {
      this.$refs.userForm.validate(valid => {
        if (!valid) return
        if (this.isEdit && this.form.id != null) {
          client.put('/updateUser', { id: this.form.id, username: this.form.username, password: this.form.password, role: this.form.userRole })
            .then(() => {
              this.$message.success('更新成功')
              this.dialogVisible = false
              this.queryUsers()
            })
            .catch(err => {
              const msg = err?.response?.data?.error || '更新失败'
              this.$message.error(msg)
            })
        } else {
          client.post('/register', { username: this.form.username, initialPassword: this.form.initialPassword, confirmPassword: this.form.confirmPassword, userRole: this.form.userRole })
            .then(() => {
              this.$message.success('添加成功')
              this.dialogVisible = false
              this.queryUsers()
            })
            .catch(err => {
              const msg = err?.response?.data?.error || '添加失败'
              this.$message.error(msg)
            })
        }
      })
    }
  }
}
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
.pagination-bar { display:flex; justify-content:flex-end; margin-top: 12px; }
.pwd-info { color:#909399; }
</style>
