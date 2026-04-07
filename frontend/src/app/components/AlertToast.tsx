"use client"

import { useEffect, useState } from "react"
import { useAlerts } from "../AlertsContext"


type Alert = {
  buoyId: number
  message: string
  timestamp: string
}

export default function AlertToast() {
  const { alerts } = useAlerts()
  const [visible, setVisible] = useState<Alert | null>(null)
  const [seenTimestamps, setSeenTimestamps] = useState<Set<string>>(new Set())

  useEffect(() => {
    if (alerts.length === 0) return

    const latest = alerts[alerts.length - 1]
    if (seenTimestamps.has(latest.timestamp)) return

    // New alert arrived — show toast
    setSeenTimestamps(prev => new Set(prev).add(latest.timestamp))
    setVisible(latest)

    const timer = setTimeout(() => setVisible(null), 5000)
    return () => clearTimeout(timer)
  }, [alerts])

  if (!visible) return null

  return (
    <div className="fixed bottom-6 right-6 z-50 w-72 bg-slate-900 border border-red-500 rounded-lg shadow-xl p-4 animate-fade-in">
      <div className="flex items-start justify-between gap-2">
        <div>
          <p className="text-sm font-semibold text-red-400">{visible.message}</p>
          <p className="text-xs text-gray-400 mt-1">
            Buoy {visible.buoyId} • {new Date(visible.timestamp).toLocaleString()}
          </p>
        </div>
        <button onClick={() => setVisible(null)} className="text-gray-500 hover:text-white text-xs mt-0.5">✕</button>
      </div>
    </div>
  )
}