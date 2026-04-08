"use client"

import { useState } from "react"
import dynamic from "next/dynamic"

const AccountSection = dynamic(() => import("./AccountSection"), { ssr: false })
const AppearanceSection = dynamic(() => import("./AppearanceSection"), { ssr: false })
const UnitsSection = dynamic(() => import("./UnitsSection"), { ssr: false })
const SystemSection = dynamic(() => import("./SystemSection"), { ssr: false })

type Tab = "account" | "appearance" | "units" | "system"

const TABS: { id: Tab; label: string }[] = [
  { id: "account", label: "Account" },
  { id: "appearance", label: "Appearance" },
  { id: "units", label: "Units" },
  { id: "system", label: "System" },
]

interface SettingsPanelProps {
  open: boolean
  onClose: () => void
}

export default function SettingsPanel({ open, onClose }: SettingsPanelProps) {
  const [activeTab, setActiveTab] = useState<Tab>("account")

  return (
    <>
      <div
        className={`fixed inset-0 bg-black/40 z-40 transition-opacity duration-300 ${open ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"}`}
        onClick={onClose}
        aria-hidden="true"
      />

      <div
        className={`fixed right-0 top-0 h-full w-full max-w-2xl bg-white dark:bg-gray-900 z-50 shadow-xl flex flex-col transition-transform duration-300 ${open ? "translate-x-0" : "translate-x-full"}`}
      >
        <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-700 px-6 py-4">
          <h1 className="text-lg font-semibold text-slate-900 dark:text-white">Settings</h1>
          <button
            onClick={onClose}
            aria-label="Close settings"
            className="rounded-full p-1.5 text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800 transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </button>
        </div>

        <div className="flex flex-1 overflow-hidden">
          <nav className="w-44 flex-shrink-0 border-r border-slate-200 dark:border-slate-700 py-4 px-2">
            {TABS.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full rounded-lg px-3 py-2 text-left text-sm transition-colors mb-1 ${
                  activeTab === tab.id
                    ? "bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 font-semibold"
                    : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          <div className="flex-1 overflow-y-auto px-6 py-6">
            {activeTab === "account" && <AccountSection />}
            {activeTab === "appearance" && <AppearanceSection />}
            {activeTab === "units" && <UnitsSection />}
            {activeTab === "system" && <SystemSection />}
          </div>
        </div>
      </div>
    </>
  )
}
