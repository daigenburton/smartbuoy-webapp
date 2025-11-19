"use client"

import { useState } from "react"

type BuoyId = "buoy-1" | "buoy-2" | "buoy-3"

const BUOYS: { id: BuoyId; name: string }[] = [
  { id: "buoy-1", name: "Buoy #1" },
  { id: "buoy-2", name: "Buoy #2" },
  { id: "buoy-3", name: "Buoy #3" },
]

type BuoyData = {
  temperatureF: number
  pressureHpa: number
  latitude: number
  longitude: number
  lastUpdated: string
}

function createMockData(buoyId: BuoyId): BuoyData {
  // NOTE: for now this is mock data – later you’ll replace this with a fetch()
  const now = new Date().toLocaleTimeString()
  const baseTemp = buoyId === "buoy-1" ? 52 : buoyId === "buoy-2" ? 55 : 49
  const basePressure = buoyId === "buoy-1" ? 1015 : buoyId === "buoy-2" ? 1012 : 1008

  return {
    temperatureF: baseTemp + Math.random() * 2,
    pressureHpa: basePressure + Math.random() * 3,
    latitude: 42.35 + Math.random() * 0.02,
    longitude: -70.99 + Math.random() * 0.02,
    lastUpdated: now,
  }
}

export default function DashboardContent() {
  const [selectedBuoy, setSelectedBuoy] = useState<BuoyId>("buoy-1")
  const [data, setData] = useState<BuoyData>(() => createMockData("buoy-1"))

  const handleChangeBuoy = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const id = event.target.value as BuoyId
    setSelectedBuoy(id)
    setData(createMockData(id)) // later: call backend for that buoy
  }

  const handleRefresh = () => {
    setData(createMockData(selectedBuoy))
  }

  return (
    <div className="w-full max-w-5xl mx-auto space-y-6">
      {/* Header / Controls row */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="h1 mb-1">SmartBuoy Dashboard</h1>
          <p className="text-sm text-gray-500">
            Monitoring key stats for <span className="font-semibold">{BUOYS.find(b => b.id === selectedBuoy)?.name}</span>
          </p>
          <p className="text-xs text-gray-400">Last updated: {data.lastUpdated}</p>
        </div>

        <div className="flex items-center gap-3">
          {/* Buoy selector */}
          <label className="text-sm text-gray-700 flex flex-col">
            <span className="mb-1 font-medium">Select Buoy</span>
            <select
              value={selectedBuoy}
              onChange={handleChangeBuoy}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm bg-white shadow-sm"
            >
              {BUOYS.map(buoy => (
                <option key={buoy.id} value={buoy.id}>
                  {buoy.name}
                </option>
              ))}
            </select>
          </label>

          {/* Refresh button */}
          <button type="button" className="btn btn-primary" onClick={handleRefresh}>
            Refresh Data
          </button>
        </div>
      </header>

      {/* Stats row: Temp + Pressure */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Temperature card */}
        <div className="card flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-semibold mb-2">Temperature</h2>
            <p className="text-sm text-gray-500 mb-4">Current water temperature</p>
          </div>
          <div className="flex items-end justify-between">
            <span className="text-4xl font-bold text-emerald-500">
              {data.temperatureF.toFixed(1)}°F
            </span>
            <span className="text-xs text-gray-400">Source: {BUOYS.find(b => b.id === selectedBuoy)?.name}</span>
          </div>
        </div>

        {/* Pressure card */}
        <div className="card flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-semibold mb-2">Pressure</h2>
            <p className="text-sm text-gray-500 mb-4">Estimated water / air pressure</p>
          </div>
          <div className="flex items-end justify-between">
            <span className="text-4xl font-bold text-sky-500">
              {data.pressureHpa.toFixed(0)} hPa
            </span>
            <span className="text-xs text-gray-400">Updated: {data.lastUpdated}</span>
          </div>
        </div>
      </section>

      {/* Location / Map row */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Location numeric info */}
        <div className="card">
          <h2 className="text-lg font-semibold mb-2">Location</h2>
          <p className="text-sm text-gray-500 mb-4">
            Approximate buoy coordinates (placeholder values for now).
          </p>
          <div className="space-y-2 text-sm">
            <p>
              <span className="font-medium">Latitude:</span>{" "}
              {data.latitude.toFixed(4)}
            </p>
            <p>
              <span className="font-medium">Longitude:</span>{" "}
              {data.longitude.toFixed(4)}
            </p>
          </div>
        </div>

        {/* Map placeholder */}
        <div className="card flex flex-col">
          <h2 className="text-lg font-semibold mb-2">Map View (Coming Soon)</h2>
          <p className="text-sm text-gray-500 mb-4">
            This section will show the buoy on a live map once mapping is integrated.
          </p>
          <div className="flex-1 rounded-lg border border-dashed border-gray-300 bg-gray-50 flex items-center justify-center text-sm text-gray-400">
            Map placeholder – integrate Leaflet / Mapbox later
          </div>
        </div>
      </section>
    </div>
  )
}
