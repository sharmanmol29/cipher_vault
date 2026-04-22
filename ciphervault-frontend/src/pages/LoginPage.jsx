import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import * as authApi from '../api/authApi'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(1, 'Required'),
})

export default function LoginPage() {
  const { login } = useAuth()
  const toast = useToast()
  const nav = useNavigate()
  const loc = useLocation()
  const from = loc.state?.from?.pathname || '/'

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const onSubmit = async (data) => {
    try {
      await login(data)
      toast.push('Signed in')
      nav(from, { replace: true })
    } catch (e) {
      const msg = e.response?.data?.message || 'Login failed'
      toast.push(msg, 'error')
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4">
      <h1 className="mb-2 text-2xl font-bold tracking-tight">Welcome back</h1>
      <p className="mb-6 text-slate-600 dark:text-slate-400">Sign in to CipherVault</p>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-[#141820]">
        <div>
          <label className="text-sm font-medium">Email</label>
          <input className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 dark:border-slate-600 dark:bg-slate-900" type="email" {...register('email')} />
          {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium">Password</label>
          <input className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 dark:border-slate-600 dark:bg-slate-900" type="password" {...register('password')} />
          {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>}
        </div>
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-lg bg-violet-600 py-2.5 font-medium text-white hover:bg-violet-500 disabled:opacity-50"
        >
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
        <a
          href={authApi.googleAuthUrl()}
          className="rounded-lg border border-slate-300 py-2.5 text-center text-sm font-medium hover:bg-slate-50 dark:border-slate-600 dark:hover:bg-slate-800"
        >
          Continue with Google
        </a>
      </form>
      <p className="mt-4 text-center text-sm text-slate-600 dark:text-slate-400">
        <Link className="text-violet-600 hover:underline" to="/signup">
          Create account
        </Link>
        {' · '}
        <Link className="text-violet-600 hover:underline" to="/forgot-password">
          Forgot password
        </Link>
      </p>
    </div>
  )
}
