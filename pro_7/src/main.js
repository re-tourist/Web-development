import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import routes from './router/index.js'

const router = createRouter({
  history: createWebHistory(),
  routes,
})

createApp(App).use(Antd).use(router).mount('#app')
