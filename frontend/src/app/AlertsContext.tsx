"use client"

import { createContext, useContext, useEffect, useState } from "react"
import { useSession } from "next-auth/react"

type Alert = {
  buoyId: number
  type: string
  message: string
  timestamp: string
  read: boolean
}

type AlertsContextType = {
  alerts: Alert[]
  unreadCount: number
  markAllRead: () => void
  clearAll: () => void
}

const AlertsContext = createContext<AlertsContextType>({
  alerts: [],
  unreadCount: 0,
  markAllRead: () => {},
  clearAll: () => {},
})

export function AlertsProvider({ children }: { children: React.ReactNode }) {
  const { data: session } = useSession()
  const [alerts, setAlerts] = useState<Alert[]>([])

  useEffect(() => {
    if (!session?.user?.email) return
    const email = session.user.email

    const fetchAlerts = () => {
      fetch(`http://localhost:8000/alerts?userId=${email}`)
        .then(res => res.json())
        .then(data => setAlerts(data))
        .catch(err => console.error("Error fetching alerts:", err))
    }

    fetchAlerts()
    const interval = setInterval(fetchAlerts, 30000)
    return () => clearInterval(interval)
  }, [session])

    const markAllRead = async () => {
        if (!session?.user?.email) return
        const email = session.user.email
    
        try {
        await fetch(`http://localhost:8000/alerts?userId=${email}`, {
            method: "POST",
        })
    
        // update local state AFTER backend succeeds
        setAlerts(prev => prev.map(a => ({ ...a, read: true })))
        } catch (err) {
        console.error("Error marking alerts read:", err)
        }
    }

  const unreadCount = alerts.filter(a => !a.read).length

  const clearAll = () => {
    markAllRead()
  }

  return (
    <AlertsContext.Provider value={{ alerts, unreadCount, markAllRead, clearAll }}>
      {children}
    </AlertsContext.Provider>
  )
}

export const useAlerts = () => useContext(AlertsContext)