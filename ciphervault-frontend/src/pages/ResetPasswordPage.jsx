import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useToast } from '../context/ToastContext'
import * as authApi from '../api/authApi'

const schema = z.object({
  newPassword: z.string().min(8),
})

export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const nav = useNavigate()
  const toast = useToast()
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({ resolver: zodResolver(schema) })

  const onSubmit = async (data) => {
    if (!token) {
      toast.push('Missing token', 'error')
      return
    }
    try {
      await authApi.resetPassword({ token, newPassword: data.newPassword })
      toast.push('Password updated')
      nav('/login')
    } catch (e) {
      toast.push(e.response?.data?.message || 'Reset failed', 'error')
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16">
      <h1 className="text-xl font-semibold">Reset password</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 flex flex-col gap-4">
        <input className="rounded-lg border px-3 py-2 dark:border-slate-600 dark:bg-slate-900" type="password" placeholder="New password" {...register('newPassword')} />
        <button type="submit" disabled={isSubmitting} className="rounded-lg bg-violet-600 py-2 text-white">
          Update password
        </button>
      </form>
      <Link to="/login" className="mt-4 inline-block text-sm text-violet-600">
        Login
      </Link>
    </div>
  )
}
