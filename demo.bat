@echo off
:: ============================================================
::  Fleet Management System — Full Demo
::  Requires: curl only (built-in Windows 10+)
::  Usage: demo.bat
:: ============================================================

set API=http://localhost:8080/api/v1
set SIM=http://localhost:8081/api/v1
set PEN=http://localhost:8082/api/v1

set CAR_1=a1000000-0000-0000-0000-000000000001
set SHIP=a1000000-0000-0000-0000-000000000005
set DRIVER_1=b2000000-0000-0000-0000-000000000001
set CAPTAIN=b2000000-0000-0000-0000-000000000004
set CONTRACT=c0000000-0000-0000-0000-000000000001

echo.
echo ====================================================
echo      Fleet Management System - Full Demo
echo ====================================================

:: ── STEP 1 ───────────────────────────────────────────
echo.
echo [STEP 1] List all assets
echo ----------------------------------------------------
curl -s "%API%/assets"
echo.

:: ── STEP 2 ───────────────────────────────────────────
echo.
echo [STEP 2] List land vehicles only
echo ----------------------------------------------------
curl -s "%API%/assets/land-vehicles"
echo.

:: ── STEP 3 ───────────────────────────────────────────
echo.
echo [STEP 3] List available operators
echo ----------------------------------------------------
curl -s "%API%/operators/available"
echo.

:: ── STEP 4 ───────────────────────────────────────────
echo.
echo [STEP 4] Create a new operator (Driver)
echo ----------------------------------------------------
curl -s -X POST "%API%/operators" ^
  -H "Content-Type: application/json" ^
  -d "{\"operatorType\":\"DRIVER\",\"firstName\":\"Nikos\",\"lastName\":\"Papadopoulos\",\"employeeId\":\"EMP-099\",\"status\":\"AVAILABLE\",\"contactInfo\":\"{\\\"phone\\\":\\\"+30-210-0000001\\\",\\\"email\\\":\\\"nikos@fms.com\\\"}\"}" ^
  > tmp_op.json
type tmp_op.json
echo.
:: Extract the operator id using PowerShell
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content tmp_op.json | ConvertFrom-Json).id"') do set NEW_OP_ID=%%i
echo Created operator ID: %NEW_OP_ID%

:: ── STEP 5 ───────────────────────────────────────────
echo.
echo [STEP 5] Create a new CAR asset
echo ----------------------------------------------------
curl -s -X POST "%API%/assets" ^
  -H "Content-Type: application/json" ^
  -d "{\"assetType\":\"CAR\",\"internalName\":\"FLEET-CAR-099\",\"manufacturer\":\"Volkswagen\",\"modelName\":\"Golf\",\"status\":\"ACTIVE\",\"purchaseDate\":\"2024-06-01\",\"licensePlate\":\"ZZZ-9999\",\"distanceCounter\":0,\"fuelType\":\"DIESEL\"}" ^
  > tmp_car.json
type tmp_car.json
echo.
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content tmp_car.json | ConvertFrom-Json).id"') do set NEW_CAR_ID=%%i
echo Created car ID: %NEW_CAR_ID%

:: ── STEP 6 ───────────────────────────────────────────
echo.
echo [STEP 6] Assign Driver 1 to Car 1
echo ----------------------------------------------------
curl -s -X POST "%API%/assignments" ^
  -H "Content-Type: application/json" ^
  -d "{\"assetId\":\"%CAR_1%\",\"operatorId\":\"%DRIVER_1%\",\"notes\":\"Regular daily assignment\"}" ^
  > tmp_assign.json
type tmp_assign.json
echo.
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content tmp_assign.json | ConvertFrom-Json).id"') do set ASSIGN_ID=%%i
echo Assignment ID: %ASSIGN_ID%

:: ── STEP 7 ───────────────────────────────────────────
echo.
echo [STEP 7] Schedule Trip 1 - Car 1 / Driver 1 (Athens to Piraeus)
echo ----------------------------------------------------
curl -s -X POST "%API%/trips" ^
  -H "Content-Type: application/json" ^
  -d "{\"assetId\":\"%CAR_1%\",\"operatorId\":\"%DRIVER_1%\",\"contractId\":\"%CONTRACT%\",\"originName\":\"Athens\",\"destinationName\":\"Piraeus\"}" ^
  > tmp_trip1.json
type tmp_trip1.json
echo.
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content tmp_trip1.json | ConvertFrom-Json).id"') do set TRIP1_ID=%%i
echo Trip 1 ID: %TRIP1_ID%

:: ── STEP 8 ───────────────────────────────────────────
echo.
echo [STEP 8] Schedule Trip 2 - Ship / Captain (Piraeus to Heraklion)
echo ----------------------------------------------------
curl -s -X POST "%API%/trips" ^
  -H "Content-Type: application/json" ^
  -d "{\"assetId\":\"%SHIP%\",\"operatorId\":\"%CAPTAIN%\",\"contractId\":\"%CONTRACT%\",\"originName\":\"Piraeus\",\"destinationName\":\"Heraklion\"}" ^
  > tmp_trip2.json
type tmp_trip2.json
echo.
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content tmp_trip2.json | ConvertFrom-Json).id"') do set TRIP2_ID=%%i
echo Trip 2 ID: %TRIP2_ID%

:: ── STEP 9 ───────────────────────────────────────────
echo.
echo [STEP 9] Start Trip 1 - Kafka event fires, Simulator begins heartbeats
echo ----------------------------------------------------
curl -s -X POST "%API%/trips/%TRIP1_ID%/start"
echo.
echo Trip 1 is now EN_ROUTE

:: ── STEP 10 ──────────────────────────────────────────
echo.
echo [STEP 10] Start Trip 2
echo ----------------------------------------------------
curl -s -X POST "%API%/trips/%TRIP2_ID%/start"
echo.
echo Trip 2 is now EN_ROUTE

:: ── STEP 11 ──────────────────────────────────────────
echo.
echo [STEP 11] Check active simulator sessions
echo ----------------------------------------------------
echo Waiting 6s for first heartbeats...
timeout /t 6 /nobreak > nul
curl -s "%SIM%/simulator/sessions"
echo.
curl -s "%SIM%/simulator/sessions/count"
echo.

:: ── STEP 12 ──────────────────────────────────────────
echo.
echo [STEP 12] Check Driver 1 penalty score
echo ----------------------------------------------------
echo Waiting 20s for penalty points to accumulate...
timeout /t 20 /nobreak > nul
curl -s "%PEN%/penalties/driver/%DRIVER_1%"
echo.

:: ── STEP 13 ──────────────────────────────────────────
echo.
echo [STEP 13] Check Captain penalty score
echo ----------------------------------------------------
curl -s "%PEN%/penalties/driver/%CAPTAIN%"
echo.

:: ── STEP 14 ──────────────────────────────────────────
echo.
echo [STEP 14] Complete Trip 1
echo ----------------------------------------------------
curl -s -X POST "%API%/trips/%TRIP1_ID%/complete"
echo.
echo Trip 1 COMPLETED

:: ── STEP 15 ──────────────────────────────────────────
echo.
echo [STEP 15] Cancel Trip 2
echo ----------------------------------------------------
curl -s -X POST "%API%/trips/%TRIP2_ID%/cancel"
echo.
echo Trip 2 CANCELLED

:: ── STEP 16 ──────────────────────────────────────────
echo.
echo [STEP 16] Verify simulator sessions cleared
echo ----------------------------------------------------
timeout /t 3 /nobreak > nul
curl -s "%SIM%/simulator/sessions/count"
echo.

:: ── STEP 17 ──────────────────────────────────────────
echo.
echo [STEP 17] Reset Driver 1 penalty points
echo ----------------------------------------------------
curl -s -X DELETE "%PEN%/penalties/driver/%DRIVER_1%/reset"
echo.
curl -s "%PEN%/penalties/driver/%DRIVER_1%"
echo.

:: ── STEP 18 ──────────────────────────────────────────
echo.
echo [STEP 18] List all trips for Car 1
echo ----------------------------------------------------
curl -s "%API%/trips/asset/%CAR_1%"
echo.

:: ── STEP 19 ──────────────────────────────────────────
echo.
echo [STEP 19] Release assignment of Driver 1 from Car 1
echo ----------------------------------------------------
curl -s -X DELETE "%API%/assignments/%ASSIGN_ID%/release"
echo.
echo Assignment released

:: ── STEP 20 ──────────────────────────────────────────
echo.
echo [STEP 20] Cleanup - delete demo car and operator
echo ----------------------------------------------------
curl -s -X DELETE "%API%/assets/%NEW_CAR_ID%"
curl -s -X DELETE "%API%/operators/%NEW_OP_ID%"
echo Demo assets removed

:: ── Cleanup temp files ────────────────────────────────
del /q tmp_op.json tmp_car.json tmp_assign.json tmp_trip1.json tmp_trip2.json 2>nul

echo.
echo ====================================================
echo          Demo completed successfully!
echo ====================================================
echo.
