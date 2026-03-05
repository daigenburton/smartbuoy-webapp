"use client"

import { signIn } from "next-auth/react"

export default function LoginPage() {
  return (
    <main className="mx-auto max-w-md px-6 py-16">
      <div className="card p-8 text-center">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-white">
          Log in to SmartBuoy
        </h1>

        <p className="mt-3 text-slate-600 dark:text-slate-300">
          Sign in with your Google account to track and manage your buoys.
        </p>

        <button
          onClick={() => signIn("google", { callbackUrl: "/dashboard" })}
          className="btn btn-primary mt-6 w-full"
        >
          Continue with Google
        </button>
      </div>
    </main>
  )
}