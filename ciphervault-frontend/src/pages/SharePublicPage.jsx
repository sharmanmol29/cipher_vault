import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import * as publicApi from '../api/publicApi'

export default function SharePublicPage() {
  const { token } = useParams()
  const [meta, setMeta] = useState(null)
  const [password, setPassword] = useState('')
  const [err, setErr] = useState('')

  useEffect(() => {
    publicApi
      .shareMeta(token)
      .then(setMeta)
      .catch(() => setErr('Link invalid or expired'))
  }, [token])

  const download = async () => {
    try {
      const blob = await publicApi.shareDownload(token, meta?.passwordProtected ? password : undefined)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = meta?.filename || 'download'
      a.click()
      URL.revokeObjectURL(url)
    } catch {
      setErr('Download failed — check password')
    }
  }

  if (err && !meta) return <div className="flex min-h-screen items-center justify-center text-red-600">{err}</div>
  if (!meta) return <div className="flex min-h-screen items-center justify-center">Loading…</div>

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4">
      <h1 className="text-xl font-bold">Shared file</h1>
      <p className="mt-2 text-slate-600 dark:text-slate-400">{meta.filename}</p>
      {meta.expired && <p className="mt-4 text-red-600">This link has expired.</p>}
      {!meta.expired && (
        <>
          {meta.passwordProtected && (
            <input
              className="mt-4 rounded-lg border px-3 py-2 dark:border-slate-600 dark:bg-slate-900"
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          )}
          <button type="button" onClick={download} className="mt-4 rounded-lg bg-violet-600 py-2.5 font-medium text-white">
            Download
          </button>
          {err && <p className="mt-2 text-sm text-red-600">{err}</p>}
        </>
      )}
    </div>
  )
}
