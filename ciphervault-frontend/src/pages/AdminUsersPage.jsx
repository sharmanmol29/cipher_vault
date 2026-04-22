import { useEffect, useState } from 'react'
import * as adminApi from '../api/adminApi'
import { useToast } from '../context/ToastContext'

export default function AdminUsersPage() {
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ content: [], totalPages: 0 })

  const load = async () => {
    const res = await adminApi.listUsers(page, 20)
    setData({ content: res.content || [], totalPages: res.totalPages || 0 })
  }

  useEffect(() => {
    load().catch(() => toast.push('Failed to load users', 'error'))
  }, [page, toast])

  return (
    <div>
      <h1 className="text-2xl font-bold">Users</h1>
      <div className="mt-6 overflow-x-auto rounded-2xl border border-slate-200 dark:border-slate-700">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 dark:bg-slate-900">
            <tr>
              <th className="px-4 py-2">Email</th>
              <th className="px-4 py-2">Role</th>
              <th className="px-4 py-2">Storage</th>
              <th className="px-4 py-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((u) => (
              <tr key={u.id} className="border-t border-slate-200 dark:border-slate-700">
                <td className="px-4 py-2">{u.email}</td>
                <td className="px-4 py-2">{u.role}</td>
                <td className="px-4 py-2">
                  {(u.storageUsedBytes / (1024 * 1024)).toFixed(1)} / {(u.storageLimitBytes / (1024 * 1024)).toFixed(0)} MB
                </td>
                <td className="px-4 py-2 space-x-2">
                  <button
                    type="button"
                    className="text-violet-600"
                    onClick={async () => {
                      const next = u.role === 'ADMIN' ? 'USER' : 'ADMIN'
                      await adminApi.changeRole(u.id, next)
                      toast.push('Role updated')
                      load()
                    }}
                  >
                    Toggle role
                  </button>
                  <button
                    type="button"
                    className="text-red-600"
                    onClick={async () => {
                      if (!confirm('Delete user and all data?')) return
                      await adminApi.deleteUser(u.id)
                      toast.push('User deleted')
                      load()
                    }}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-4 flex gap-2">
        <button type="button" disabled={page <= 0} className="rounded border px-3 py-1 text-sm" onClick={() => setPage((p) => p - 1)}>
          Prev
        </button>
        <button
          type="button"
          disabled={page >= data.totalPages - 1}
          className="rounded border px-3 py-1 text-sm"
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  )
}
