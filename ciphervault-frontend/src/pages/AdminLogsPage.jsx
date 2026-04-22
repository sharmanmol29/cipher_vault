import { useEffect, useState } from 'react'
import * as adminApi from '../api/adminApi'
import { useToast } from '../context/ToastContext'

export default function AdminLogsPage() {
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ content: [], totalPages: 0 })

  const load = async () => {
    const res = await adminApi.listLogs(page, 50)
    setData({ content: res.content || [], totalPages: res.totalPages || 0 })
  }

  useEffect(() => {
    load().catch(() => toast.push('Failed to load logs', 'error'))
  }, [page, toast])

  return (
    <div>
      <h1 className="text-2xl font-bold">Audit log</h1>
      <div className="mt-6 space-y-2">
        {data.content.map((log) => (
          <div key={log.id} className="rounded-xl border border-slate-200 p-3 text-sm dark:border-slate-700 dark:bg-[#141820]">
            <span className="font-medium">{log.action}</span>
            <span className="text-slate-500"> · {log.userEmail || '—'} · </span>
            <span className="text-slate-400">{log.createdAt && new Date(log.createdAt).toLocaleString()}</span>
            {log.details && <p className="mt-1 text-slate-600 dark:text-slate-300">{log.details}</p>}
          </div>
        ))}
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
