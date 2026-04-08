import { NextResponse } from "next/server"
import { resolveAuth } from "@/lib/resolveAuth"
import { prisma } from "@/lib/prisma"
import { logAudit } from "@/lib/audit"

export async function PATCH(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const body = await req.json()
  const updates: { name?: string; email?: string } = {}

  if (typeof body.name === "string") updates.name = body.name.trim()
  if (typeof body.email === "string") updates.email = body.email.trim()

  if (updates.email) {
    const taken = await prisma.user.findFirst({
      where: { email: updates.email, NOT: { id: auth.userId } },
    })
    if (taken) return NextResponse.json({ error: "Email already in use" }, { status: 409 })
  }

  const user = await prisma.user.update({
    where: { id: auth.userId },
    data: updates,
    select: { name: true, email: true },
  })

  await logAudit(auth.userId, "profile_updated")

  return NextResponse.json(user)
}
