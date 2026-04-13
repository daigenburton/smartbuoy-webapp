import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { resolveAuth } from "@/lib/resolveAuth";

export async function PATCH(req: Request) {
  const auth = await resolveAuth(req);
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const { id } = await req.json();

  await prisma.notification.updateMany({
    where: {
      id,
      userId: auth.userId,
    },
    data: {
      hidden: true,
    },
  });

  return NextResponse.json({ success: true });
}