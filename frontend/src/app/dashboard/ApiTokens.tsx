"use client"

import { useState, useEffect } from "react"

type ApiToken = {
  id: string
  name: string
  createdAt: string
  lastUsedAt: string | null
}

type NewTokenReveal = {
  id: string
  name: string
  token: string
}

export default function ApiTokens() {
  const [tokens, setTokens] = useState<ApiToken[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [newName, setNewName] = useState("")
  const [generating, setGenerating] = useState(false)
  const [generateError, setGenerateError] = useState<string | null>(null)
  const [revealed, setRevealed] = useState<NewTokenReveal | null>(null)
  const [copied, setCopied] = useState(false)

  async function fetchTokens() {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch("/api/tokens")
      if (!res.ok) throw new Error(`Failed to load tokens (${res.status})`)
      setTokens(await res.json())
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load tokens")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void fetchTokens()
  }, [])

  async function handleGenerate() {
    const name = newName.trim()
    if (!name) {
      setGenerateError("Token name is required")
      return
    }
    setGenerating(true)
    setGenerateError(null)
    try {
      const res = await fetch("/api/tokens", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name }),
      })
      if (!res.ok) {
        const body = await res.json()
        throw new Error(body?.error ?? `Failed to create token (${res.status})`)
      }
      const data = await res.json()
      setRevealed({ id: data.id, name: data.name, token: data.token })
      setNewName("")
    } catch (err) {
      setGenerateError(err instanceof Error ? err.message : "Failed to generate token")
    } finally {
      setGenerating(false)
    }
  }

  async function handleRevoke(id: string) {
    try {
      const res = await fetch(`/api/tokens/${id}`, { method: "DELETE" })
      if (!res.ok && res.status !== 204) throw new Error("Failed to revoke token")
      setTokens(prev => prev.filter(t => t.id !== id))
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to revoke token")
    }
  }

  function handleCopy() {
    if (!revealed) return
    void navigator.clipboard.writeText(revealed.token).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  function handleDone() {
    setRevealed(null)
    setCopied(false)
    void fetchTokens()
  }

  return (
    <section className="card">
      <h2 className="text-lg font-semibold mb-4">API Tokens</h2>

      <div className="flex gap-2 mb-4">
        <input
          type="text"
          value={newName}
          onChange={e => setNewName(e.target.value)}
          onKeyDown={e => { if (e.key === "Enter") void handleGenerate() }}
          placeholder="Token name, e.g. raspberry-pi-script"
          className="flex-1 border border-gray-300 rounded-md px-3 py-2 text-sm bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-emerald-400"
          disabled={generating}
        />
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void handleGenerate()}
          disabled={generating}
        >
          {generating ? "Generating..." : "Generate Token"}
        </button>
      </div>

      {generateError && (
        <p className="text-sm text-red-600 mb-3">{generateError}</p>
      )}

      {revealed && (
        <div className="mb-4 rounded-md border border-amber-300 bg-amber-50 p-4 space-y-3">
          <p className="text-sm font-medium text-amber-800">
            Copy this token now — it will not be shown again.
          </p>
          <code className="block w-full break-all rounded bg-white border border-amber-200 px-3 py-2 text-xs font-mono text-gray-800">
            {revealed.token}
          </code>
          <div className="flex gap-2">
            <button
              type="button"
              className="btn btn-primary text-sm"
              onClick={handleCopy}
            >
              {copied ? "Copied!" : "Copy"}
            </button>
            <button
              type="button"
              className="btn btn-ghost text-sm"
              onClick={handleDone}
            >
              Done
            </button>
          </div>
        </div>
      )}

      {loading && <p className="text-sm text-gray-500">Loading tokens...</p>}
      {error && <p className="text-sm text-red-600">{error}</p>}

      {!loading && !error && tokens.length === 0 && (
        <p className="text-sm text-gray-400">No tokens yet.</p>
      )}

      {tokens.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                <th className="pb-2 pr-4 font-medium">Name</th>
                <th className="pb-2 pr-4 font-medium">Created</th>
                <th className="pb-2 pr-4 font-medium">Last used</th>
                <th className="pb-2 font-medium" />
              </tr>
            </thead>
            <tbody>
              {tokens.map(token => (
                <tr key={token.id} className="border-b border-gray-100 last:border-0">
                  <td className="py-2 pr-4 font-medium">{token.name}</td>
                  <td className="py-2 pr-4 text-gray-500">
                    {new Date(token.createdAt).toLocaleDateString()}
                  </td>
                  <td className="py-2 pr-4 text-gray-500">
                    {token.lastUsedAt
                      ? new Date(token.lastUsedAt).toLocaleDateString()
                      : "Never"}
                  </td>
                  <td className="py-2 text-right">
                    <button
                      type="button"
                      className="btn btn-ghost text-xs text-red-600 hover:bg-red-50"
                      onClick={() => void handleRevoke(token.id)}
                    >
                      Revoke
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
