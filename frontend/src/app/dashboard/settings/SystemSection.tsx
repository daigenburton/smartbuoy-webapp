"use client"

import { useEffect, useState } from "react"

type HealthStatus = { status: "up" | "down"; latencyMs?: number } | null

type AuditEntry = {
  id: string
  action: string
  detail: string | null
  createdAt: string
}

const PAGE_SIZE = 10

function formatAction(action: string) {
  return action.replace(/_/g, " ").replace(/^\w/, c => c.toUpperCase())
}

function StatusDot({ status }: { status: HealthStatus }) {
  if (!status) {
    return (
      <span className="flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
        <span className="h-2.5 w-2.5 rounded-full bg-slate-400" />
        Checking...
      </span>
    )
  }
  if (status.status === "up") {
    return (
      <span className="flex items-center gap-2 text-sm text-green-700 dark:text-green-400">
        <span className="h-2.5 w-2.5 rounded-full bg-green-500" />
        Connected ({status.latencyMs}ms)
      </span>
    )
  }
  return (
    <span className="flex items-center gap-2 text-sm text-red-700 dark:text-red-400">
      <span className="h-2.5 w-2.5 rounded-full bg-red-500" />
      Unreachable
    </span>
  )
}

export default function SystemSection() {
  const [health, setHealth] = useState<HealthStatus>(null)
  const [logs, setLogs] = useState<AuditEntry[]>([])
  const [page, setPage] = useState(0)

  useEffect(() => {
    function checkHealth() {
      fetch("/api/health/backend")
        .then(r => r.ok ? r.json() : { status: "down" })
        .then(data => setHealth(data))
        .catch(() => setHealth({ status: "down" }))
    }

    checkHealth()
    const interval = setInterval(checkHealth, 30_000)
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    fetch("/api/user/audit-log")
      .then(r => r.ok ? r.json() : [])
      .then(data => setLogs(Array.isArray(data) ? data : []))
      .catch(() => {})
  }, [])

  const totalPages = Math.ceil(logs.length / PAGE_SIZE)
  const pageEntries = logs.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE)

  return (
    <div>
      <h2 className="mb-6 text-lg font-semibold text-slate-900 dark:text-white">System</h2>

      <div className="mb-8 rounded-lg border border-slate-200 dark:border-slate-700 p-4">
        <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">Backend status</p>
        <StatusDot status={health} />
      </div>

      <div>
        <p className="mb-3 text-sm font-medium text-slate-700 dark:text-slate-300">Audit log</p>

        {logs.length === 0 ? (
          <p className="text-sm text-slate-500 dark:text-slate-400">No activity recorded yet.</p>
        ) : (
          <>
            <div className="overflow-hidden rounded-lg border border-slate-200 dark:border-slate-700">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 dark:bg-slate-800">
                  <tr>
                    <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 dark:text-slate-400 w-40">
                      Time
                    </th>
                    <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 dark:text-slate-400 w-36">
                      Action
                    </th>
                    <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 dark:text-slate-400">
                      Detail
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
                  {pageEntries.map(entry => (
                    <tr key={entry.id} className="bg-white dark:bg-slate-900">
                      <td className="px-3 py-2 text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">
                        {new Date(entry.createdAt).toLocaleString()}
                      </td>
                      <td className="px-3 py-2 text-slate-700 dark:text-slate-300">
                        {formatAction(entry.action)}
                      </td>
                      <td className="px-3 py-2 text-slate-600 dark:text-slate-400">
                        {entry.detail ?? "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <div className="mt-3 flex items-center justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400">
                  Page {page + 1} of {totalPages}
                </span>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="rounded px-3 py-1 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    Prev
                  </button>
                  <button
                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="rounded px-3 py-1 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
