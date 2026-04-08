"use client"

import { useState } from "react"
import { useSession, signOut } from "next-auth/react"

const inputClass =
  "w-full rounded-md border border-gray-300 dark:border-gray-600 px-3 py-2 text-sm bg-white dark:bg-gray-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"

function InlineMsg({ msg }: { msg: { ok: boolean; text: string } | null }) {
  if (!msg) return null
  return (
    <p className={`mt-1.5 text-xs ${msg.ok ? "text-green-600 dark:text-green-400" : "text-red-600 dark:text-red-400"}`}>
      {msg.text}
    </p>
  )
}

function DisplayNameForm({ initial }: { initial: string }) {
  const { update } = useSession()
  const [name, setName] = useState(initial)
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null)

  async function save() {
    setMsg(null)
    const res = await fetch("/api/user/profile", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name }),
    })
    if (res.ok) {
      await update({ name })
      setMsg({ ok: true, text: "Name updated." })
    } else {
      setMsg({ ok: false, text: "Failed to update name." })
    }
  }

  return (
    <div className="mb-6">
      <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
        Display name
      </label>
      <div className="flex gap-2">
        <input className={inputClass} value={name} onChange={e => setName(e.target.value)} />
        <button
          onClick={save}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
        >
          Save
        </button>
      </div>
      <InlineMsg msg={msg} />
    </div>
  )
}

function EmailForm({ initial }: { initial: string }) {
  const [email, setEmail] = useState(initial)
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null)

  async function save() {
    setMsg(null)
    const res = await fetch("/api/user/profile", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    })
    setMsg(res.ok ? { ok: true, text: "Email updated." } : { ok: false, text: "Failed to update email." })
  }

  return (
    <div className="mb-6">
      <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
        Email
      </label>
      <div className="flex gap-2">
        <input type="email" className={inputClass} value={email} onChange={e => setEmail(e.target.value)} />
        <button
          onClick={save}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
        >
          Save
        </button>
      </div>
      <InlineMsg msg={msg} />
    </div>
  )
}

function ChangePasswordForm() {
  const { data: session } = useSession()
  const [current, setCurrent] = useState("")
  const [next, setNext] = useState("")
  const [confirm, setConfirm] = useState("")
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null)

  const isOAuth = !!(session?.user as { hasPassword?: boolean })?.hasPassword === false
    && !!(session?.user as { image?: string })?.image

  if (isOAuth) {
    return (
      <div className="mb-6">
        <p className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Change password</p>
        <p className="text-sm text-slate-400 dark:text-slate-500">Not available for Google sign-in accounts.</p>
      </div>
    )
  }

  async function submit() {
    setMsg(null)
    if (next !== confirm) return setMsg({ ok: false, text: "New passwords do not match." })
    if (next.length < 8) return setMsg({ ok: false, text: "New password must be at least 8 characters." })
    const res = await fetch("/api/user/change-password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ currentPassword: current, newPassword: next }),
    })
    if (res.ok) {
      setMsg({ ok: true, text: "Password changed." })
      setCurrent(""); setNext(""); setConfirm("")
    } else {
      const data = await res.json().catch(() => ({}))
      setMsg({ ok: false, text: data?.error ?? "Failed to change password." })
    }
  }

  return (
    <div className="mb-6">
      <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Change password</p>
      <div className="space-y-2">
        <input type="password" placeholder="Current password" className={inputClass} value={current} onChange={e => setCurrent(e.target.value)} />
        <input type="password" placeholder="New password" className={inputClass} value={next} onChange={e => setNext(e.target.value)} />
        <input type="password" placeholder="Confirm new password" className={inputClass} value={confirm} onChange={e => setConfirm(e.target.value)} />
      </div>
      <button
        onClick={submit}
        className="mt-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
      >
        Change password
      </button>
      <InlineMsg msg={msg} />
    </div>
  )
}

function ExportDataButton() {
  async function exportData() {
    const res = await fetch("/api/user/export")
    if (!res.ok) return
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = "smartbuoy-data.json"
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="mb-6">
      <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Export my data</p>
      <button
        onClick={exportData}
        className="rounded-md border border-slate-300 dark:border-slate-600 px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
      >
        Export Data (JSON)
      </button>
    </div>
  )
}

function DeleteAccountSection({ userEmail }: { userEmail: string }) {
  const [confirming, setConfirming] = useState(false)
  const [confirmEmail, setConfirmEmail] = useState("")
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null)

  async function deleteAccount() {
    setMsg(null)
    const res = await fetch("/api/user/account", {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ confirmEmail }),
    })
    if (res.ok) {
      signOut({ callbackUrl: "/login" })
    } else {
      const data = await res.json().catch(() => ({}))
      setMsg({ ok: false, text: data?.error ?? "Failed to delete account." })
    }
  }

  return (
    <div className="border-t border-slate-200 dark:border-slate-700 pt-6">
      <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Delete account</p>
      {!confirming ? (
        <button
          onClick={() => setConfirming(true)}
          className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 transition-colors"
        >
          Delete Account
        </button>
      ) : (
        <div>
          <p className="mb-2 text-sm text-slate-600 dark:text-slate-400">
            Type your email <span className="font-mono text-slate-800 dark:text-slate-200">{userEmail}</span> to confirm deletion.
          </p>
          <div className="flex gap-2">
            <input
              type="email"
              className={inputClass}
              placeholder="Enter your email"
              value={confirmEmail}
              onChange={e => setConfirmEmail(e.target.value)}
            />
            <button
              onClick={deleteAccount}
              className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 transition-colors whitespace-nowrap"
            >
              Confirm
            </button>
            <button
              onClick={() => { setConfirming(false); setConfirmEmail(""); setMsg(null) }}
              className="rounded-md border border-slate-300 dark:border-slate-600 px-4 py-2 text-sm text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
            >
              Cancel
            </button>
          </div>
          <InlineMsg msg={msg} />
        </div>
      )}
    </div>
  )
}

export default function AccountSection() {
  const { data: session } = useSession()

  const name = session?.user?.name ?? ""
  const email = session?.user?.email ?? ""

  return (
    <div>
      <h2 className="mb-6 text-lg font-semibold text-slate-900 dark:text-white">Account</h2>
      <DisplayNameForm initial={name} />
      <EmailForm initial={email} />
      <ChangePasswordForm />
      <ExportDataButton />
      <DeleteAccountSection userEmail={email} />
    </div>
  )
}
