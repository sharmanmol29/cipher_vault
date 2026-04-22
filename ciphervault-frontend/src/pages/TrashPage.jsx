import { useCallback, useEffect, useState } from 'react'
import * as fileApi from '../api/fileApi'
import { useToast } from '../context/ToastContext'

export default function TrashPage() {
  const toast = useToast()
  const [files, setFiles] = useState([])

  const load = useCallback(async () => {
    const data = await fileApi.listTrash()
    setFiles(data)
  }, [])

  useEffect(() => {
    load().catch(() => toast.push('Failed to load trash', 'error'))
  }, [load, toast])

  return (
    <div>
      <h1 className="text-2xl font-bold">Recycle bin</h1>
      <p className="mt-1 text-sm text-slate-500">Files are permanently removed after the retention period.</p>
      <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {files.map((file) => (
          <div key={file.id} className="rounded-xl border border-slate-200 p-4 dark:border-slate-700 dark:bg-[#141820]">
            <p className="font-medium">{file.originalFilename}</p>
            <p className="text-xs text-slate-500">Trashed {file.trashedAt && new Date(file.trashedAt).toLocaleString()}</p>
            <div className="mt-3 flex gap-3 text-sm">
              <button
                type="button"
                className="text-violet-600"
                onClick={async () => {
                  await fileApi.restoreFile(file.id)
                  toast.push('Restored')
                  load()
                }}
              >
                Restore
              </button>
              <button
                type="button"
                className="text-red-600"
                onClick={async () => {
                  if (!confirm('Permanently delete this file?')) return
                  await fileApi.purgeFile(file.id)
                  toast.push('Permanently deleted')
                  load()
                }}
              >
                Delete forever
              </button>
            </div>
          </div>
        ))}
        {files.length === 0 && <p className="text-slate-500">Bin is empty.</p>}
      </div>
    </div>
  )
}
