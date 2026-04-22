import { useTheme } from '../context/ThemeContext'

export default function SettingsPage() {
  const { dark, toggle } = useTheme()
  return (
    <div className="mx-auto max-w-lg">
      <h1 className="text-2xl font-bold">Settings</h1>
      <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-700 dark:bg-[#141820]">
        <div className="flex items-center justify-between">
          <div>
            <p className="font-medium">Appearance</p>
            <p className="text-sm text-slate-500">Dark mode across the workspace</p>
          </div>
          <button type="button" onClick={toggle} className="rounded-lg border px-4 py-2 text-sm dark:border-slate-600">
            {dark ? 'Dark on' : 'Light on'}
          </button>
        </div>
      </div>
    </div>
  )
}
