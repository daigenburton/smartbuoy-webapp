import { NextResponse } from "next/server"
import { resolveAuth } from "@/lib/resolveAuth"
import { prisma } from "@/lib/prisma"

export async function DELETE(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const body = await req.json()
  const { confirmEmail } = body

  const user = await prisma.user.findUnique({ where: { id: auth.userId } })
  if (!user || user.email !== confirmEmail) {
    return NextResponse.json({ error: "Email confirmation does not match" }, { status: 400 })
  }

  await prisma.user.delete({ where: { id: auth.userId } })

  return NextResponse.json({ success: true })
}
