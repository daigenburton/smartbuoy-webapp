import { NextResponse } from "next/server"
import { resolveAuth } from "@/lib/resolveAuth"
import { prisma } from "@/lib/prisma"
import { logAudit } from "@/lib/audit"

const VALID_THEMES = ["light", "dark", "system"]
const VALID_FONT_SIZES = ["small", "default", "large", "xl"]
const VALID_TEMPERATURE_UNITS = ["celsius", "fahrenheit"]
const VALID_PRESSURE_UNITS = ["hpa", "psi", "atm"]
const VALID_COORDINATE_FORMATS = ["decimal", "dms"]
const VALID_DATE_FORMATS = ["iso", "us", "eu"]
const VALID_CHART_TIME_RANGES = ["15m", "1h", "6h", "24h", "7d"]

export async function GET(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  try {
    const preference = await prisma.userPreference.upsert({
      where: { userId: auth.userId },
      update: {},
      create: { userId: auth.userId },
    })
    return NextResponse.json(preference)
  } catch (err) {
    console.error("[preferences GET]", err)
    return NextResponse.json({ error: String(err) }, { status: 500 })
  }
}

export async function PUT(req: Request) {
  const auth = await resolveAuth(req)
  if (!auth) return NextResponse.json({ error: "Unauthorized" }, { status: 401 })

  const body = await req.json()

  const allowed: Record<string, unknown> = {}

  if (body.theme !== undefined) {
    if (!VALID_THEMES.includes(body.theme)) {
      return NextResponse.json({ error: "Invalid theme" }, { status: 400 })
    }
    allowed.theme = body.theme
  }
  if (body.fontSize !== undefined) {
    if (!VALID_FONT_SIZES.includes(body.fontSize)) {
      return NextResponse.json({ error: "Invalid fontSize" }, { status: 400 })
    }
    allowed.fontSize = body.fontSize
  }
  if (body.colorblindMode !== undefined) {
    allowed.colorblindMode = Boolean(body.colorblindMode)
  }
  if (body.temperatureUnit !== undefined) {
    if (!VALID_TEMPERATURE_UNITS.includes(body.temperatureUnit)) {
      return NextResponse.json({ error: "Invalid temperatureUnit" }, { status: 400 })
    }
    allowed.temperatureUnit = body.temperatureUnit
  }
  if (body.pressureUnit !== undefined) {
    if (!VALID_PRESSURE_UNITS.includes(body.pressureUnit)) {
      return NextResponse.json({ error: "Invalid pressureUnit" }, { status: 400 })
    }
    allowed.pressureUnit = body.pressureUnit
  }
  if (body.coordinateFormat !== undefined) {
    if (!VALID_COORDINATE_FORMATS.includes(body.coordinateFormat)) {
      return NextResponse.json({ error: "Invalid coordinateFormat" }, { status: 400 })
    }
    allowed.coordinateFormat = body.coordinateFormat
  }
  if (body.timezone !== undefined) {
    allowed.timezone = String(body.timezone)
  }
  if (body.dateFormat !== undefined) {
    if (!VALID_DATE_FORMATS.includes(body.dateFormat)) {
      return NextResponse.json({ error: "Invalid dateFormat" }, { status: 400 })
    }
    allowed.dateFormat = body.dateFormat
  }
  if (body.chartTimeRange !== undefined) {
    if (!VALID_CHART_TIME_RANGES.includes(body.chartTimeRange)) {
      return NextResponse.json({ error: "Invalid chartTimeRange" }, { status: 400 })
    }
    allowed.chartTimeRange = body.chartTimeRange
  }

  const preference = await prisma.userPreference.upsert({
    where: { userId: auth.userId },
    update: allowed,
    create: { userId: auth.userId, ...allowed },
  })

  await logAudit(auth.userId, "preferences_updated", JSON.stringify(allowed))

  return NextResponse.json(preference)
}
