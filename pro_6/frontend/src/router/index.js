import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

const UserView = () => import('../views/element/UserView.vue')
const QuestionView = () => import('../views/element/QuestionView.vue')

const router = new Router({
  mode: 'hash',
  routes: [
    { path: '/user', name: 'user', component: UserView },
    { path: '/question', name: 'question', component: QuestionView },
    { path: '/', redirect: '/user' }
  ]
})

export default router
