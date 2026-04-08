export function formatTemperature(celsius: number, unit: string): string {
  if (unit === "fahrenheit") return `${((celsius * 9) / 5 + 32).toFixed(1)}°F`
  return `${celsius.toFixed(1)}°C`
}

export function formatPressure(hpa: number, unit: string): string {
  switch (unit) {
    case "mbar": return `${hpa.toFixed(0)} mbar`
    case "atm":  return `${(hpa / 1013.25).toFixed(4)} atm`
    case "psi":  return `${(hpa * 0.0145038).toFixed(2)} psi`
    default:     return `${hpa.toFixed(0)} hPa`
  }
}

export function formatCoordinates(
  lat: number, lng: number, format: string
): { lat: string; lng: string } {
  if (format === "dms") {
    return { lat: toDMS(lat, "lat"), lng: toDMS(lng, "lng") }
  }
  return { lat: lat.toFixed(4), lng: lng.toFixed(4) }
}

function toDMS(decimal: number, axis: "lat" | "lng"): string {
  const abs = Math.abs(decimal)
  const deg = Math.floor(abs)
  const minFloat = (abs - deg) * 60
  const min = Math.floor(minFloat)
  const sec = ((minFloat - min) * 60).toFixed(2)
  const dir = axis === "lat" ? (decimal >= 0 ? "N" : "S") : (decimal >= 0 ? "E" : "W")
  return `${deg}°${min}'${sec}"${dir}`
}

export function formatTimestamp(isoOrLocale: string, format: string, timezone: string): string {
  const date = new Date(isoOrLocale)
  if (isNaN(date.getTime())) return isoOrLocale

  const tz = timezone || "UTC"

  if (format === "relative") {
    const diffMs = Date.now() - date.getTime()
    const diffMin = Math.floor(diffMs / 60000)
    if (diffMin < 1) return "just now"
    if (diffMin < 60) return `${diffMin}m ago`
    const diffHr = Math.floor(diffMin / 60)
    if (diffHr < 24) return `${diffHr}h ago`
    return `${Math.floor(diffHr / 24)}d ago`
  }

  try {
    if (format === "us") return date.toLocaleString("en-US", { timeZone: tz })
    if (format === "eu") return date.toLocaleString("en-GB", { timeZone: tz })
    return date.toLocaleString("sv-SE", { timeZone: tz })
  } catch {
    return date.toLocaleString()
  }
}
