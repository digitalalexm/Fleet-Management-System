package com.fms.penalty.service;

import com.fms.penalty.model.*;
import com.fms.penalty.repository.PenaltyStore;
import com.fms.penalty.strategy.PenaltyStrategy;
import com.fms.penalty.strategy.PenaltyStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests PenaltyService in isolation.
 * Registry and Store are mocked — no access to strategy internals needed.
 */
@ExtendWith(MockitoExtension.class)
class PenaltyServiceTest {

    @Mock PenaltyStrategyRegistry registry;
    @Mock PenaltyStore            store;
    @Mock PenaltyStrategy         mockStrategy;
    @InjectMocks PenaltyService   service;

    private HeartbeatEvent ev(AssetType type, double speed) {
        return new HeartbeatEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                type, 38.0, 23.75, speed, 0, 2000, 80.0, OffsetDateTime.now());
    }

    @Test
    void noInfractionReturnsEmpty() {
        when(registry.resolve(AssetType.CAR)).thenReturn(mockStrategy);
        when(mockStrategy.calculate(any())).thenReturn(0);

        assertTrue(service.evaluate(ev(AssetType.CAR, 45)).isEmpty());
        verifyNoInteractions(store);
    }

    @Test
    void infractionSavesPointsAndReturnsEvent() {
        when(registry.resolve(AssetType.CAR)).thenReturn(mockStrategy);
        when(mockStrategy.calculate(any())).thenReturn(3);
        when(mockStrategy.describe(anyDouble(), eq(3))).thenReturn("Speeding [CAR]: 70.0 km/h. +3 pts");
        when(store.addPoints(any(), eq(3))).thenReturn(8L);

        Optional<PenaltyEvent> result = service.evaluate(ev(AssetType.CAR, 70));

        assertTrue(result.isPresent());
        verify(store).addPoints(any(), eq(3));
    }

    @Test
    void returnsCorrectOperatorIdAndTotal() {
        when(registry.resolve(AssetType.CAR)).thenReturn(mockStrategy);
        when(mockStrategy.calculate(any())).thenReturn(5);
        when(mockStrategy.describe(anyDouble(), eq(5))).thenReturn("Severe speeding [CAR]: 90.0 km/h. +5 pts");
        when(store.addPoints(any(), eq(5))).thenReturn(10L);

        HeartbeatEvent e = ev(AssetType.CAR, 90);
        PenaltyEvent   p = service.evaluate(e).orElseThrow();

        assertEquals(e.operatorId(), p.operatorId());
        assertEquals(10L,            p.totalPoints());
        assertEquals(AssetType.CAR,  p.assetType());
    }

    @Test
    void reasonNotBlank() {
        when(registry.resolve(AssetType.CAR)).thenReturn(mockStrategy);
        when(mockStrategy.calculate(any())).thenReturn(2);
        when(mockStrategy.describe(anyDouble(), eq(2))).thenReturn("Speeding [CAR]: 65.0 km/h. +2 pts");
        when(store.addPoints(any(), eq(2))).thenReturn(2L);

        assertFalse(service.evaluate(ev(AssetType.CAR, 65)).orElseThrow().reason().isBlank());
    }

    @Test
    void delegatesToCorrectStrategyForEachType() {
        when(registry.resolve(any(AssetType.class))).thenReturn(mockStrategy);
        when(mockStrategy.calculate(any())).thenReturn(4);
        when(mockStrategy.describe(anyDouble(), eq(4))).thenReturn("reason");
        when(store.addPoints(any(), eq(4))).thenReturn(4L);

        service.evaluate(ev(AssetType.TRUCK,    60));
        service.evaluate(ev(AssetType.AIRCRAFT, 500));
        service.evaluate(ev(AssetType.SHIP,     25));

        verify(registry).resolve(AssetType.TRUCK);
        verify(registry).resolve(AssetType.AIRCRAFT);
        verify(registry).resolve(AssetType.SHIP);
    }

    @Test
    void getPointsDelegates() {
        UUID id = UUID.randomUUID();
        when(store.getPoints(id)).thenReturn(42L);
        assertEquals(42L, service.getPoints(id));
    }

    @Test
    void resetPointsDelegates() {
        UUID id = UUID.randomUUID();
        service.resetPoints(id);
        verify(store).resetPoints(id);
    }
}

