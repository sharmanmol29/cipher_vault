import { useAuth } from '../context/AuthContext'

export default function ProfilePage() {
  const { user } = useAuth()
  if (!user) return null
  const pct = Math.min(100, (user.storageUsedBytes / user.storageLimitBytes) * 100)
  return (
    <div className="mx-auto max-w-lg">
      <h1 className="text-2xl font-bold">Profile</h1>
      <div className="mt-6 space-y-4 rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-700 dark:bg-[#141820]">
        <div>
          <p className="text-sm text-slate-500">Email</p>
          <p className="font-medium">{user.email}</p>
        </div>
        <div>
          <p className="text-sm text-slate-500">Display name</p>
          <p className="font-medium">{user.displayName}</p>
        </div>
        <div>
          <p className="text-sm text-slate-500">Role</p>
          <p className="font-medium">{user.role}</p>
        </div>
        <div>
          <p className="text-sm text-slate-500">Storage</p>
          <div className="mt-2 h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
            <div className="h-full rounded-full bg-violet-500 transition-all" style={{ width: `${pct}%` }} />
          </div>
          <p className="mt-1 text-xs text-slate-500">
            {(user.storageUsedBytes / (1024 * 1024)).toFixed(2)} MB of {(user.storageLimitBytes / (1024 * 1024)).toFixed(0)} MB used
          </p>
        </div>
      </div>
    </div>
  )
}
