"use client"

import { usePreferences } from "@/lib/usePreferences"

const THEME_OPTIONS = [
  { value: "light", label: "☀ Light" },
  { value: "system", label: "◐ System" },
  { value: "dark", label: "☾ Dark" },
]

const FONT_OPTIONS = [
  { value: "small", label: "Small" },
  { value: "default", label: "Default" },
  { value: "large", label: "Large" },
  { value: "xl", label: "XL" },
]

export default function AppearanceSection() {
  const { preferences, loading, error, updatePreference } = usePreferences()

  if (loading) return <div className="text-sm text-slate-500 dark:text-slate-400">Loading…</div>
  if (error || !preferences) return <div className="text-sm text-red-500 font-mono">{error ?? "Failed to load preferences."}</div>

  return (
    <div className="space-y-8">
      <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Appearance</h2>

      <div>
        <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Theme</p>
        <div className="flex gap-2">
          {THEME_OPTIONS.map(opt => (
            <button
              key={opt.value}
              onClick={() => updatePreference("theme", opt.value)}
              className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
                preferences.theme === opt.value
                  ? "bg-blue-600 text-white"
                  : "bg-slate-100 text-slate-700 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      <div>
        <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Font Size</p>
        <div className="flex gap-2">
          {FONT_OPTIONS.map(opt => (
            <button
              key={opt.value}
              onClick={() => updatePreference("fontSize", opt.value)}
              className={`rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
                preferences.fontSize === opt.value
                  ? "bg-blue-600 text-white"
                  : "bg-slate-100 text-slate-700 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      <div>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-slate-700 dark:text-slate-300">
              Colorblind-safe palette
            </p>
            <p className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">
              Replaces green/blue chart colors with accessible alternatives
            </p>
          </div>
          <button
            role="switch"
            aria-checked={preferences.colorblindMode}
            onClick={() => updatePreference("colorblindMode", !preferences.colorblindMode)}
            className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
              preferences.colorblindMode ? "bg-blue-600" : "bg-slate-200 dark:bg-slate-700"
            }`}
          >
            <span
              className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ${
                preferences.colorblindMode ? "translate-x-5" : "translate-x-0"
              }`}
            />
          </button>
        </div>
      </div>
    </div>
  )
}
