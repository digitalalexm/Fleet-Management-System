package com.fms.penalty.strategy;

import com.fms.penalty.model.AssetType;
import com.fms.penalty.model.HeartbeatEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PenaltyStrategyTest {

    CarPenaltyStrategy      car;
    TruckPenaltyStrategy    truck;
    AircraftPenaltyStrategy aircraft;
    ShipPenaltyStrategy     ship;
    DefaultPenaltyStrategy  def;

    @BeforeEach
    void setUp() {
        car = new CarPenaltyStrategy();
        car.severeSpeed = 80.0; car.severePoints = 5;
        car.mildSpeed   = 60.0; car.mildPoints   = 2;
        car.init();

        truck = new TruckPenaltyStrategy();
        truck.severeSpeed = 70.0; truck.severePoints = 8;
        truck.mildSpeed   = 50.0; truck.mildPoints   = 3;
        truck.init();

        aircraft = new AircraftPenaltyStrategy();
        aircraft.severeKnots = 250.0; aircraft.severePoints = 10;
        aircraft.mildKnots   = 200.0; aircraft.mildPoints   = 4;
        aircraft.init();

        ship = new ShipPenaltyStrategy();
        ship.severeSpeed = 30.0; ship.severePoints = 6;
        ship.mildSpeed   = 20.0; ship.mildPoints   = 2;
        ship.init();

        def = new DefaultPenaltyStrategy();
    }

    private HeartbeatEvent ev(AssetType type, double speed) {
        return new HeartbeatEvent(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(),
                type, 38.0, 23.75, speed, 0, 2000, 80, OffsetDateTime.now());
    }

    // ── CAR ──────────────────────────────────────────────────────────
    @Test 
    void carNoPointsAt50() {
        assertEquals(0, car.calculate(ev(AssetType.CAR, 50))); 
    }
    
    @Test 
    void carNoPointsExactly60() { 
        assertEquals(0, car.calculate(ev(AssetType.CAR, 60))); 
    }
    
    @Test 
    void carPointsAbove60() {
        assertTrue(car.calculate(ev(AssetType.CAR, 70)) >= 1); 
    }

    @Test 
    void carAssetType() {
        assertEquals(AssetType.CAR, car.assetType()); 
    }

    // ── TRUCK ────────────────────────────────────────────────────────
    @Test 
    void truckNoPointsAt40() { 
        assertEquals(0, truck.calculate(ev(AssetType.TRUCK, 40))); 
    }

    @Test 
    void truckNoPointsExactly50() { 
        assertEquals(0, truck.calculate(ev(AssetType.TRUCK, 50))); 
    }

    @Test 
    void truckPointsAbove50() { 
        assertTrue(truck.calculate(ev(AssetType.TRUCK, 55)) >= 1); 
    }

    @Test 
    void truckStricterThanCar() { 
        assertTrue(truck.calculate(ev(AssetType.TRUCK, 65)) >= car.calculate(ev(AssetType.CAR, 65))); 
    }

    @Test 
    void truckAssetType() { 
        assertEquals(AssetType.TRUCK, truck.assetType()); 
    }

    // ── AIRCRAFT ─────────────────────────────────────────────────────
    @Test 
    void aircraftNoPointsBelow200kt() { 
        assertEquals(0, aircraft.calculate(ev(AssetType.AIRCRAFT, 190 * 1.852))); 
    }

    @Test 
    void aircraftPointsAbove200kt() { 
        assertTrue(aircraft.calculate(ev(AssetType.AIRCRAFT, 210 * 1.852)) >= 1); 
    }

    @Test 
    void aircraftAssetType() { 
        assertEquals(AssetType.AIRCRAFT, aircraft.assetType()); 
    }

    // ── SHIP ─────────────────────────────────────────────────────────
    @Test 
    void shipNoPointsAt15() { 
        assertEquals(0, ship.calculate(ev(AssetType.SHIP, 15))); 
    }

    @Test 
    void shipNoPointsExactly20() { 
        assertEquals(0, ship.calculate(ev(AssetType.SHIP, 20))); 
    }

    @Test 
    void shipPointsAbove20() { 
        assertTrue(ship.calculate(ev(AssetType.SHIP, 25)) >= 1); 
    }

    @Test 
    void shipAssetType() { 
        assertEquals(AssetType.SHIP, ship.assetType()); 
    }

    // ── DEFAULT fallback ─────────────────────────────────────────────
    @Test 
    void defaultNoPointsBelow60() { 
        assertEquals(0, def.calculate(ev(AssetType.CAR, 55))); 
    }

    @Test 
    void defaultPointsAbove60() { 
        assertTrue(def.calculate(ev(AssetType.CAR, 70)) >= 1); 
    }

    // ── describe() ───────────────────────────────────────────────────
    @Test 
    void describeContainsSpeed() { 
        assertTrue(car.describe(75, 3).contains("75")); 
    }

    @Test 
    void describeNoInfraction() { 
        assertTrue(car.describe(50, 0).contains("No infraction")); 
    }

    @Test 
    void describeSevere() { 
        assertTrue(car.describe(90, 5).toLowerCase().contains("severe")); 
    }

    // ── Never negative ───────────────────────────────────────────────
    @ParameterizedTest 
    @ValueSource(doubles = {0, 50, 60, 80, 100, 150})
    void carNeverNegative(double s) { 
        assertTrue(car.calculate(ev(AssetType.CAR, s)) >= 0); 
    }

    @ParameterizedTest 
    @ValueSource(doubles = {0, 40, 50, 70, 90})
    void truckNeverNegative(double s) { 
        assertTrue(truck.calculate(ev(AssetType.TRUCK, s)) >= 0); 
    }
}
