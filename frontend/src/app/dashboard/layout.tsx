"use client"

import { useState } from "react"
import Link from "next/link"
import dynamic from "next/dynamic"
import NavAccount from "@/app/components/NavAccount"

const SettingsPanel = dynamic(() => import("./settings/SettingsPanel"), { ssr: false })

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [settingsOpen, setSettingsOpen] = useState(false)

  return (
    <>
      <header className="sticky top-0 z-50 border-b border-blue-100/60 bg-white/70 backdrop-blur dark:border-slate-700/60 dark:bg-slate-950/60">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <Link href="/home" className="flex items-center gap-2">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm text-sm font-bold">
              SB
            </span>
            <span className="text-sm font-semibold tracking-tight text-slate-900 dark:text-white">
              SmartBuoy
            </span>
          </Link>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setSettingsOpen(true)}
              aria-label="Settings"
              className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 text-2xl leading-none"
            >
              ⚙
            </button>
            <NavAccount />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-6 py-8">{children}</main>
      <SettingsPanel open={settingsOpen} onClose={() => setSettingsOpen(false)} />
    </>
  )
}
