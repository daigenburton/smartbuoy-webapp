import { NextResponse } from "next/server"
import { resolveAuth } from "@/lib/resolveAuth"
import { prisma } from "@/lib/prisma"

export async function GET(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const logs = await prisma.auditLog.findMany({
    where: { userId: auth.userId },
    orderBy: { createdAt: "desc" },
    take: 100,
  })

  return NextResponse.json(logs)
}
