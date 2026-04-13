"use client"

import { useEffect, useRef, useState, useCallback } from "react"

interface Notification {
  id: string
  buoyId: number
  type: string
  message: string
  createdAt: string
  read: boolean
}

export default function NotificationBell() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [open, setOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const fetchNotifications = useCallback(async () => {
    try {
      const res = await fetch("/api/user/notifications")
      if (res.ok) setNotifications(await res.json())
    } catch {}
  }, [])

  useEffect(() => {
    void fetchNotifications()
    const interval = setInterval(fetchNotifications, 15000)
    return () => clearInterval(interval)
  }, [fetchNotifications])

  // close dropdown menu when clicking outside
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [])

  const unreadCount = notifications.filter(n => !n.read).length

  // const markAllRead = async () => {
  //   const unreadIds = notifications.filter(n => !n.read).map(n => n.id)
  //   if (unreadIds.length === 0) return
  //   await fetch("/api/user/notifications", {
  //     method: "PATCH",
  //     headers: { "Content-Type": "application/json" },
  //     body: JSON.stringify({ notificationIds: unreadIds }),
  //   })
  //   setNotifications(prev => prev.map(n => ({ ...n, read: true })))
  // }

  const markAllRead = async () => {
    const unreadIds = notifications.filter(n => !n.read).map(n => n.id)
    if (unreadIds.length === 0) return
  
    const res = await fetch("/api/user/notifications", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ notificationIds: unreadIds }),
    })
  
    if (!res.ok) {
      console.error("Failed to mark notifications as read")
      return
    }
  
    const data = await res.json()
    console.log("PATCH success:", data)
  
    setNotifications(prev =>
      prev.map(n => ({ ...n, read: true }))
    )
  }

  const dismissNotification = async (id: string) => {
    await fetch("/api/user/notifications/dismiss", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id }),
    });
  
    setNotifications(prev => prev.filter(n => n.id !== id));
  };

  const handleOpen = () => {
    setOpen(prev => !prev)
  }

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell button */}
      <button
        onClick={handleOpen}
        className="relative flex items-center justify-center h-9 w-9 rounded-full border border-blue-100 bg-white/70 backdrop-blur hover:bg-blue-50 transition-colors"
        aria-label="Notifications"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-5 w-5 text-slate-600"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth={2}
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>

        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-blue-600 text-[10px] font-bold text-white flex items-center justify-center">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>
      
      {/* Dropdown */}
      {open && (
        <div className="absolute right-0 mt-2 w-80 rounded-xl border border-slate-200 bg-white shadow-lg z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
            <span className="text-sm font-semibold text-slate-800">Notifications</span>
            {notifications.length > 0 && (
              <button
                onClick={markAllRead}
                className="text-xs text-blue-600 hover:underline"
              >
                Mark all read
              </button>
            )}
          </div>

          <div className="max-h-72 overflow-y-auto divide-y divide-slate-100">
            {notifications.length === 0 ? (
              <p className="px-4 py-6 text-center text-sm text-slate-400">
                No notifications yet
              </p>
            ) : (
              notifications.map(n => (
                <div
                  key={n.id}
                  className={`px-4 py-3 text-sm ${n.read ? "bg-white" : "bg-blue-50"}`}
                >
                <div className="flex justify-between items-start">
                  
                  {/* left side: message */}
                <div>
                  <p className={`font-medium ${n.read ? "text-slate-700" : "text-slate-900"}`}>
                    {n.message}
                  </p>
                  <p className="text-xs text-slate-400 mt-0.5">
                    {new Date(n.createdAt).toLocaleString()}
                  </p>
                </div>
                
                {/* right side: dismiss button */}
                <button
                  onClick={() => dismissNotification(n.id)}
                  className="text-xs text-gray-400 hover:text-red-500 ml-2"
                >
                  ✕
                </button>
              </div>
            </div>
            ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}