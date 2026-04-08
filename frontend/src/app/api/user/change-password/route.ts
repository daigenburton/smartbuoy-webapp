import { NextResponse } from "next/server"
import bcrypt from "bcryptjs"
import { resolveAuth } from "@/lib/resolveAuth"
import { prisma } from "@/lib/prisma"
import { logAudit } from "@/lib/audit"

export async function POST(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const user = await prisma.user.findUnique({ where: { id: auth.userId } })
  if (!user?.password) {
    return NextResponse.json({ error: "Not available for OAuth accounts" }, { status: 400 })
  }

  const body = await req.json()
  const { currentPassword, newPassword } = body

  const valid = await bcrypt.compare(currentPassword, user.password)
  if (!valid) return NextResponse.json({ error: "Current password is incorrect" }, { status: 401 })

  if (!newPassword || newPassword.length < 8) {
    return NextResponse.json({ error: "New password must be at least 8 characters" }, { status: 400 })
  }

  const hashed = await bcrypt.hash(newPassword, 12)
  await prisma.user.update({ where: { id: auth.userId }, data: { password: hashed } })

  await logAudit(auth.userId, "password_changed")

  return NextResponse.json({ success: true })
}
