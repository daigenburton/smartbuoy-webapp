"use client"

import { useSearchParams } from "next/navigation"
import Link from "next/link"
import { Suspense } from "react"

const ERROR_MESSAGES: Record<string, string> = {
  Configuration: "There is a problem with the server configuration.",
  AccessDenied: "You do not have permission to sign in.",
  Verification: "The sign-in link is no longer valid. It may have already been used or expired.",
  OAuthSignin: "Could not start the sign-in process. Please try again.",
  OAuthCallback: "Could not complete sign-in. Please try again.",
  OAuthCreateAccount: "Could not create your account. Please try again.",
  EmailCreateAccount: "Could not create your account. Please try again.",
  Callback: "Could not complete sign-in. Please try again.",
  OAuthAccountNotLinked: "This email is already associated with a different sign-in method.",
  SessionRequired: "You must be signed in to access this page.",
  Default: "An unexpected error occurred. Please try again.",
}

function AuthErrorContent() {
  const params = useSearchParams()
  const errorCode = params.get("error") ?? "Default"
  const message = ERROR_MESSAGES[errorCode] ?? ERROR_MESSAGES.Default

  return (
    <main className="mx-auto max-w-md px-6 py-16">
      <div className="card p-8 text-center">
        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30">
          <svg className="h-6 w-6 text-red-600 dark:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M12 3a9 9 0 110 18A9 9 0 0112 3z" />
          </svg>
        </div>

        <h1 className="text-xl font-semibold text-slate-900 dark:text-white">Sign-in error</h1>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">{message}</p>

        <div className="mt-6 flex flex-col gap-3">
          <Link href="/login" className="btn btn-primary w-full">
            Back to sign in
          </Link>
          <Link href="/home" className="text-sm text-slate-500 hover:text-slate-700 dark:hover:text-slate-300">
            Go to home
          </Link>
        </div>
      </div>
    </main>
  )
}

export default function AuthErrorPage() {
  return (
    <Suspense>
      <AuthErrorContent />
    </Suspense>
  )
}
