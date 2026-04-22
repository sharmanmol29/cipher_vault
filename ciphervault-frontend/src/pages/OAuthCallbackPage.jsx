import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { setTokens } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function OAuthCallbackPage() {
  const [params] = useSearchParams()
  const nav = useNavigate()
  const { reload } = useAuth()

  useEffect(() => {
    const accessToken = params.get('accessToken')
    const refreshToken = params.get('refreshToken')
    if (accessToken && refreshToken) {
      setTokens({ accessToken, refreshToken })
      reload().then(() => nav('/', { replace: true }))
    } else {
      nav('/login', { replace: true })
    }
  }, [params, nav, reload])

  return (
    <div className="flex min-h-screen items-center justify-center text-slate-500">
      Completing sign-in…
    </div>
  )
}
