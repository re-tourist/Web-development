import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

// Admin legacy views
const AdminUsers = () => import('../views/element/UserView.vue')
const AdminQuestions = () => import('../views/element/QuestionView.vue')

// Auth & User views
const LoginView = () => import('../views/auth/LoginView.vue')
const RegisterView = () => import('../views/auth/RegisterView.vue')
const UserLayout = () => import('../layouts/UserLayout.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const QuizHome = () => import('../views/user/QuizHome.vue')
const QuizPlay = () => import('../views/user/QuizPlay.vue')

const router = new Router({
  mode: 'hash',
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/register', component: RegisterView, meta: { public: true } },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { roles: ['1'] }, // 1 = ADMIN
      children: [
        { path: 'users', component: AdminUsers },
        { path: 'questions', component: AdminQuestions }
      ]
    },
    {
      path: '/',
      component: UserLayout,
      meta: { roles: ['0','1'] }, // 0 = USER, 1 = ADMIN
      children: [
        { path: '', component: QuizHome },
        { path: 'play', component: QuizPlay }
      ]
    },
    { path: '*', redirect: '/' }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.meta && to.meta.public) return next()
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (!token) return next('/login')
  if (to.meta && to.meta.roles && !to.meta.roles.includes(String(role))) {
    // 无权限时退回用户页面
    return next('/')
  }
  next()
})

export default router
