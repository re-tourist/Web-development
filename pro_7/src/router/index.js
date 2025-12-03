import HomeView from '../views/HomeView.vue'
import ChatRoom from '../views/ChatRoom.vue'

export default [
  { path: '/', name: 'home', component: HomeView },
  { path: '/room/:id?', name: 'room', component: ChatRoom },
]
