import { prisma } from "./prisma"

export async function logAudit(userId: string, action: string, detail?: string) {
  await prisma.auditLog.create({ data: { userId, action, detail } })
}
