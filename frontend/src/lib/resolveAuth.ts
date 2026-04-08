import { getServerSession } from "next-auth"
import { authOptions } from "./auth"
import { hashToken } from "./token"
import { prisma } from "./prisma"

export async function resolveAuth(req: Request): Promise<{ userId: string } | null> {
  const session = await getServerSession(authOptions)
  if (session?.user?.id) return { userId: session.user.id }

  const authHeader = req.headers.get("authorization")
  if (!authHeader?.startsWith("Bearer ")) return null

  const raw = authHeader.slice(7)
  const tokenHash = hashToken(raw)

  const token = await prisma.apiToken.findUnique({ where: { tokenHash } })
  if (!token) return null

  void prisma.apiToken.update({
    where: { id: token.id },
    data: { lastUsedAt: new Date() },
  })

  return { userId: token.userId }
}
