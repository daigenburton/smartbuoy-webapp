"use client"

import { useState } from "react"
import { useAlerts } from "../AlertsContext"

export default function AlertBell() {
  const { alerts, unreadCount, markAllRead, clearAll } = useAlerts()
  const [isOpen, setIsOpen] = useState(false)

  const handleOpen = () => {
    setIsOpen(prev => !prev)
  }

  const unreadAlerts = alerts.filter(a => !a.read)

  return (
    <div className="relative">
      <button onClick={handleOpen} className="relative p-2">
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 h-4 w-4 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-slate-900 border border-slate-700 rounded-lg shadow-lg z-50">
          <div className="p-3 border-b border-slate-700 flex items-center justify-between">
            <h3 className="font-semibold text-white text-sm">Alerts</h3>
            {alerts.length > 0 && (
              <button
                onClick={clearAll}
                className="text-xs text-gray-400 hover:text-white"
              >
                Clear all
              </button>
            )}
          </div>
          <ul className="max-h-80 overflow-y-auto">
            {unreadAlerts.length === 0 ? (
                <li className="p-4 text-sm text-gray-400">No alerts</li>
            ) : (
                unreadAlerts.map((alert, i) => (
                <li key={i} className="p-3 border-b border-slate-800 hover:bg-slate-800">
                    <p className="text-sm font-medium text-red-400">{alert.message}</p>
                    <p className="text-xs text-gray-500 mt-1">
                    Buoy {alert.buoyId} • {new Date(alert.timestamp).toLocaleString()}
                    </p>
                </li>
                ))
            )}
            </ul>
        </div>
      )}
    </div>
  )
}