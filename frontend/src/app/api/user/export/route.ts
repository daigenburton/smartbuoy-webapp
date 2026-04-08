import { NextResponse } from "next/server"
import { resolveAuth } from "@/lib/resolveAuth"
import { prisma } from "@/lib/prisma"

export async function GET(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const user = await prisma.user.findUnique({
    where: { id: auth.userId },
    include: {
      apiTokens: true,
      preference: true,
      auditLogs: { orderBy: { createdAt: "desc" } },
    },
  })

  if (!user) return NextResponse.json({ error: "User not found" }, { status: 404 })

  const exportData = {
    exportedAt: new Date().toISOString(),
    profile: { name: user.name, email: user.email },
    preferences: user.preference,
    apiTokens: user.apiTokens.map(({ name, createdAt, lastUsedAt }) => ({
      name,
      createdAt,
      lastUsedAt,
    })),
    auditLog: user.auditLogs,
  }

  return new NextResponse(JSON.stringify(exportData, null, 2), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Content-Disposition": 'attachment; filename="smartbuoy-export.json"',
    },
  })
}
