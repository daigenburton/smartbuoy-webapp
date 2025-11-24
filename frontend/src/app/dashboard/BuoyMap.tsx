"use client"

import type { LatLngExpression } from "leaflet"
import React from "react"
import {
  MapContainer as RLMapContainer,
  TileLayer as RLTileLayer,
  CircleMarker as RLCircleMarker,
  Polyline as RLPolyline,
} from "react-leaflet"

type Location = {
  latitude: number
  longitude: number
}

type BuoyMapProps = {
  firstLocation: Location | null
  currentLocation: Location
}

// Relax the TS typing for React-Leaflet components
const MapContainer = RLMapContainer as unknown as React.ComponentType<any>
const TileLayer = RLTileLayer as unknown as React.ComponentType<any>
const CircleMarker = RLCircleMarker as unknown as React.ComponentType<any>
const Polyline = RLPolyline as unknown as React.ComponentType<any>

export default function BuoyMap({
  firstLocation,
  currentLocation,
}: BuoyMapProps) {
  if (!firstLocation) {
    return (
      <div className="flex-1 rounded-lg border border-gray-200 bg-slate-50 flex items-center justify-center text-xs text-gray-400">
        Waiting for first location…
      </div>
    )
  }

  const center: LatLngExpression = [
    currentLocation.latitude,
    currentLocation.longitude,
  ]

  const firstPos: LatLngExpression = [
    firstLocation.latitude,
    firstLocation.longitude,
  ]

  return (
    <div className="flex-1 rounded-lg overflow-hidden border border-gray-200">
      <MapContainer
        center={center}
        zoom={14}
        scrollWheelZoom={false}
        className="h-40 w-full"
      >
        <TileLayer
          attribution="&copy; OpenStreetMap contributors"
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* First position (gray) */}
        <CircleMarker
          center={firstPos}
          radius={6}
          pathOptions={{
            color: "#6b7280",
            fillColor: "#9ca3af",
            fillOpacity: 0.9,
          }}
        />

        {/* Current position (green) */}
        <CircleMarker
          center={center}
          radius={8}
          pathOptions={{
            color: "#10b981",
            fillColor: "#34d399",
            fillOpacity: 0.9,
          }}
        />

        {/* Line between first and current */}
        <Polyline
          positions={[firstPos, center]}
          pathOptions={{ color: "#10b981", dashArray: "4 4" }}
        />
      </MapContainer>
    </div>
  )
}
