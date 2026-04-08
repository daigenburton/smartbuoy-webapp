#!/bin/bash
# Injects live data points into InfluxDB every 5 seconds for buoy_id=1.
# Values drift slowly so the dashboard shows visible changes.
# Run: bash inject_data.sh
# Stop: Ctrl+C

TOKEN="EeHYIBNDFuDTsebL7DIoFfEtCia6cL1B5z1klT6UhBE"
ORG="smart-buoy"
BUCKET="device-data"
URL="http://localhost:8086"

STEP=0

while true; do
  # Temperature: 18–24°C oscillating
  TEMP=$(echo "$STEP" | awk '{printf "%.2f", 21 + 3 * sin($1 * 0.3)}')
  # Pressure: 1010–1015 hPa oscillating
  PRESSURE=$(echo "$STEP" | awk '{printf "%.2f", 1012.5 + 2.5 * cos($1 * 0.2)}')
  # Latitude: slow northward drift ~Boston Harbor
  LAT=$(echo "$STEP" | awk '{printf "%.5f", 42.3601 + $1 * 0.0002}')
  # Longitude: slow westward drift
  LON=$(echo "$STEP" | awk '{printf "%.5f", -71.0589 - $1 * 0.0001}')

  TIMESTAMP=$(date +%s%N)

  PAYLOAD="buoy_data,buoy_id=1 temperature=${TEMP},pressure=${PRESSURE},latitude=${LAT},longitude=${LON} ${TIMESTAMP}"

  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "${URL}/api/v2/write?org=${ORG}&bucket=${BUCKET}&precision=ns" \
    -H "Authorization: Token ${TOKEN}" \
    -H "Content-Type: text/plain; charset=utf-8" \
    --data-raw "${PAYLOAD}")

  echo "[step $STEP | $(date +%H:%M:%S)] temp=${TEMP}°C  pressure=${PRESSURE}hPa  lat=${LAT}  lon=${LON}  → HTTP $HTTP_STATUS"

  STEP=$((STEP + 1))
  sleep 5
done
