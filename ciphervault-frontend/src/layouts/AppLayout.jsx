import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'

const navCls = ({ isActive }) =>
  `block rounded-lg px-3 py-2 text-sm font-medium transition ${
    isActive
      ? 'bg-violet-100 text-violet-900 dark:bg-violet-500/20 dark:text-violet-200'
      : 'text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'
  }`

export default function AppLayout() {
  const { user, logout } = useAuth()
  const { dark, toggle } = useTheme()

  return (
    <div className="flex min-h-screen bg-slate-50 dark:bg-[#0f1115]">
      <aside className="hidden w-56 shrink-0 border-r border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-[#141820] md:block">
        <Link to="/" className="mb-6 flex items-center gap-2 px-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-violet-600 text-lg font-bold text-white">
            C
          </span>
          <span className="font-semibold tracking-tight">CipherVault</span>
        </Link>
        <nav className="flex flex-col gap-1">
          <NavLink to="/" end className={navCls}>
            My files
          </NavLink>
          <NavLink to="/trash" className={navCls}>
            Recycle bin
          </NavLink>
          <NavLink to="/profile" className={navCls}>
            Profile
          </NavLink>
          <NavLink to="/settings" className={navCls}>
            Settings
          </NavLink>
          {user?.role === 'ADMIN' && (
            <>
              <div className="my-2 border-t border-slate-200 pt-2 dark:border-slate-700" />
              <NavLink to="/admin/users" className={navCls}>
                Admin · Users
              </NavLink>
              <NavLink to="/admin/logs" className={navCls}>
                Admin · Logs
              </NavLink>
            </>
          )}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex flex-col gap-3 border-b border-slate-200 bg-white/80 px-4 py-3 backdrop-blur dark:border-slate-800 dark:bg-[#141820]/90 md:flex-row md:items-center md:justify-between">
          <div className="flex flex-wrap gap-2 md:hidden">
            <NavLink to="/" end className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">
              Files
            </NavLink>
            <NavLink to="/trash" className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">
              Bin
            </NavLink>
            <NavLink to="/profile" className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">
              Profile
            </NavLink>
            {user?.role === 'ADMIN' && (
              <>
                <NavLink to="/admin/users" className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">
                  Users
                </NavLink>
                <NavLink to="/admin/logs" className="rounded-lg bg-slate-100 px-2 py-1 text-xs dark:bg-slate-800">
                  Logs
                </NavLink>
              </>
            )}
          </div>
          <div className="ml-auto flex items-center gap-2">
            <button
              type="button"
              onClick={toggle}
              className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm dark:border-slate-600 dark:bg-slate-800"
            >
              {dark ? 'Light' : 'Dark'}
            </button>
            <span className="hidden text-sm text-slate-500 dark:text-slate-400 sm:inline">{user?.email}</span>
            <button
              type="button"
              onClick={logout}
              className="rounded-lg bg-slate-900 px-3 py-1.5 text-sm text-white dark:bg-violet-600"
            >
              Log out
            </button>
          </div>
        </header>

        <main className="flex-1 p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
