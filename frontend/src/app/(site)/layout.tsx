import Link from "next/link"

function NavLink({
  href,
  children,
  ...props
}: { href: string; children: React.ReactNode } & React.ComponentPropsWithoutRef<typeof Link>) {
  return (
    <Link
      href={href}
      className="text-sm font-medium text-slate-700 hover:text-slate-900 dark:text-slate-200 dark:hover:text-white"
      {...props}
    >
      {children}
    </Link>
  )
}

export default function SiteLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <header className="sticky top-0 z-50 border-b border-blue-100/60 bg-white/70 backdrop-blur dark:border-slate-700/60 dark:bg-slate-950/60">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <Link href="/home" className="flex items-center gap-2">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm">
              SB
            </span>
            <span className="text-sm font-semibold tracking-tight text-slate-900 dark:text-white">
              SmartBuoy
            </span>
          </Link>

          <nav className="hidden items-center gap-6 md:flex">
            <NavLink href="/home">Home</NavLink>
            <NavLink href="/dashboard" target="_blank" rel="noopener noreferrer">Dashboard</NavLink>
            <NavLink href="/vision">Vision</NavLink>
            <NavLink href="/contact">Contact</NavLink>
          </nav>

        </div>
      </header>

      <main>{children}</main>

      <footer className="mx-auto max-w-6xl px-6 py-10 text-sm text-slate-500 dark:text-slate-400">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <div className="font-medium text-slate-700 dark:text-slate-200">SmartBuoy</div>
            <div>Empowering smarter oceans with real-time buoy telemetry.</div>
          </div>

          <div className="flex flex-wrap gap-4">
            <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/home">
              Home
            </Link>
            <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/dashboard" target="_blank" rel="noopener noreferrer">
              Dashboard
            </Link>
            <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/vision">
              Vision
            </Link>
            <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/contact">
              Contact
            </Link>
          </div>
        </div>

        <div className="mt-8 border-t border-blue-100/60 pt-6 dark:border-slate-700/60">
          © {new Date().getFullYear()} SmartBuoy Team
        </div>
      </footer>
    </>
  )
}
