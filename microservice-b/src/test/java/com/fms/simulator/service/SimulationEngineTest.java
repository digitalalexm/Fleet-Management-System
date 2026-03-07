package com.fms.simulator.service;

import com.fms.simulator.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SimulationEngineTest {

    SimulationEngine engine;

    @BeforeEach 
    void setUp() {
        engine = new SimulationEngine();
        engine.latMin = 37.95; engine.latMax = 38.05;
        engine.lonMin = 23.70; engine.lonMax = 23.80;
    }

    SimulationSession session(AssetType type) {
        return SimulationSession.start(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                type, 38.0, 23.75);
    }

    @Test 
    void heartbeatNotNull() {
        assertNotNull(engine.nextHeartbeat(session(AssetType.CAR)));
    }

    @Test 
    void heartbeatAssetTypePreserved() {
        assertEquals(AssetType.TRUCK, engine.nextHeartbeat(session(AssetType.TRUCK)).assetType());
    }

    @Test 
    void heartbeatSpeedPositive() {
        assertTrue(engine.nextHeartbeat(session(AssetType.CAR)).speedKmh() > 0);
    }

    @Test 
    void speedKnotsConversion() {
        var hb = engine.nextHeartbeat(session(AssetType.CAR));
        assertEquals(hb.speedKmh() / 1.852, hb.speedKnots(), 0.001);
    }

    @Test 
    void advanceUpdatesPosition() {
        var s  = session(AssetType.CAR);
        var hb = engine.nextHeartbeat(s);
        var u  = engine.advance(s, hb);
        assertEquals(hb.latitude(),  u.currentLat());
        assertEquals(hb.longitude(), u.currentLon());
    }

    @Test 
    void positionStaysInBounds() {
        var s = session(AssetType.CAR);
        for (int i = 0; i < 50; i++) {
            var hb = engine.nextHeartbeat(s);
            assertTrue(hb.latitude()  >= 37.95 && hb.latitude()  <= 38.05);
            assertTrue(hb.longitude() >= 23.70 && hb.longitude() <= 23.80);
            s = engine.advance(s, hb);
        }
    }

    // All enum values produce a positive speed
    @ParameterizedTest 
    @EnumSource(AssetType.class)
    void allTypesPositiveSpeed(AssetType type) {
        assertTrue(engine.generateSpeed(type) > 0);
    }

    @RepeatedTest(30) 
    void carSpeedInRange() {
        double s = engine.generateSpeed(AssetType.CAR);
        assertTrue(s >= 10 && s <= 120, "CAR speed out of range: " + s);
    }

    @RepeatedTest(30) 
    void truckSpeedInRange() {
        double s = engine.generateSpeed(AssetType.TRUCK);
        assertTrue(s >= 20 && s <= 90, "TRUCK speed out of range: " + s);
    }

    @RepeatedTest(30) 
    void shipSpeedInRange() {
        double s = engine.generateSpeed(AssetType.SHIP);
        assertTrue(s >= 10 && s <= 40, "SHIP speed out of range: " + s);
    }

    @RepeatedTest(30) 
    void aircraftSpeedInRange() {
        double s = engine.generateSpeed(AssetType.AIRCRAFT);
        // Using a range consistent with aircraft operations (e.g., 150-600 km/h)
        assertTrue(s >= 150 && s <= 600, "AIRCRAFT speed out of range: " + s);
    }
}
