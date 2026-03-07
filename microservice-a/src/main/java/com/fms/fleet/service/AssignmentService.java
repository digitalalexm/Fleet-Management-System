package com.fms.fleet.service;
import com.fms.fleet.exception.*;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.mapper.DtoMapper;
import com.fms.fleet.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class AssignmentService {
    @Inject AssignmentRepository assignRepo;
    @Inject AssetRepository assetRepo;
    @Inject OperatorRepository operatorRepo;
    @Inject DtoMapper mapper;

    public AssignmentResponse assign(AssignmentRequest req) {
        var asset=assetRepo.findById(req.assetId()).orElseThrow(()->new ResourceNotFoundException("Asset not found: "+req.assetId()));
        if (!"ACTIVE".equals(asset.status())) throw new BusinessRuleException("Asset is not ACTIVE");
        var op=operatorRepo.findById(req.operatorId()).orElseThrow(()->new ResourceNotFoundException("Operator not found: "+req.operatorId()));
        if (!"AVAILABLE".equals(op.status())) throw new BusinessRuleException("Operator is not AVAILABLE");
        assignRepo.findCurrentAssignment(req.assetId()).ifPresent(e->{throw new BusinessRuleException("Asset already has an active assignment");});
        UUID id=assignRepo.create(req.assetId(),req.operatorId(),req.notes());
        return mapper.toAssignmentResponse(id,req.assetId(),req.operatorId(),OffsetDateTime.now(),null,req.notes());
    }
    public List<AssignmentResponse> getByAsset(UUID assetId) {
        assetRepo.findById(assetId).orElseThrow(()->new ResourceNotFoundException("Asset not found: "+assetId));
        return assignRepo.findActiveByAssetId(assetId).stream().map(row->mapper.toAssignmentResponse(
                (UUID)row.get("id"),(UUID)row.get("asset_id"),(UUID)row.get("operator_id"),
                toODT(row.get("assigned_at")),toODT(row.get("released_at")),(String)row.get("notes"))).toList();
    }
    public void release(UUID id) {
        if (!assignRepo.release(id)) throw new ResourceNotFoundException("Active assignment not found: "+id);
    }
    private OffsetDateTime toODT(Object v) {
        if (v==null) return null;
        if (v instanceof OffsetDateTime odt) return odt;
        if (v instanceof java.sql.Timestamp ts) return ts.toInstant().atOffset(java.time.ZoneOffset.UTC);
        return null;
    }
}
