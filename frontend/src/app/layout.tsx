// import type { Metadata } from "next"
// import Link from "next/link"
// import { Geist, Geist_Mono } from "next/font/google"
// import "./globals.css"
// import "leaflet/dist/leaflet.css"

// const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] })
// const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] })

// export const metadata: Metadata = {
//   title: "SmartBuoy",
//   description:
//     "SmartBuoy is an IoT monitoring system for lobster trap buoys with real-time temperature, pressure, and GPS tracking.",
// }

// function NavLink({ href, children }: { href: string; children: React.ReactNode }) {
//   return (
//     <Link
//       href={href}
//       className="text-sm font-medium text-slate-700 hover:text-slate-900 dark:text-slate-200 dark:hover:text-white"
//     >
//       {children}
//     </Link>
//   )
// }

// export default function RootLayout({ children }: { children: React.ReactNode }) {
//   return (
//     <html lang="en">
//       <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
//         {/* Top Nav */}
//         <header className="sticky top-0 z-50 border-b border-blue-100/60 bg-white/70 backdrop-blur dark:border-slate-700/60 dark:bg-slate-950/60">
//           <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
//             <Link href="/home" className="flex items-center gap-2">
//               <span className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm">
//                 SB
//               </span>
//               <span className="text-sm font-semibold tracking-tight text-slate-900 dark:text-white">
//                 SmartBuoy
//               </span>
//             </Link>

//             <nav className="hidden items-center gap-6 md:flex">
//               <NavLink href="/home">Home</NavLink>
//               <NavLink href="/dashboard">Dashboard</NavLink>
//               <NavLink href="/vision">Vision</NavLink>
//               <NavLink href="/contact">Contact</NavLink>
//             </nav>
//             <div className="flex items-center gap-3">
//               <Link href="/login" className="btn btn-primary px-5 py-2 text-sm">
//                 Log in
//               </Link>
//             </div>
//           </div>
//         </header>

//         {/* Page content */}
//         <main>{children}</main>

//         {/* Footer */}
//         <footer className="mx-auto max-w-6xl px-6 py-10 text-sm text-slate-500 dark:text-slate-400">
//           <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
//             <div>
//               <div className="font-medium text-slate-700 dark:text-slate-200">SmartBuoy</div>
//               <div>Empowering smarter oceans with real-time buoy telemetry.</div>
//             </div>

//             <div className="flex flex-wrap gap-4">
//               <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/home">
//                 Home
//               </Link>
//               <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/dashboard">
//                 Dashboard
//               </Link>
//               <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/vision">
//                 Vision
//               </Link>
//               <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/contact">
//                 Contact
//               </Link>
//             </div>
//           </div>

//           <div className="mt-8 border-t border-blue-100/60 pt-6 dark:border-slate-700/60">
//             © {new Date().getFullYear()} SmartBuoy Team
//           </div>
//         </footer>
//       </body>
//     </html>
//   )
// }

import type { Metadata } from "next"
import Link from "next/link"
import { Geist, Geist_Mono } from "next/font/google"
import "./globals.css"
import "leaflet/dist/leaflet.css"
import Providers from "./providers"

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] })
const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] })

export const metadata: Metadata = {
  title: "SmartBuoy",
  description:
    "SmartBuoy is an IoT monitoring system for lobster trap buoys with real-time temperature, pressure, and GPS tracking.",
}

function NavLink({ href, children }: { href: string; children: React.ReactNode }) {
  return (
    <Link
      href={href}
      className="text-sm font-medium text-slate-700 hover:text-slate-900 dark:text-slate-200 dark:hover:text-white"
    >
      {children}
    </Link>
  )
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <Providers>
          {/* Top Nav */}
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
                <NavLink href="/dashboard">Dashboard</NavLink>
                <NavLink href="/vision">Vision</NavLink>
                <NavLink href="/contact">Contact</NavLink>
              </nav>

              <div className="flex items-center gap-3">
                <Link href="/login" className="btn btn-primary px-5 py-2 text-sm">
                  Log in
                </Link>
              </div>
            </div>
          </header>

          {/* Page content */}
          <main>{children}</main>

          {/* Footer */}
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
                <Link className="hover:text-slate-700 dark:hover:text-slate-200" href="/dashboard">
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
        </Providers>
      </body>
    </html>
  )
}