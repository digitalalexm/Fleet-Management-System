package com.fms.fleet.service;

import com.fms.fleet.exception.*;
import com.fms.fleet.kafka.TripEventPublisher;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.entity.*;
import com.fms.fleet.model.mapper.DtoMapper;
import com.fms.fleet.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {
    @Mock TripRepository tripRepo;
    @Mock AssetRepository assetRepo;
    @Mock OperatorRepository operatorRepo;
    @Mock TripEventPublisher publisher;
    @Spy  DtoMapper mapper;
    @InjectMocks TripService service;

    Asset activeAsset;
    Operator availableOp;
    Trip scheduledTrip;

    @BeforeEach void setUp() {
        UUID assetId=UUID.randomUUID(), opId=UUID.randomUUID(), tripId=UUID.randomUUID();
        activeAsset=new Asset(assetId,AssetType.CAR,"CAR-001","Toyota","Corolla","ACTIVE",null,OffsetDateTime.now(),"ABC",BigDecimal.ZERO,"PETROL",null,null,null,null,null);
        availableOp=new Operator(opId,"DRIVER","John","Doe","EMP-001","AVAILABLE","{}",OffsetDateTime.now());
        scheduledTrip=new Trip(tripId,assetId,opId,UUID.randomUUID(),OffsetDateTime.now(),OffsetDateTime.now().plusHours(2),null,null,"Athens","Piraeus","SCHEDULED",null,null,OffsetDateTime.now());
    }

    @Test void scheduleSuccessfulTrip() {
        when(assetRepo.findById(activeAsset.id())).thenReturn(Optional.of(activeAsset));
        when(operatorRepo.findById(availableOp.id())).thenReturn(Optional.of(availableOp));
        when(tripRepo.create(any())).thenReturn(scheduledTrip);
        TripRequest req=new TripRequest(activeAsset.id(),availableOp.id(),UUID.randomUUID(),OffsetDateTime.now(),OffsetDateTime.now().plusHours(2),"Athens","Piraeus");
        TripResponse res=service.schedule(req);
        assertNotNull(res);
        assertEquals("SCHEDULED",res.status());
        verify(publisher).publish(any(),any(),eq("TRIP_SCHEDULED"));
    }

    @Test void scheduleThrowsWhenAssetNotFound() {
        when(assetRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->service.schedule(new TripRequest(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),null,null,"A","B")));
    }

    @Test void scheduleThrowsWhenOperatorBusy() {
        Operator busyOp=new Operator(availableOp.id(),"DRIVER","John","Doe","EMP-001","ON_TRIP","{}",OffsetDateTime.now());
        when(assetRepo.findById(activeAsset.id())).thenReturn(Optional.of(activeAsset));
        when(operatorRepo.findById(busyOp.id())).thenReturn(Optional.of(busyOp));
        assertThrows(BusinessRuleException.class,()->service.schedule(new TripRequest(activeAsset.id(),busyOp.id(),UUID.randomUUID(),null,null,"A","B")));
    }

    @Test void startTransitionsToEnRoute() {
        Trip enRoute=new Trip(scheduledTrip.id(),scheduledTrip.assetId(),scheduledTrip.operatorId(),scheduledTrip.contractId(),null,null,OffsetDateTime.now(),null,"Athens","Piraeus","EN_ROUTE",null,null,OffsetDateTime.now());
        when(tripRepo.findById(scheduledTrip.id())).thenReturn(Optional.of(scheduledTrip));
        when(assetRepo.findById(scheduledTrip.assetId())).thenReturn(Optional.of(activeAsset));
        when(tripRepo.updateStatus(eq(scheduledTrip.id()),eq("EN_ROUTE"),any(),isNull())).thenReturn(Optional.of(enRoute));
        TripResponse res=service.start(scheduledTrip.id());
        assertEquals("EN_ROUTE",res.status());
        verify(operatorRepo).updateStatus(scheduledTrip.operatorId(),"ON_TRIP");
    }

    @Test void completeFreesOperator() {
        Trip enRoute=new Trip(scheduledTrip.id(),scheduledTrip.assetId(),scheduledTrip.operatorId(),scheduledTrip.contractId(),null,null,OffsetDateTime.now().minusHours(1),null,"A","B","EN_ROUTE",null,null,OffsetDateTime.now());
        Trip completed=new Trip(enRoute.id(),enRoute.assetId(),enRoute.operatorId(),enRoute.contractId(),null,null,enRoute.actualStart(),OffsetDateTime.now(),"A","B","COMPLETED",BigDecimal.TEN,BigDecimal.ONE,enRoute.createdAt());
        when(tripRepo.findById(enRoute.id())).thenReturn(Optional.of(enRoute));
        when(assetRepo.findById(enRoute.assetId())).thenReturn(Optional.of(activeAsset));
        when(tripRepo.updateStatus(eq(enRoute.id()),eq("COMPLETED"),isNull(),any())).thenReturn(Optional.of(completed));
        TripResponse res=service.complete(enRoute.id());
        assertEquals("COMPLETED",res.status());
        verify(operatorRepo).updateStatus(enRoute.operatorId(),"AVAILABLE");
    }
}
