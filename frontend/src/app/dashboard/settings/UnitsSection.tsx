"use client"

import { usePreferences } from "@/lib/usePreferences"
import { useMemo, useState } from "react"

const TIMEZONES: string[] = Intl.supportedValuesOf("timeZone")

const selectClass =
  "border border-gray-300 dark:border-gray-600 rounded-md px-3 py-2 text-sm bg-white dark:bg-gray-800 w-full text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"

const TEMPERATURE_OPTIONS = [
  { value: "celsius", label: "Celsius" },
  { value: "fahrenheit", label: "Fahrenheit" },
]

const PRESSURE_OPTIONS = [
  { value: "hpa", label: "hPa" },
  { value: "psi", label: "psi" },
  { value: "atm", label: "atm" },
]

const COORDINATE_OPTIONS = [
  { value: "decimal", label: "Decimal degrees" },
  { value: "dms", label: "DMS" },
]

const DATE_OPTIONS = [
  { value: "iso", label: "ISO 8601" },
  { value: "us", label: "US MM/DD/YYYY" },
  { value: "eu", label: "EU DD/MM/YYYY" },
]

const CHART_RANGE_OPTIONS = [
  { value: "15m", label: "15 minutes" },
  { value: "1h", label: "1 hour" },
  { value: "6h", label: "6 hours" },
  { value: "24h", label: "24 hours" },
  { value: "7d", label: "7 days" },
]

export default function UnitsSection() {
  const { preferences, loading, error, updatePreference } = usePreferences()
  const [tzSearch, setTzSearch] = useState<string | null>(null)
  const [tzOpen, setTzOpen] = useState(false)

  const tzValue = tzSearch ?? preferences?.timezone ?? "UTC"

  const filteredTz = useMemo(() => {
    if (!tzSearch) return TIMEZONES
    const q = tzSearch.toLowerCase()
    return TIMEZONES.filter(tz => tz.toLowerCase().includes(q))
  }, [tzSearch])

  if (loading) return <div className="text-sm text-slate-500 dark:text-slate-400">Loading…</div>
  if (error || !preferences) return <div className="text-sm text-red-500 font-mono">{error ?? "Failed to load preferences."}</div>

  return (
    <div>
      <h2 className="mb-6 text-lg font-semibold text-slate-900 dark:text-white">Units & Format</h2>

      <div className="mb-5">
        <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
          Temperature
        </label>
        <select
          className={selectClass}
          value={preferences.temperatureUnit}
          onChange={e => updatePreference("temperatureUnit", e.target.value)}
        >
          {TEMPERATURE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      <div className="mb-5">
        <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
          Pressure
        </label>
        <select
          className={selectClass}
          value={preferences.pressureUnit}
          onChange={e => updatePreference("pressureUnit", e.target.value)}
        >
          {PRESSURE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      <div className="mb-5">
        <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
          Coordinates
        </label>
        <select
          className={selectClass}
          value={preferences.coordinateFormat}
          onChange={e => updatePreference("coordinateFormat", e.target.value)}
        >
          {COORDINATE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      <div className="mb-5 relative">
        <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
          Timezone
        </label>
        <input
          type="text"
          className={selectClass}
          value={tzValue}
          placeholder="Search timezones…"
          onChange={e => { setTzSearch(e.target.value); setTzOpen(true) }}
          onFocus={() => setTzOpen(true)}
          onBlur={() => setTimeout(() => setTzOpen(false), 150)}
        />
        {tzOpen && filteredTz.length > 0 && (
          <ul className="absolute z-10 mt-1 max-h-52 w-full overflow-auto rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 shadow-lg text-sm">
            {filteredTz.slice(0, 100).map(tz => (
              <li
                key={tz}
                className={`cursor-pointer px-3 py-1.5 hover:bg-blue-50 dark:hover:bg-blue-900/30 ${tz === preferences.timezone ? "font-semibold text-blue-600 dark:text-blue-400" : "text-slate-900 dark:text-white"}`}
                onMouseDown={() => {
                  updatePreference("timezone", tz)
                  setTzSearch(null)
                  setTzOpen(false)
                }}
              >
                {tz}
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="mb-5">
        <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
          Date format
        </label>
        <select
          className={selectClass}
          value={preferences.dateFormat}
          onChange={e => updatePreference("dateFormat", e.target.value)}
        >
          {DATE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>

      <div className="mb-5">
        <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
          Chart time range
        </label>
        <select
          className={selectClass}
          value={preferences.chartTimeRange}
          onChange={e => updatePreference("chartTimeRange", e.target.value)}
        >
          {CHART_RANGE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
      </div>
    </div>
  )
}
