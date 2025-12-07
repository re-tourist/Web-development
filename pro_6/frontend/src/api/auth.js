import client from './client'

export function login(username, password) {
  return client.post('/login', { username, password })
}

export function register(username, initialPassword, confirmPassword) {
  return client.post('/register', { username, initialPassword, confirmPassword })
}

