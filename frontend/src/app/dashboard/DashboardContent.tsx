"use client"

import { useState, useEffect, ChangeEvent } from "react"

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

type Location = {
  latitude: number
  longitude: number
}

function createMockData(buoyId: BuoyId): BuoyData {
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
  const [firstLocation, setFirstLocation] = useState<Location | null>(null)

  // Ensure we capture the first location on initial load
  useEffect(() => {
    if (!firstLocation) {
      setFirstLocation({
        latitude: data.latitude,
        longitude: data.longitude,
      })
    }
  }, [data, firstLocation])

  const handleChangeBuoy = (event: ChangeEvent<HTMLSelectElement>) => {
    const id = event.target.value as BuoyId
    setSelectedBuoy(id)

    const newData = createMockData(id)
    setData(newData)

    // Reset the base (first) location when switching buoys
    setFirstLocation({
      latitude: newData.latitude,
      longitude: newData.longitude,
    })
  }

  const handleRefresh = () => {
    const newData = createMockData(selectedBuoy)
    setData(newData)
    // Note: we do NOT change firstLocation here,
    // so the relative map shows movement from the original point
  }

  const selectedBuoyName = BUOYS.find(b => b.id === selectedBuoy)?.name ?? "Selected Buoy"

  return (
    <div className="w-full max-w-5xl mx-auto space-y-6">
      {/* Header / Controls row */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="h1 mb-1">SmartBuoy Dashboard</h1>
          <p className="text-sm text-gray-500">
            Monitoring key stats for <span className="font-semibold">{selectedBuoyName}</span>
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
            <span className="text-xs text-gray-400">Source: {selectedBuoyName}</span>
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
              <span className="font-medium">Latitude:</span> {data.latitude.toFixed(4)}
            </p>
            <p>
              <span className="font-medium">Longitude:</span> {data.longitude.toFixed(4)}
            </p>
          </div>
        </div>

        {/* Relative position mini-map */}
        <div className="card flex flex-col">
          <h2 className="text-lg font-semibold mb-2">Relative Position Map</h2>
          <p className="text-sm text-gray-500 mb-4">
            Showing the buoy&apos;s current location relative to its first recorded position for this session.
          </p>

          <div className="flex-1 rounded-lg border border-gray-200 bg-slate-50 flex flex-col gap-3 p-4">
            {/* Legend */}
            <div className="flex items-center gap-3 text-xs text-gray-500">
              <div className="flex items-center gap-1">
                <span className="inline-block w-3 h-3 rounded-full bg-gray-400" /> First position
              </div>
              <div className="flex items-center gap-1">
                <span className="inline-block w-3 h-3 rounded-full bg-emerald-500" /> Current position
              </div>
            </div>

            {/* Relative map box */}
            <div className="relative flex-1 rounded-lg border border-dashed border-gray-300 bg-white overflow-hidden mt-1">
              {firstLocation ? (
                <>
                  {/* Origin point (center) */}
                  <div
                    className="absolute w-3 h-3 rounded-full bg-gray-400"
                    style={{
                      left: "50%",
                      top: "50%",
                      transform: "translate(-50%, -50%)",
                    }}
                  />

                  {/* Current point, offset from center */}
                  <div
                    className="absolute w-4 h-4 rounded-full bg-emerald-500 border-2 border-white shadow"
                    style={{
                      // simple scaling from lat/lon difference -> pixel offsets
                      left: `calc(50% + ${(data.longitude - firstLocation.longitude) * 8000}px)`,
                      top: `calc(50% - ${(data.latitude - firstLocation.latitude) * 8000}px)`,
                      transform: "translate(-50%, -50%)",
                    }}
                  />
                </>
              ) : (
                <div className="w-full h-full flex items-center justify-center text-xs text-gray-400">
                  Waiting for first location…
                </div>
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
