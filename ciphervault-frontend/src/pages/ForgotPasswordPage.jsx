import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { useToast } from '../context/ToastContext'
import * as authApi from '../api/authApi'

const schema = z.object({ email: z.string().email() })

export default function ForgotPasswordPage() {
  const toast = useToast()
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({ resolver: zodResolver(schema) })

  const onSubmit = async (data) => {
    try {
      await authApi.forgotPassword(data.email)
      toast.push('If an account exists, reset instructions were sent')
    } catch {
      toast.push('Request failed', 'error')
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16">
      <h1 className="text-xl font-semibold">Forgot password</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 flex flex-col gap-4">
        <input className="rounded-lg border px-3 py-2 dark:border-slate-600 dark:bg-slate-900" placeholder="Email" type="email" {...register('email')} />
        <button type="submit" disabled={isSubmitting} className="rounded-lg bg-violet-600 py-2 text-white">
          Send reset link
        </button>
      </form>
      <Link to="/login" className="mt-4 inline-block text-sm text-violet-600">
        Back to login
      </Link>
    </div>
  )
}
