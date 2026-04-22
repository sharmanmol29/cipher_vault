import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

const ACCESS = 'cv_access'
const REFRESH = 'cv_refresh'

export function getAccessToken() {
  return localStorage.getItem(ACCESS)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH)
}

export function setTokens({ accessToken, refreshToken }) {
  if (accessToken) localStorage.setItem(ACCESS, accessToken)
  if (refreshToken) localStorage.setItem(REFRESH, refreshToken)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS)
  localStorage.removeItem(REFRESH)
}

api.interceptors.request.use((config) => {
  const t = getAccessToken()
  if (t) {
    config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

let refreshing = null

api.interceptors.response.use(
  (r) => r,
  async (error) => {
    const original = error.config
    if (!original || original._retry) return Promise.reject(error)
    if (error.response?.status !== 401) return Promise.reject(error)
    const rt = getRefreshToken()
    if (!rt || original.url?.includes('/api/auth/refresh')) {
      clearTokens()
      return Promise.reject(error)
    }
    original._retry = true
    try {
      refreshing =
        refreshing ||
        axios.post(`${baseURL}/api/auth/refresh`, { refreshToken: rt }, { headers: { 'Content-Type': 'application/json' } })
      const { data } = await refreshing
      refreshing = null
      setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
      original.headers.Authorization = `Bearer ${data.accessToken}`
      return api(original)
    } catch (e) {
      refreshing = null
      clearTokens()
      return Promise.reject(e)
    }
  }
)
