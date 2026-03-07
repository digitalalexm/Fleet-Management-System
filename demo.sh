#!/bin/bash
# ============================================================
#  Fleet Management System — Full Demo
#  Requires: curl, jq
#  Usage: chmod +x demo.sh && ./demo.sh
# ============================================================

API="http://localhost:8080/api/v1"
SIM="http://localhost:8081/api/v1"
PEN="http://localhost:8082/api/v1"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

step() { echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"; echo -e "${YELLOW}▶ STEP $1: $2${NC}"; echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"; }
ok()   { echo -e "${GREEN}✔ $1${NC}"; }
info() { echo -e "  $1"; }

# ── Seed UUIDs (from init-db/01-init.sql) ────────────────────
CAR_1="a1000000-0000-0000-0000-000000000001"
CAR_2="a1000000-0000-0000-0000-000000000002"
TRUCK="a1000000-0000-0000-0000-000000000003"
SHIP="a1000000-0000-0000-0000-000000000005"
DRIVER_1="b2000000-0000-0000-0000-000000000001"
DRIVER_2="b2000000-0000-0000-0000-000000000002"
CAPTAIN="b2000000-0000-0000-0000-000000000004"
CONTRACT="c0000000-0000-0000-0000-000000000001"

echo -e "\n${GREEN}╔══════════════════════════════════════════════════╗"
echo -e "║     Fleet Management System — Full Demo          ║"
echo -e "╚══════════════════════════════════════════════════╝${NC}"

# ─────────────────────────────────────────────────────────────
step 1 "List all assets"
# ─────────────────────────────────────────────────────────────
ASSETS=$(curl -s "$API/assets")
echo "$ASSETS" | jq '[.[] | {id, assetType, internalName, status}]'
ok "Found $(echo "$ASSETS" | jq length) assets"

# ─────────────────────────────────────────────────────────────
step 2 "List land vehicles only"
# ─────────────────────────────────────────────────────────────
LAND=$(curl -s "$API/assets/land-vehicles")
echo "$LAND" | jq '[.[] | {id, assetType, internalName, licensePlate}]'
ok "Found $(echo "$LAND" | jq length) land vehicles"

# ─────────────────────────────────────────────────────────────
step 3 "List available operators"
# ─────────────────────────────────────────────────────────────
OPS=$(curl -s "$API/operators/available")
echo "$OPS" | jq '[.[] | {id, operatorType, firstName, lastName, status}]'
ok "Found $(echo "$OPS" | jq length) available operators"

# ─────────────────────────────────────────────────────────────
step 4 "Create a new operator (Driver)"
# ─────────────────────────────────────────────────────────────
NEW_OP=$(curl -s -X POST "$API/operators" \
  -H "Content-Type: application/json" \
  -d '{
    "operatorType": "DRIVER",
    "firstName": "Nikos",
    "lastName": "Papadopoulos",
    "employeeId": "EMP-099",
    "status": "AVAILABLE",
    "contactInfo": "{\"phone\":\"+30-210-0000001\",\"email\":\"nikos@fms.com\"}"
  }')
NEW_OP_ID=$(echo "$NEW_OP" | jq -r '.id')
echo "$NEW_OP" | jq '{id, operatorType, firstName, lastName, employeeId}'
ok "Created operator: $NEW_OP_ID"

# ─────────────────────────────────────────────────────────────
step 5 "Create a new CAR asset"
# ─────────────────────────────────────────────────────────────
NEW_CAR=$(curl -s -X POST "$API/assets" \
  -H "Content-Type: application/json" \
  -d '{
    "assetType": "CAR",
    "internalName": "FLEET-CAR-099",
    "manufacturer": "Volkswagen",
    "modelName": "Golf",
    "status": "ACTIVE",
    "purchaseDate": "2024-06-01",
    "licensePlate": "ZZZ-9999",
    "distanceCounter": 0,
    "fuelType": "DIESEL"
  }')
NEW_CAR_ID=$(echo "$NEW_CAR" | jq -r '.id')
echo "$NEW_CAR" | jq '{id, assetType, internalName, licensePlate, status}'
ok "Created car: $NEW_CAR_ID"

# ─────────────────────────────────────────────────────────────
step 6 "Assign Driver 1 to Car 1"
# ─────────────────────────────────────────────────────────────
ASSIGN=$(curl -s -X POST "$API/assignments" \
  -H "Content-Type: application/json" \
  -d "{
    \"assetId\": \"$CAR_1\",
    \"operatorId\": \"$DRIVER_1\",
    \"notes\": \"Regular daily assignment\"
  }")
ASSIGN_ID=$(echo "$ASSIGN" | jq -r '.id')
echo "$ASSIGN" | jq '{id, assetId, operatorId, assignedAt}'
ok "Assignment created: $ASSIGN_ID"

# ─────────────────────────────────────────────────────────────
step 7 "Schedule Trip 1 — Car 1 / Driver 1 (Athens → Piraeus)"
# ─────────────────────────────────────────────────────────────
TRIP1=$(curl -s -X POST "$API/trips" \
  -H "Content-Type: application/json" \
  -d "{
    \"assetId\": \"$CAR_1\",
    \"operatorId\": \"$DRIVER_1\",
    \"contractId\": \"$CONTRACT\",
    \"scheduledStart\": \"$(date -u +"%Y-%m-%dT%H:%M:%SZ")\",
    \"scheduledEnd\":   \"$(date -u -d '+2 hours' +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+2H +"%Y-%m-%dT%H:%M:%SZ")\",
    \"originName\": \"Athens\",
    \"destinationName\": \"Piraeus\"
  }")
TRIP1_ID=$(echo "$TRIP1" | jq -r '.id')
echo "$TRIP1" | jq '{id, status, originName, destinationName}'
ok "Trip 1 scheduled: $TRIP1_ID"

# ─────────────────────────────────────────────────────────────
step 8 "Schedule Trip 2 — Ship / Captain (Piraeus → Heraklion)"
# ─────────────────────────────────────────────────────────────
TRIP2=$(curl -s -X POST "$API/trips" \
  -H "Content-Type: application/json" \
  -d "{
    \"assetId\": \"$SHIP\",
    \"operatorId\": \"$CAPTAIN\",
    \"contractId\": \"$CONTRACT\",
    \"originName\": \"Piraeus\",
    \"destinationName\": \"Heraklion\"
  }")
TRIP2_ID=$(echo "$TRIP2" | jq -r '.id')
echo "$TRIP2" | jq '{id, status, originName, destinationName}'
ok "Trip 2 scheduled: $TRIP2_ID"

# ─────────────────────────────────────────────────────────────
step 9 "Start Trip 1 — triggers Kafka event → Simulator starts heartbeats"
# ─────────────────────────────────────────────────────────────
TRIP1_STARTED=$(curl -s -X POST "$API/trips/$TRIP1_ID/start")
echo "$TRIP1_STARTED" | jq '{id, status, actualStart}'
ok "Trip 1 is now EN_ROUTE — Simulator will start sending heartbeats"

# ─────────────────────────────────────────────────────────────
step 10 "Start Trip 2"
# ─────────────────────────────────────────────────────────────
TRIP2_STARTED=$(curl -s -X POST "$API/trips/$TRIP2_ID/start")
echo "$TRIP2_STARTED" | jq '{id, status, actualStart}'
ok "Trip 2 is now EN_ROUTE"

# ─────────────────────────────────────────────────────────────
step 11 "Check active simulator sessions"
# ─────────────────────────────────────────────────────────────
echo -e "  Waiting 6s for first heartbeats...\n"
sleep 6
SESSIONS=$(curl -s "$SIM/simulator/sessions")
echo "$SESSIONS" | jq '[.[] | {tripId, assetType, currentLat, currentLon}]'
COUNT=$(curl -s "$SIM/simulator/sessions/count" | jq '.activeSessions')
ok "$COUNT active simulation session(s)"

# ─────────────────────────────────────────────────────────────
step 12 "Check Driver 1 penalty score (after heartbeats)"
# ─────────────────────────────────────────────────────────────
echo -e "  Waiting 20s for penalty points to accumulate...\n"
sleep 20
PEN_D1=$(curl -s "$PEN/penalties/driver/$DRIVER_1")
echo "$PEN_D1" | jq '.'
RISK=$(echo "$PEN_D1" | jq -r '.riskLevel')
PTS=$(echo "$PEN_D1" | jq -r '.totalPenaltyPoints')
ok "Driver 1 has $PTS penalty points — Risk level: $RISK"

# ─────────────────────────────────────────────────────────────
step 13 "Check Captain penalty score"
# ─────────────────────────────────────────────────────────────
PEN_CAP=$(curl -s "$PEN/penalties/driver/$CAPTAIN")
echo "$PEN_CAP" | jq '.'
ok "Captain has $(echo "$PEN_CAP" | jq -r '.totalPenaltyPoints') penalty points — Risk: $(echo "$PEN_CAP" | jq -r '.riskLevel')"

# ─────────────────────────────────────────────────────────────
step 14 "Complete Trip 1"
# ─────────────────────────────────────────────────────────────
TRIP1_DONE=$(curl -s -X POST "$API/trips/$TRIP1_ID/complete")
echo "$TRIP1_DONE" | jq '{id, status, actualStart, actualEnd}'
ok "Trip 1 COMPLETED — simulator session removed"

# ─────────────────────────────────────────────────────────────
step 15 "Cancel Trip 2"
# ─────────────────────────────────────────────────────────────
TRIP2_CANCELLED=$(curl -s -X POST "$API/trips/$TRIP2_ID/cancel")
echo "$TRIP2_CANCELLED" | jq '{id, status}'
ok "Trip 2 CANCELLED"

# ─────────────────────────────────────────────────────────────
step 16 "Verify simulator sessions are now empty"
# ─────────────────────────────────────────────────────────────
sleep 3
FINAL_COUNT=$(curl -s "$SIM/simulator/sessions/count" | jq '.activeSessions')
echo "  Active sessions: $FINAL_COUNT"
ok "All sessions cleared"

# ─────────────────────────────────────────────────────────────
step 17 "Reset Driver 1 penalty points"
# ─────────────────────────────────────────────────────────────
RESET=$(curl -s -X DELETE "$PEN/penalties/driver/$DRIVER_1/reset")
echo "$RESET" | jq '.'
AFTER=$(curl -s "$PEN/penalties/driver/$DRIVER_1" | jq -r '.totalPenaltyPoints')
ok "Driver 1 points reset → now: $AFTER"

# ─────────────────────────────────────────────────────────────
step 18 "List all trips for Car 1"
# ─────────────────────────────────────────────────────────────
TRIPS=$(curl -s "$API/trips/asset/$CAR_1")
echo "$TRIPS" | jq '[.[] | {id, status, originName, destinationName}]'
ok "Car 1 has $(echo "$TRIPS" | jq length) trip(s) in history"

# ─────────────────────────────────────────────────────────────
step 19 "Release assignment of Driver 1 from Car 1"
# ─────────────────────────────────────────────────────────────
REL=$(curl -s -X DELETE "$API/assignments/$ASSIGN_ID/release")
info "Response: $(echo $REL | head -c 100)"
ok "Assignment released"

# ─────────────────────────────────────────────────────────────
step 20 "Delete the new car and operator created in this demo"
# ─────────────────────────────────────────────────────────────
curl -s -X DELETE "$API/assets/$NEW_CAR_ID" > /dev/null
curl -s -X DELETE "$API/operators/$NEW_OP_ID" > /dev/null
ok "Cleanup done — demo assets removed"

echo -e "\n${GREEN}╔══════════════════════════════════════════════════╗"
echo -e "║              Demo completed! ✔                   ║"
echo -e "╚══════════════════════════════════════════════════╝${NC}\n"
