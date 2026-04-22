import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export async function shareMeta(token) {
  const { data } = await axios.get(`${baseURL}/api/public/share/${token}/meta`)
  return data
}

export async function shareDownload(token, password) {
  const { data } = await axios.post(
    `${baseURL}/api/public/share/${token}/download`,
    password ? { password } : {},
    { responseType: 'blob' }
  )
  return data
}
