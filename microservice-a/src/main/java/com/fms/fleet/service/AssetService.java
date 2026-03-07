package com.fms.fleet.service;
import com.fms.fleet.exception.ResourceNotFoundException;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.mapper.DtoMapper;
import com.fms.fleet.repository.AssetRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AssetService {
    @Inject AssetRepository repo;
    @Inject DtoMapper mapper;

    public List<AssetResponse> getAll() { return repo.findAll().stream().map(mapper::toResponse).toList(); }
    public List<AssetResponse> getLandVehicles() { return repo.findAllLandVehicles().stream().map(mapper::toResponse).toList(); }
    public AssetResponse getById(UUID id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(()->new ResourceNotFoundException("Asset not found: "+id));
    }
    public AssetResponse create(AssetRequest req) { return mapper.toResponse(repo.create(mapper.fromRequest(req))); }
    public AssetResponse update(UUID id, AssetRequest req) {
        return repo.update(id,mapper.fromRequest(req)).map(mapper::toResponse)
                .orElseThrow(()->new ResourceNotFoundException("Asset not found: "+id));
    }
    public void delete(UUID id) {
        if (!repo.delete(id)) throw new ResourceNotFoundException("Asset not found: "+id);
    }
}
