import { NextResponse } from "next/server"
import { resolveAuth } from "@/lib/resolveAuth"

export async function POST(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const backendBase =
    process.env.BACKEND_API_BASE_URL || "http://localhost:8000"

  const body = await req.json()

  const res = await fetch(`${backendBase}/deploy`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })

  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
