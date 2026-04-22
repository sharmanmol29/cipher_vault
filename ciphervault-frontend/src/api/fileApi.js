import { api } from './client'

export async function listFiles(folderId) {
  const { data } = await api.get('/api/files', { params: { folderId } })
  return data
}

export async function listTrash() {
  const { data } = await api.get('/api/files/trash')
  return data
}

export async function uploadFile(file, folderId) {
  const form = new FormData()
  form.append('file', file)
  if (folderId != null) form.append('folderId', String(folderId))
  const { data } = await api.post('/api/files/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export async function downloadFileBlob(id) {
  const { data } = await api.get(`/api/files/download/${id}`, { responseType: 'blob' })
  return data
}

export async function softDeleteFile(id) {
  await api.delete(`/api/files/delete/${id}`)
}

export async function restoreFile(id) {
  await api.post(`/api/files/restore/${id}`)
}

export async function purgeFile(id) {
  await api.delete(`/api/files/purge/${id}`)
}

export async function shareFile(id, password) {
  const { data } = await api.post(`/api/files/share/${id}`, password ? { password } : {})
  return data
}
