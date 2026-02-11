"use client"

import { useState, useEffect, ChangeEvent } from "react"
import DeployBuoy from "./DeployBuoy"
//import BuoyMap from "./BuoyMap"
import dynamic from "next/dynamic"

const BuoyMap = dynamic(() => import("./BuoyMap"), {
  ssr: false,
})
//end of change

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
  const baseTemp =
    buoyId === "buoy-1" ? 52 : buoyId === "buoy-2" ? 55 : 49
  const basePressure =
    buoyId === "buoy-1" ? 1015 : buoyId === "buoy-2" ? 1012 : 1008

  return {
    temperatureF: baseTemp + Math.random() * 2,
    pressureHpa: basePressure + Math.random() * 3,
    latitude: 42.35 + Math.random() * 0.02,
    longitude: -70.99 + Math.random() * 0.02,
    lastUpdated: now,
  }
}

async function fetchBuoyDataFromApi(buoyId: BuoyId): Promise<BuoyData> {
  const res = await fetch(`/api/buoys/${buoyId}`, { cache: "no-store" })
  if (!res.ok) throw new Error(`API responded with ${res.status}`)

  const json = await res.json()

  return {
    temperatureF: json.temperatureF,
    pressureHpa: json.pressureHpa,
    latitude: json.latitude,
    longitude: json.longitude,
    lastUpdated: new Date(json.timestamp).toLocaleTimeString(),
  }
}

export default function DashboardContent() {
  const [selectedBuoy, setSelectedBuoy] = useState<BuoyId>("buoy-1")
  const [data, setData] = useState<BuoyData>(() => createMockData("buoy-1"))
  const [firstLocation, setFirstLocation] = useState<Location | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const selectedBuoyName =
    BUOYS.find(b => b.id === selectedBuoy)?.name ?? "Selected Buoy"

  const loadBuoyData = async (buoyId: BuoyId, resetFirstLocation: boolean) => {
    setIsLoading(true)
    setError(null)

    try {
      const apiData = await fetchBuoyDataFromApi(buoyId)
      setData(apiData)

      if (resetFirstLocation || !firstLocation) {
        setFirstLocation({
          latitude: apiData.latitude,
          longitude: apiData.longitude,
        })
      }
    } catch (err) {
      console.error("API error, falling back to mock:", err)
      setError("Using mock data (backend not reachable).")

      const mock = createMockData(buoyId)
      setData(mock)

      if (resetFirstLocation || !firstLocation) {
        setFirstLocation({
          latitude: mock.latitude,
          longitude: mock.longitude,
        })
      }
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadBuoyData("buoy-1", true)
  }, [])

  const handleChangeBuoy = (event: ChangeEvent<HTMLSelectElement>) => {
    const id = event.target.value as BuoyId
    setSelectedBuoy(id)
    void loadBuoyData(id, true)
  }

  const handleRefresh = () => {
    void loadBuoyData(selectedBuoy, false)
  }

  return (
    <div className="w-full max-w-5xl mx-auto space-y-6">
      
      {/* Header */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="h1 mb-1">SmartBuoy Dashboard</h1>
          <p className="text-sm text-gray-500">
            Monitoring{" "}
            <span className="font-semibold">{selectedBuoyName}</span>
          </p>
          <p className="text-xs text-gray-400">Last updated: {data.lastUpdated}</p>
          {error && <p className="text-xs text-amber-600 mt-1">{error}</p>}
        </div>

        <div className="flex items-center gap-2.5">
          <label className="text-sm text-gray-700 flex flex-col">
            <span className="mb-1 font-medium">Select Buoy</span>
            <select
              value={selectedBuoy}
              onChange={handleChangeBuoy}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm bg-white shadow-sm"
            >
              {BUOYS.map(b => (
                <option key={b.id} value={b.id}>
                  {b.name}
                </option>
              ))}
            </select>
          </label>

          <button
            type="button"
            className="btn btn-primary h-[42px]"
            onClick={handleRefresh}
            disabled={isLoading}
          >
            {isLoading ? "Refreshing..." : "Refresh Data"}
          </button>

          <DeployBuoy buoyId={selectedBuoy} />
        </div>
      </header>

      {/* Temperature + Pressure */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">

        {/* Temperature */}
        <div className="card flex flex-col justify-between">
          <h2 className="text-lg font-semibold mb-2">Temperature</h2>
          <div className="flex items-end justify-between">
            <span className="text-4xl font-bold text-emerald-500">
              {data.temperatureF.toFixed(1)}°F
            </span>
            <span className="text-xs text-gray-400">Source: {selectedBuoyName}</span>
          </div>
        </div>

        {/* Pressure */}
        <div className="card flex flex-col justify-between">
          <h2 className="text-lg font-semibold mb-2">Pressure</h2>
          <div className="flex items-end justify-between">
            <span className="text-4xl font-bold text-sky-500">
              {data.pressureHpa.toFixed(0)} hPa
            </span>
            <span className="text-xs text-gray-400">
              Updated: {data.lastUpdated}
            </span>
          </div>
        </div>
      </section>

      {/* Location + Map */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">

        {/* Numeric Coordinates */}
        <div className="card">
          <h2 className="text-lg font-semibold mb-2">Location</h2>
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

        {/* Map */}
        <div className="card flex flex-col">
          <h2 className="text-lg font-semibold mb-2">Live Map</h2>

          {/* Legend */}
          <div className="flex items-center gap-3 text-xs text-gray-500 mb-3">
            <div className="flex items-center gap-1">
              <span className="inline-block w-3 h-3 rounded-full bg-gray-400" />{" "}
              First position
            </div>
            <div className="flex items-center gap-1">
              <span className="inline-block w-3 h-3 rounded-full bg-emerald-500" />{" "}
              Current position
            </div>
          </div>

          <BuoyMap
            firstLocation={firstLocation}
            currentLocation={{
              latitude: data.latitude,
              longitude: data.longitude,
            }}
          />
        </div>
      </section>
    </div>
  )
}
