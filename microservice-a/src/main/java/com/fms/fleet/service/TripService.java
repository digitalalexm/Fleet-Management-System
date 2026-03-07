package com.fms.fleet.service;
import com.fms.fleet.exception.*;
import com.fms.fleet.kafka.TripEventPublisher;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.entity.Asset;
import com.fms.fleet.model.entity.Trip;
import com.fms.fleet.model.mapper.DtoMapper;
import com.fms.fleet.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TripService {
    @Inject TripRepository tripRepo;
    @Inject AssetRepository assetRepo;
    @Inject OperatorRepository operatorRepo;
    @Inject TripEventPublisher publisher;
    @Inject DtoMapper mapper;

    public List<TripResponse> getAll() { return tripRepo.findAll().stream().map(mapper::toResponse).toList(); }
    public List<TripResponse> getByAsset(UUID assetId) {
        assetRepo.findById(assetId).orElseThrow(()->new ResourceNotFoundException("Asset not found: "+assetId));
        return tripRepo.findByAssetId(assetId).stream().map(mapper::toResponse).toList();
    }
    public TripResponse getById(UUID id) {
        return tripRepo.findById(id).map(mapper::toResponse)
                .orElseThrow(()->new ResourceNotFoundException("Trip not found: "+id));
    }
    public TripResponse schedule(TripRequest req) {
        Asset asset=assetRepo.findById(req.assetId()).orElseThrow(()->new ResourceNotFoundException("Asset not found: "+req.assetId()));
        if (!"ACTIVE".equals(asset.status())) throw new BusinessRuleException("Asset is not ACTIVE");
        var op=operatorRepo.findById(req.operatorId()).orElseThrow(()->new ResourceNotFoundException("Operator not found: "+req.operatorId()));
        if (!"AVAILABLE".equals(op.status())) throw new BusinessRuleException("Operator is not AVAILABLE");
        Trip trip=tripRepo.create(mapper.fromRequest(req));
        publisher.publish(trip,asset,"TRIP_SCHEDULED");
        return mapper.toResponse(trip);
    }
    public TripResponse start(UUID id) {
        Trip trip=tripRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Trip not found: "+id));
        if (!"SCHEDULED".equals(trip.status())) throw new BusinessRuleException("Only SCHEDULED trips can be started");
        Asset asset=assetRepo.findById(trip.assetId()).orElseThrow();
        Trip updated=tripRepo.updateStatus(id,"EN_ROUTE",OffsetDateTime.now(),null).orElseThrow();
        operatorRepo.updateStatus(trip.operatorId(),"ON_TRIP");
        publisher.publish(updated,asset,"TRIP_STARTED");
        return mapper.toResponse(updated);
    }
    public TripResponse complete(UUID id) {
        Trip trip=tripRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Trip not found: "+id));
        if (!"EN_ROUTE".equals(trip.status())) throw new BusinessRuleException("Only EN_ROUTE trips can be completed");
        Asset asset=assetRepo.findById(trip.assetId()).orElseThrow();
        Trip updated=tripRepo.updateStatus(id,"COMPLETED",null,OffsetDateTime.now()).orElseThrow();
        operatorRepo.updateStatus(trip.operatorId(),"AVAILABLE");
        publisher.publish(updated,asset,"TRIP_COMPLETED");
        return mapper.toResponse(updated);
    }
    public TripResponse cancel(UUID id) {
        Trip trip=tripRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Trip not found: "+id));
        if ("COMPLETED".equals(trip.status())||"CANCELLED".equals(trip.status())) throw new BusinessRuleException("Cannot cancel trip with status: "+trip.status());
        Asset asset=assetRepo.findById(trip.assetId()).orElseThrow();
        Trip updated=tripRepo.updateStatus(id,"CANCELLED",null,null).orElseThrow();
        operatorRepo.findById(trip.operatorId()).ifPresent(op->{
            if ("ON_TRIP".equals(op.status())) operatorRepo.updateStatus(trip.operatorId(),"AVAILABLE");
        });
        publisher.publish(updated,asset,"TRIP_CANCELLED");
        return mapper.toResponse(updated);
    }
}
