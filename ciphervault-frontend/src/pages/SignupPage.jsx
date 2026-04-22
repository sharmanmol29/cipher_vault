import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useToast } from '../context/ToastContext'
import * as authApi from '../api/authApi'

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
  displayName: z.string().min(1),
})

export default function SignupPage() {
  const toast = useToast()
  const nav = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) })

  const onSubmit = async (data) => {
    try {
      await authApi.signup(data)
      toast.push('Account created — you can sign in now')
      nav('/login')
    } catch (e) {
      toast.push(e.response?.data?.message || 'Signup failed', 'error')
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4">
      <h1 className="mb-6 text-2xl font-bold">Create account</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-700 dark:bg-[#141820]">
        <div>
          <label className="text-sm font-medium">Display name</label>
          <input className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 dark:border-slate-600 dark:bg-slate-900" {...register('displayName')} />
          {errors.displayName && <p className="mt-1 text-sm text-red-600">{errors.displayName.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium">Email</label>
          <input className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 dark:border-slate-600 dark:bg-slate-900" type="email" {...register('email')} />
          {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
        </div>
        <div>
          <label className="text-sm font-medium">Password</label>
          <input className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 dark:border-slate-600 dark:bg-slate-900" type="password" {...register('password')} />
          {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>}
        </div>
        <button type="submit" disabled={isSubmitting} className="rounded-lg bg-violet-600 py-2.5 font-medium text-white disabled:opacity-50">
          Sign up
        </button>
        <a href={authApi.googleAuthUrl()} className="rounded-lg border py-2.5 text-center text-sm">
          Sign up with Google
        </a>
      </form>
      <p className="mt-4 text-center text-sm">
        <Link to="/login" className="text-violet-600 hover:underline">
          Already have an account?
        </Link>
      </p>
    </div>
  )
}
