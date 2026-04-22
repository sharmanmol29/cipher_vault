import { api } from './client'

export async function listUsers(page = 0, size = 20) {
  const { data } = await api.get('/api/admin/users', { params: { page, size } })
  return data
}

export async function deleteUser(id) {
  await api.delete(`/api/admin/users/${id}`)
}

export async function changeRole(id, role) {
  await api.patch(`/api/admin/users/${id}/role`, { role })
}

export async function listLogs(page = 0, size = 50) {
  const { data } = await api.get('/api/admin/logs', { params: { page, size } })
  return data
}
