import { api, clearTokens, setTokens } from './client'

export async function login(payload) {
  const { data } = await api.post('/api/auth/login', payload)
  setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
  return data
}

export async function signup(payload) {
  await api.post('/api/auth/signup', payload)
}

export async function forgotPassword(email) {
  await api.post('/api/auth/forgot', { email })
}

export async function resetPassword(payload) {
  await api.post('/api/auth/reset', payload)
}

export async function fetchMe() {
  const { data } = await api.get('/api/auth/me')
  return data
}

export function logout() {
  clearTokens()
}

export function googleAuthUrl() {
  const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return `${base}/oauth2/authorization/google`
}
