// app/api/user/notifications/route.ts
import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { resolveAuth } from "@/lib/resolveAuth";

// GET — fetch notifications for the logged-in user
export async function GET(req: Request) {
  const auth = await resolveAuth(req);
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const notifications = await prisma.notification.findMany({
    where: { userId: auth.userId },
    orderBy: { createdAt: "desc" },
    take: 50,
  });

  return NextResponse.json(notifications);
}

// POST — create a notification (called internally by Java backend)
// Protected by a shared secret header, NOT user session auth
export async function POST(req: Request) {
  const secret = req.headers.get("x-internal-secret");
  if (secret !== process.env.INTERNAL_API_SECRET) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const body = await req.json(); // { userId, buoyId, type, message }

  if (!body.userId || !body.buoyId || !body.type || !body.message) {
    return NextResponse.json({ error: "Missing fields" }, { status: 400 });
  }

  const notification = await prisma.notification.create({
    data: {
      userId: body.userId,
      buoyId: body.buoyId,
      type: body.type,
      message: body.message,
    },
  });

  return NextResponse.json(notification, { status: 201 });
}

// PATCH — mark notifications as read
export async function PATCH(req: Request) {
  const auth = await resolveAuth(req);
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const body = await req.json(); // { notificationIds: ["id1", "id2"] }

  if (!Array.isArray(body.notificationIds) || body.notificationIds.length === 0) {
    return NextResponse.json({ error: "notificationIds must be a non-empty array" }, { status: 400 });
  }

  await prisma.notification.updateMany({
    where: { id: { in: body.notificationIds }, userId: auth.userId },
    data: { read: true },
  });

  return NextResponse.json({ success: true });
}