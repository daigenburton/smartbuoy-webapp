export const metadata = {
  title: "SmartBuoy - Dashboard",
}

export default function DashboardPage() {
  return (
    <main className="page">
      <h1 className="h1 mb-6">SmartBuoy Dashboard</h1>

      <p className="card container-wide text-lg leading-relaxed text-gray-700 mb-10">
        This dashboard will display live and historical buoy stats, including temperature,
        tidal movement, GPS position, and system status.
      </p>

      <section className="card container-wide">
        <h2 className="text-xl font-semibold mb-4">Buoy Overview</h2>
        <p className="text-gray-700">
          Future work: here we will show key metrics such as current water temperature,
          last update time, and buoy health status.
        </p>
      </section>
    </main>
  )
}
