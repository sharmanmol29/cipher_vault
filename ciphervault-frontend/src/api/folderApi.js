import { api } from './client'

export async function listFolders(parentId) {
  const { data } = await api.get('/api/folders', { params: { parentId } })
  return data
}

export async function createFolder(payload) {
  const { data } = await api.post('/api/folders/create', payload)
  return data
}

export async function deleteFolder(id) {
  await api.delete(`/api/folders/delete/${id}`)
}
