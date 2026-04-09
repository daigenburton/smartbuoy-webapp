// hooks/useNotifications.ts
import { useEffect, useState, useCallback } from "react";

export interface Notification {
  id: string;
  buoyId: number;
  type: string;
  message: string;
  createdAt: string;
  read: boolean;
}

export function useNotifications(pollIntervalMs = 15000) {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  const fetchNotifications = useCallback(async () => {
    const res = await fetch("/api/user/notifications");
    if (res.ok) setNotifications(await res.json());
  }, []);

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, pollIntervalMs);
    return () => clearInterval(interval);
  }, [fetchNotifications, pollIntervalMs]);

  const markAsRead = useCallback(async (ids: string[]) => {
    await fetch("/api/user/notifications", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ notificationIds: ids }),
    });
    setNotifications(prev =>
      prev.map(n => (ids.includes(n.id) ? { ...n, read: true } : n))
    );
  }, []);

  return { notifications, markAsRead, unreadCount: notifications.filter(n => !n.read).length };
}