package com.fms.fleet.service;
import com.fms.fleet.exception.*;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.mapper.DtoMapper;
import com.fms.fleet.repository.OperatorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OperatorService {
    @Inject OperatorRepository repo;
    @Inject DtoMapper mapper;

    public List<OperatorResponse> getAll() { return repo.findAll().stream().map(mapper::toResponse).toList(); }
    public List<OperatorResponse> getAvailable() { return repo.findAvailable().stream().map(mapper::toResponse).toList(); }
    public OperatorResponse getById(UUID id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(()->new ResourceNotFoundException("Operator not found: "+id));
    }
    public OperatorResponse create(OperatorRequest req) {
        repo.findByEmployeeId(req.employeeId()).ifPresent(e->{throw new BusinessRuleException("Employee ID already registered: "+req.employeeId());});
        return mapper.toResponse(repo.create(mapper.fromRequest(req)));
    }
    public OperatorResponse update(UUID id, OperatorRequest req) {
        return repo.update(id,mapper.fromRequest(req)).map(mapper::toResponse)
                .orElseThrow(()->new ResourceNotFoundException("Operator not found: "+id));
    }
    public void delete(UUID id) {
        var op=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Operator not found: "+id));
        if ("ON_TRIP".equals(op.status())) throw new BusinessRuleException("Cannot delete operator who is ON_TRIP");
        repo.delete(id);
    }
}
