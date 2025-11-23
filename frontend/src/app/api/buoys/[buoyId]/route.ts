import { NextResponse } from "next/server"

type CombinedBuoyResponse = {
  temperatureF: number
  pressureHpa: number
  latitude: number
  longitude: number
  timestamp: number
}

const BUOY_NUMERIC_ID: Record<string, number> = {
  "buoy-1": 1,
  "buoy-2": 2,
}

export async function GET(
  _req: Request,
  context: { params: Promise<{ buoyId: string }> },
) {
  const { buoyId } = await context.params

  const backendBase = process.env.BACKEND_API_BASE_URL || "http://localhost:8000"
  const buoyKey = buoyId
  const numericId = BUOY_NUMERIC_ID[buoyKey]

  if (!numericId) {
    return NextResponse.json(
      { error: `Unknown buoy id: ${buoyKey}` },
      { status: 400 },
    )
  }

  try {
    const tempUrl = `${backendBase}/temp/${numericId}`
    const pressureUrl = `${backendBase}/pressure/${numericId}`
    const locationUrl = `${backendBase}/location/${numericId}`

    const [tempRes, pressureRes, locationRes] = await Promise.all([
      fetch(tempUrl),
      fetch(pressureUrl),
      fetch(locationUrl),
    ])

    if (!tempRes.ok || !pressureRes.ok || !locationRes.ok) {
      return NextResponse.json(
        {
          error: "Backend returned non-OK",
          statuses: {
            temp: tempRes.status,
            pressure: pressureRes.status,
            location: locationRes.status,
          },
        },
        { status: 502 },
      )
    }

    const tempJson = await tempRes.json()
    const pressureJson = await pressureRes.json()
    const locationJson = await locationRes.json()

    const combined: CombinedBuoyResponse = {
      temperatureF: tempJson.value,
      pressureHpa: pressureJson.value,
      latitude: locationJson.latitude,
      longitude: locationJson.longitude,
      timestamp: locationJson.timestamp,
    }

    return NextResponse.json(combined, { status: 200 })
  } catch (err) {
    console.error("Error talking to backend:", err)
    return NextResponse.json(
      { error: "Failed to reach backend", details: String(err) },
      { status: 500 },
    )
  }
}
