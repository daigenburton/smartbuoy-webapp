import { NextResponse } from "next/server"

export async function GET() {
  const backendBase = process.env.BACKEND_API_BASE_URL || "http://localhost:8000"
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 2000)

  const start = Date.now()

  try {
    const res = await fetch(`${backendBase}/ping`, {
      signal: controller.signal,
      cache: "no-store",
    })
    clearTimeout(timer)

    if (!res.ok) return NextResponse.json({ status: "down" })

    return NextResponse.json({ status: "up", latencyMs: Date.now() - start })
  } catch {
    clearTimeout(timer)
    return NextResponse.json({ status: "down" })
  }
}
