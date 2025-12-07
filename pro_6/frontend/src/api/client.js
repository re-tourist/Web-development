import axios from 'axios'

// 在开发环境(端口8081，使用devServer代理)走相对路径 /api；
// 在静态预览(例如 http-server 端口 8083)直接指向后端 http://localhost:8080/api
const isDevProxy = typeof window !== 'undefined' && window.location && window.location.port === '8081'
const baseURL = isDevProxy ? '/api' : 'http://localhost:8080/api'

const client = axios.create({
  baseURL,
  timeout: 10000
})

client.interceptors.response.use(
  resp => resp,
  err => {
    console.error('API error:', err)
    return Promise.reject(err)
  }
)

export default client
