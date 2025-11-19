// frontend/src/app/dashboard/page.tsx

import DashboardContent from "./DashboardContent"

export const metadata = {
  title: "SmartBuoy - Dashboard",
}

export default function DashboardPage() {
  return (
    <main className="page">
      <DashboardContent />
    </main>
  )
}
