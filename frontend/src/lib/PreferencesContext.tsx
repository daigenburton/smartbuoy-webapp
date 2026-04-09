"use client"

import { createContext, useContext, useState, useEffect } from "react"

export type UserPreference = {
  id: string
  userId: string
  theme: string
  fontSize: string
  colorblindMode: boolean
  temperatureUnit: string
  pressureUnit: string
  coordinateFormat: string
  timezone: string
  dateFormat: string
  chartTimeRange: string
}

type PreferencesContextValue = {
  preferences: UserPreference | null
  loading: boolean
  error: string | null
  updatePreference: (key: string, value: string | boolean) => Promise<void>
}

const PreferencesContext = createContext<PreferencesContextValue | null>(null)

const THEME_CLASSES = ["light", "dark"] as const
const FONT_CLASSES = ["font-small", "font-large", "font-xl"] as const

function applyClass(html: HTMLElement, classes: readonly string[], value: string, prefix = "") {
  for (const cls of classes) html.classList.remove(cls)
  if (value && value !== "default" && value !== "system") {
    html.classList.add(prefix ? `${prefix}${value}` : value)
  }
}

export function PreferencesProvider({ children }: { children: React.ReactNode }) {
  const [preferences, setPreferences] = useState<UserPreference | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch("/api/user/preferences")
      .then(async res => {
        if (res.ok) return res.json()
        const body = await res.json().catch(() => ({}))
        throw new Error(`${res.status}: ${body.error ?? res.statusText}`)
      })
      .then(data => setPreferences(data))
      .catch(err => setError(String(err)))
      .finally(() => setLoading(false))
  }, [])

  async function updatePreference(key: string, value: string | boolean) {
    setPreferences(prev => prev ? { ...prev, [key]: value } : prev)

    const html = document.documentElement

    if (key === "theme") {
      applyClass(html, THEME_CLASSES, String(value))
      localStorage.setItem("sb_theme", String(value))
    } else if (key === "fontSize") {
      applyClass(html, FONT_CLASSES, String(value), "font-")
      localStorage.setItem("sb_font", String(value))
    } else if (key === "colorblindMode") {
      if (value) html.classList.add("colorblind")
      else html.classList.remove("colorblind")
      localStorage.setItem("sb_colorblind", String(value))
    }

    try {
      await fetch("/api/user/preferences", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ [key]: value }),
      })
    } catch {}
  }

  return (
    <PreferencesContext.Provider value={{ preferences, loading, error, updatePreference }}>
      {children}
    </PreferencesContext.Provider>
  )
}

export function usePreferencesContext() {
  const ctx = useContext(PreferencesContext)
  if (!ctx) throw new Error("usePreferencesContext must be used inside PreferencesProvider")
  return ctx
}
