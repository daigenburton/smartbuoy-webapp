"use client"

import { SessionProvider } from "next-auth/react"
import { AlertsProvider } from "./AlertsContext"  
import AlertToast from "./components/AlertToast"

// export default function Providers({ children }: { children: React.ReactNode }) {
//   return <SessionProvider><AlertsProvider>{children}<AlertsProvider></AlertsProvider></SessionProvider>
// }

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <SessionProvider>
      <AlertsProvider>
        {children}
        <AlertToast />        
      </AlertsProvider>         
    </SessionProvider>
  )
}