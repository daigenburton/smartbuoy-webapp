"use client"

import Link from "next/link"
import Image from "next/image"
import { signOut, useSession } from "next-auth/react"
import { usePathname } from "next/navigation"

export default function NavAccount() {
  const { data: session, status } = useSession()
  const pathname = usePathname()

  const onDashboard = pathname.startsWith("/dashboard")

  if (status === "loading") return null

  // If user not logged in → show login button
  if (!session?.user) {
    return (
      <Link href="/login" className="btn btn-primary px-5 py-2 text-sm">
        Log in
      </Link>
    )
  }

  const name = session.user.name ?? "Account"
  const image = session.user.image

  // HOME PAGE NAVBAR
  if (!onDashboard) {
    return (
      <Link href="/login" className="btn btn-primary px-5 py-2 text-sm">
        Log in
      </Link>
    )
  }

  // DASHBOARD NAVBAR
  return (
    <div className="flex items-center gap-3">
      <div className="flex items-center gap-2 rounded-full border border-blue-100 bg-white/70 px-3 py-1.5 backdrop-blur">
        {image ? (
          <Image
            src={image}
            alt={name}
            width={28}
            height={28}
            className="rounded-full"
          />
        ) : (
          <div className="h-7 w-7 rounded-full bg-blue-600 text-xs font-semibold text-white flex items-center justify-center">
            {name.slice(0, 1).toUpperCase()}
          </div>
        )}

        <span className="text-sm font-medium text-slate-900">
          {name}
        </span>
      </div>

      <button
        onClick={() => signOut({ callbackUrl: "/home" })}
        className="btn btn-primary px-5 py-2 text-sm"
      >
        Log out
      </button>
    </div>
  )
}