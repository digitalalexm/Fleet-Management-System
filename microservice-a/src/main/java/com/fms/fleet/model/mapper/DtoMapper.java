package com.fms.fleet.model.mapper;

import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.util.UUID;

@ApplicationScoped
public class DtoMapper {

    // ── Asset ────────────────────────────────────────────────────────
    public AssetResponse toResponse(Asset a) {
        return new AssetResponse(
                a.id(),
                a.assetType().name(),   // enum → String for JSON
                a.internalName(), a.manufacturer(), a.modelName(),
                a.status(), a.purchaseDate(), a.createdAt(),
                a.licensePlate(), a.distanceCounter(), a.fuelType(),
                a.tailNumber(), a.totalFlightHours(), a.nextInspectionDate(),
                a.shipName(), a.vesselType());
    }

    public Asset fromRequest(AssetRequest r) {
        return new Asset(
                null,
                AssetType.fromJson(r.assetType()),  // String → enum (validated)
                r.internalName(), r.manufacturer(), r.modelName(),
                r.status(), r.purchaseDate(), null,
                r.licensePlate(), r.distanceCounter(), r.fuelType(),
                r.tailNumber(), r.totalFlightHours(), r.nextInspectionDate(),
                r.shipName(), r.vesselType());
    }

    // ── Operator ─────────────────────────────────────────────────────
    public OperatorResponse toResponse(Operator o) {
        return new OperatorResponse(o.id(), o.operatorType(), o.firstName(), o.lastName(),
                o.employeeId(), o.status(), o.contactInfo(), o.createdAt());
    }

    public Operator fromRequest(OperatorRequest r) {
        return new Operator(null, r.operatorType(), r.firstName(), r.lastName(),
                r.employeeId(), r.status(), r.contactInfo(), null);
    }

    // ── Trip ─────────────────────────────────────────────────────────
    public TripResponse toResponse(Trip t) {
        return new TripResponse(t.id(), t.assetId(), t.operatorId(), t.contractId(),
                t.scheduledStart(), t.scheduledEnd(), t.actualStart(), t.actualEnd(),
                t.originName(), t.destinationName(), t.status(),
                t.distanceCoveredKm(), t.fuelConsumedLiters(), t.createdAt());
    }

    public Trip fromRequest(TripRequest r) {
        return new Trip(null, r.assetId(), r.operatorId(), r.contractId(),
                r.scheduledStart(), r.scheduledEnd(), null, null,
                r.originName(), r.destinationName(), "SCHEDULED", null, null, null);
    }

    // ── Assignment ───────────────────────────────────────────────────
    public AssignmentResponse toAssignmentResponse(UUID id, UUID assetId, UUID operatorId,
            OffsetDateTime assignedAt, OffsetDateTime releasedAt, String notes) {
        return new AssignmentResponse(id, assetId, operatorId, assignedAt, releasedAt, notes);
    }
}
