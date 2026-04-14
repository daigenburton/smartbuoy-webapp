// "use client"

// import { useState, useEffect, useRef, ChangeEvent } from "react"
// import DeployBuoy from "./DeployBuoy"
// import ApiTokens from "./ApiTokens"
// import { usePreferences } from "@/lib/usePreferences"
// import { formatTemperature, formatPressure, formatCoordinates, formatTimestamp } from "@/lib/units"
// //import BuoyMap from "./BuoyMap"
// import dynamic from "next/dynamic"

// const BuoyMap = dynamic(() => import("./BuoyMap"), {
//   ssr: false,
// })
// //end of change

// type BuoyId = "buoy-1" | "buoy-2" | "buoy-3"

// const BUOYS: { id: BuoyId; name: string }[] = [
//   { id: "buoy-1", name: "Buoy #1" },
//   { id: "buoy-2", name: "Buoy #2" },
//   { id: "buoy-3", name: "Buoy #3" },
// ]

// type BuoyData = {
//   temperatureF: number
//   pressureHpa: number
//   latitude: number
//   longitude: number
//   lastUpdated: string
//   timestampRaw: number
// }

// type Location = {
//   latitude: number
//   longitude: number
// }


// async function fetchBuoyDataFromApi(buoyId: BuoyId): Promise<BuoyData> {
//   const res = await fetch(`/api/buoys/${buoyId}`, { cache: "no-store" })
//   if (!res.ok) {
//     if (res.status === 404) throw new Error("No data received yet for this buoy.")
//     if (res.status === 401) throw new Error("Not authorized.")
//     throw new Error("Backend unreachable.")
//   }

//   const json = await res.json()

//   return {
//     temperatureF: json.temperatureF,
//     pressureHpa: json.pressureHpa,
//     latitude: json.latitude,
//     longitude: json.longitude,
//     lastUpdated: new Date(json.timestamp).toLocaleTimeString(),
//     timestampRaw: json.timestamp,
//   }
// }

// export default function DashboardContent() {
//   const { preferences } = usePreferences()
//   const [selectedBuoy, setSelectedBuoy] = useState<BuoyId>("buoy-1")
//   const [data, setData] = useState<BuoyData | null>(null)
//   const [firstLocation, setFirstLocation] = useState<Location | null>(null)
//   const [isLoading, setIsLoading] = useState(false)
//   const [error, setError] = useState<string | null>(null)
//   const [backendConnected, setBackendConnected] = useState<boolean | null>(null)
//   const healthIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

//   const selectedBuoyName =
//     BUOYS.find(b => b.id === selectedBuoy)?.name ?? "Selected Buoy"

//   async function checkHealth() {
//     try {
//       const res = await fetch("/api/health/backend", { cache: "no-store" })
//       const json = await res.json()
//       setBackendConnected(json.status === "up")
//     } catch {
//       setBackendConnected(false)
//     }
//   }

//   const loadBuoyData = async (buoyId: BuoyId, resetFirstLocation: boolean) => {
//     setIsLoading(true)
//     setError(null)

//     try {
//       const apiData = await fetchBuoyDataFromApi(buoyId)
//       setData(apiData)

//       if (resetFirstLocation || !firstLocation) {
//         setFirstLocation({
//           latitude: apiData.latitude,
//           longitude: apiData.longitude,
//         })
//       }
//     } catch (err) {
//       setError(err instanceof Error ? err.message : "Backend unreachable.")
//     } finally {
//       setIsLoading(false)
//     }
//   }

//   useEffect(() => {
//     void checkHealth()
//     void loadBuoyData("buoy-1", true)
//     healthIntervalRef.current = setInterval(checkHealth, 15000)
//     return () => {
//       if (healthIntervalRef.current) clearInterval(healthIntervalRef.current)
//     }
//   }, [])

//   const handleChangeBuoy = (event: ChangeEvent<HTMLSelectElement>) => {
//     const id = event.target.value as BuoyId
//     setSelectedBuoy(id)
//     void loadBuoyData(id, true)
//   }

//   const handleRefresh = () => {
//     void loadBuoyData(selectedBuoy, false)
//   }

//   return (
//     <div className="w-full max-w-5xl mx-auto space-y-6">
      
//       {/* Header */}
//       <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
//         <div>
//           <p className="text-sm font-bold text-slate-900 dark:text-white drop-shadow-sm">
//             Monitoring <span>{selectedBuoyName}</span>
//           </p>
//           <p className="text-xs text-gray-400">
//             Last updated:{" "}
//             {data
//               ? formatTimestamp(
//                   new Date(data.timestampRaw).toISOString(),
//                   preferences?.dateFormat ?? "iso",
//                   preferences?.timezone ?? "UTC"
//                 )
//               : "—"}
//           </p>
//           <div className="mt-1 flex items-center gap-1.5">
//             <span className={`h-2 w-2 rounded-full ${
//               backendConnected === null
//                 ? "bg-slate-300 animate-pulse"
//                 : backendConnected
//                   ? "bg-green-500"
//                   : "bg-red-500"
//             }`} />
//             <span className="text-xs text-slate-500 dark:text-slate-400">
//               {backendConnected === null
//                 ? "Checking connection…"
//                 : backendConnected
//                   ? error
//                     ? "Connected · No data"
//                     : "Connected"
//                   : "Backend unreachable"}
//             </span>
//           </div>
//         </div>

//         <div className="flex flex-wrap items-center gap-2.5">
//           <label className="text-sm text-gray-700 flex flex-col">
//             <span className="mb-1 font-medium">Select Buoy</span>
//             <select
//               value={selectedBuoy}
//               onChange={handleChangeBuoy}
//               className="border border-gray-300 rounded-md px-3 py-2 text-sm bg-white shadow-sm"
//             >
//               {BUOYS.map(b => (
//                 <option key={b.id} value={b.id}>
//                   {b.name}
//                 </option>
//               ))}
//             </select>
//           </label>

//           <button
//             type="button"
//             className="btn btn-primary h-[42px]"
//             onClick={handleRefresh}
//             disabled={isLoading}
//           >
//             {isLoading ? "Refreshing..." : "Refresh Data"}
//           </button>

//           <DeployBuoy buoyId={selectedBuoy} />
//         </div>
//       </header>

//       {/* Temperature + Pressure */}
//       <section className="grid grid-cols-1 md:grid-cols-2 gap-4">

//         {/* Temperature */}
//         <div className="card flex flex-col justify-between" style={{ padding: '1.5rem' }}>
//           <h2 className="text-lg font-semibold mb-2">Temperature</h2>
//           <div className="flex items-end justify-between">
//             <span className="text-4xl font-bold" style={{ color: 'var(--color-temp)' }}>
//               {isLoading ? "Loading..." : data ? formatTemperature(data.temperatureF, preferences?.temperatureUnit ?? "celsius") : <span className="text-gray-400">—</span>}
//             </span>
//             <span className="text-xs text-gray-400">Source: {selectedBuoyName}</span>
//           </div>
//         </div>

//         {/* Pressure */}
//         <div className="card flex flex-col justify-between" style={{ padding: '1.5rem' }}>
//           <h2 className="text-lg font-semibold mb-2">Pressure</h2>
//           <div className="flex items-end justify-between">
//             <span className="text-4xl font-bold" style={{ color: 'var(--color-pressure)' }}>
//               {isLoading ? "Loading..." : data ? formatPressure(data.pressureHpa, preferences?.pressureUnit ?? "hpa") : <span className="text-gray-400">—</span>}
//             </span>
//             <span className="text-xs text-gray-400">
//               Updated:{" "}
//               {data
//                 ? formatTimestamp(
//                     new Date(data.timestampRaw).toISOString(),
//                     preferences?.dateFormat ?? "iso",
//                     preferences?.timezone ?? "UTC"
//                   )
//                 : "—"}
//             </span>
//           </div>
//         </div>
//       </section>

//       {/* Location + Map */}
//       <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">

//         {/* Numeric Coordinates */}
//         <div className="card" style={{ padding: '1.5rem' }}>
//           <h2 className="text-lg font-semibold mb-2">Location</h2>
//           <div className="space-y-2 text-sm">
//             {(() => {
//               const coords = data ? formatCoordinates(data.latitude, data.longitude, preferences?.coordinateFormat ?? "decimal") : null
//               return (
//                 <>
//                   <p>
//                     <span className="font-medium">Latitude:</span>{" "}
//                     {isLoading ? "Loading..." : coords ? coords.lat : <span className="text-gray-400">—</span>}
//                   </p>
//                   <p>
//                     <span className="font-medium">Longitude:</span>{" "}
//                     {isLoading ? "Loading..." : coords ? coords.lng : <span className="text-gray-400">—</span>}
//                   </p>
//                 </>
//               )
//             })()}
//           </div>
//         </div>

//         {/* Map */}
//         <div className="card flex flex-col" style={{ padding: '1.5rem' }}>
//           <h2 className="text-lg font-semibold mb-2">Live Map</h2>

//           {/* Legend */}
//           <div className="flex items-center gap-3 text-xs text-gray-500 mb-3">
//             <div className="flex items-center gap-1">
//               <span className="inline-block w-3 h-3 rounded-full bg-gray-400" />{" "}
//               First position
//             </div>
//             <div className="flex items-center gap-1">
//               <span className="inline-block w-3 h-3 rounded-full bg-emerald-500" />{" "}
//               Current position
//             </div>
//           </div>

//           <BuoyMap
//             firstLocation={firstLocation}
//             currentLocation={data ? { latitude: data.latitude, longitude: data.longitude } : null}
//           />
//         </div>
//       </section>

//       <ApiTokens />
//     </div>
//   )
// }

"use client"

import { useState, useEffect, useRef, ChangeEvent } from "react"
import DeployBuoy from "./DeployBuoy"
import ApiTokens from "./ApiTokens"
import { usePreferences } from "@/lib/usePreferences"
import { formatTemperature, formatPressure, formatCoordinates, formatTimestamp } from "@/lib/units"
import dynamic from "next/dynamic"

const BuoyMap = dynamic(() => import("./BuoyMap"), {
  ssr: false,
})

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
  timestampRaw: number
}

type Location = {
  latitude: number
  longitude: number
}

type ShadowState = {
  deployed?: boolean | null
  battery?: number | null
}

async function fetchBuoyDataFromApi(buoyId: BuoyId): Promise<BuoyData> {
  const res = await fetch(`/api/buoys/${buoyId}`, { cache: "no-store" })
  if (!res.ok) {
    if (res.status === 404) throw new Error("No data received yet for this buoy.")
    if (res.status === 401) throw new Error("Not authorized.")
    throw new Error("Backend unreachable.")
  }

  const json = await res.json()

  return {
    temperatureF: json.temperatureF,
    pressureHpa: json.pressureHpa,
    latitude: json.latitude,
    longitude: json.longitude,
    lastUpdated: new Date(json.timestamp).toLocaleTimeString(),
    timestampRaw: json.timestamp,
  }
}

export default function DashboardContent() {
  const { preferences } = usePreferences()
  const [selectedBuoy, setSelectedBuoy] = useState<BuoyId>("buoy-1")
  const [data, setData] = useState<BuoyData | null>(null)
  const [firstLocation, setFirstLocation] = useState<Location | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isDeploying, setIsDeploying] = useState(false)
  const [isFinding, setIsFinding] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [backendConnected, setBackendConnected] = useState<boolean | null>(null)
  const [shadowState, setShadowState] = useState<ShadowState | null>(null)

  const healthIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const selectedBuoyName =
    BUOYS.find((b) => b.id === selectedBuoy)?.name ?? "Selected Buoy"

  async function checkHealth() {
    try {
      const res = await fetch("/api/health/backend", { cache: "no-store" })
      const json = await res.json()
      setBackendConnected(json.status === "up")
    } catch {
      setBackendConnected(false)
    }
  }

  async function loadShadowState(buoyId: BuoyId) {
    try {
      const res = await fetch(`/api/shadow/${buoyId}`, { cache: "no-store" })
      if (!res.ok) return
      const json = await res.json()
      setShadowState(json)
    } catch {
      // ignore until backend route is ready
    }
  }

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
      setError(err instanceof Error ? err.message : "Backend unreachable.")
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void checkHealth()
    void loadBuoyData("buoy-1", true)
    void loadShadowState("buoy-1")

    healthIntervalRef.current = setInterval(() => {
      void checkHealth()
      void loadShadowState(selectedBuoy)
    }, 15000)

    return () => {
      if (healthIntervalRef.current) clearInterval(healthIntervalRef.current)
    }
  }, [])

  const handleChangeBuoy = (event: ChangeEvent<HTMLSelectElement>) => {
    const id = event.target.value as BuoyId
    setSelectedBuoy(id)
    void loadBuoyData(id, true)
    void loadShadowState(id)
  }

  const handleRefresh = () => {
    void loadBuoyData(selectedBuoy, false)
    void loadShadowState(selectedBuoy)
  }

  const handleDeployBuoy = async () => {
    try {
      setIsDeploying(true)
      setError(null)

      const res = await fetch(`/api/shadow/${selectedBuoy}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          deployed: true,
        }),
      })

      if (!res.ok) {
        throw new Error("Failed to deploy buoy")
      }

      await loadShadowState(selectedBuoy)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to deploy buoy")
    } finally {
      setIsDeploying(false)
    }
  }

  const handleFindMyBuoy = async () => {
    try {
      setIsFinding(true)
      setError(null)

      const res = await fetch(`/api/shadow/${selectedBuoy}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          buzzer: true,
          led: true,
        }),
      })

      if (!res.ok) {
        throw new Error("Failed to trigger Find My Buoy")
      }

      await loadShadowState(selectedBuoy)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to trigger Find My Buoy")
    } finally {
      setIsFinding(false)
    }
  }

  return (
    <div className="w-full max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-bold text-slate-900 dark:text-white drop-shadow-sm">
            Monitoring <span>{selectedBuoyName}</span>
          </p>

          <p className="text-xs text-gray-400">
            Last updated:{" "}
            {data
              ? formatTimestamp(
                  new Date(data.timestampRaw).toISOString(),
                  preferences?.dateFormat ?? "iso",
                  preferences?.timezone ?? "UTC"
                )
              : "—"}
          </p>

          <div className="mt-1 flex items-center gap-1.5">
            <span
              className={`h-2 w-2 rounded-full ${
                backendConnected === null
                  ? "bg-slate-300 animate-pulse"
                  : backendConnected
                    ? "bg-green-500"
                    : "bg-red-500"
              }`}
            />
            <span className="text-xs text-slate-500 dark:text-slate-400">
              {backendConnected === null
                ? "Checking connection…"
                : backendConnected
                  ? error
                    ? "Connected · No data"
                    : "Connected"
                  : "Backend unreachable"}
            </span>
          </div>

          <div className="mt-1 text-xs text-slate-500">
            Battery:{" "}
            <span className="font-medium text-slate-700">
              {shadowState?.battery != null ? `${shadowState.battery}%` : "—"}
            </span>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <label className="text-sm text-gray-700 flex flex-col">
            <span className="mb-1 font-medium">Select Buoy</span>
            <select
              value={selectedBuoy}
              onChange={handleChangeBuoy}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm bg-white shadow-sm"
            >
              {BUOYS.map((b) => (
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

          <DeployBuoy buoyId={selectedBuoy} buttonLabel="Set Drift Range" />

          <button
            type="button"
            className="btn btn-primary h-[42px]"
            onClick={handleDeployBuoy}
            disabled={isDeploying}
          >
            {isDeploying ? "Deploying..." : "Deploy Buoy"}
          </button>

          <button
            type="button"
            className="btn btn-primary h-[42px]"
            onClick={handleFindMyBuoy}
            disabled={isFinding}
          >
            {isFinding ? "Finding..." : "Find My Buoy"}
          </button>
        </div>
      </header>

      {/* Temperature + Pressure */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="card flex flex-col justify-between" style={{ padding: "1.5rem" }}>
          <h2 className="text-lg font-semibold mb-2">Temperature</h2>
          <div className="flex items-end justify-between">
            <span className="text-4xl font-bold" style={{ color: "var(--color-temp)" }}>
              {isLoading ? (
                "Loading..."
              ) : data ? (
                formatTemperature(
                  data.temperatureF,
                  preferences?.temperatureUnit ?? "celsius"
                )
              ) : (
                <span className="text-gray-400">—</span>
              )}
            </span>
            <span className="text-xs text-gray-400">Source: {selectedBuoyName}</span>
          </div>
        </div>

        <div className="card flex flex-col justify-between" style={{ padding: "1.5rem" }}>
          <h2 className="text-lg font-semibold mb-2">Pressure</h2>
          <div className="flex items-end justify-between">
            <span className="text-4xl font-bold" style={{ color: "var(--color-pressure)" }}>
              {isLoading ? (
                "Loading..."
              ) : data ? (
                formatPressure(
                  data.pressureHpa,
                  preferences?.pressureUnit ?? "hpa"
                )
              ) : (
                <span className="text-gray-400">—</span>
              )}
            </span>
            <span className="text-xs text-gray-400">
              Updated:{" "}
              {data
                ? formatTimestamp(
                    new Date(data.timestampRaw).toISOString(),
                    preferences?.dateFormat ?? "iso",
                    preferences?.timezone ?? "UTC"
                  )
                : "—"}
            </span>
          </div>
        </div>
      </section>

      {/* Location + Map */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="card" style={{ padding: "1.5rem" }}>
          <h2 className="text-lg font-semibold mb-2">Location</h2>
          <div className="space-y-2 text-sm">
            {(() => {
              const coords = data
                ? formatCoordinates(
                    data.latitude,
                    data.longitude,
                    preferences?.coordinateFormat ?? "decimal"
                  )
                : null

              return (
                <>
                  <p>
                    <span className="font-medium">Latitude:</span>{" "}
                    {isLoading ? "Loading..." : coords ? coords.lat : <span className="text-gray-400">—</span>}
                  </p>
                  <p>
                    <span className="font-medium">Longitude:</span>{" "}
                    {isLoading ? "Loading..." : coords ? coords.lng : <span className="text-gray-400">—</span>}
                  </p>
                </>
              )
            })()}
          </div>
        </div>

        <div className="card flex flex-col" style={{ padding: "1.5rem" }}>
          <h2 className="text-lg font-semibold mb-2">Live Map</h2>

          <div className="flex items-center gap-3 text-xs text-gray-500 mb-3">
            <div className="flex items-center gap-1">
              <span className="inline-block w-3 h-3 rounded-full bg-gray-400" /> First position
            </div>
            <div className="flex items-center gap-1">
              <span className="inline-block w-3 h-3 rounded-full bg-emerald-500" /> Current position
            </div>
          </div>

          <BuoyMap
            firstLocation={firstLocation}
            currentLocation={data ? { latitude: data.latitude, longitude: data.longitude } : null}
          />
        </div>
      </section>

      <ApiTokens />
    </div>
  )
}