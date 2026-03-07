package com.fms.fleet.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class AssignmentRepository {
    
    @Inject 
    DSLContext dsl;

    private static final String SEL="SELECT id,asset_id,operator_id,assigned_at,released_at,notes FROM asset_assignments";

    public List<Map<String,Object>> findActiveByAssetId(UUID assetId) {
        return dsl.fetch(SEL+" WHERE asset_id=? AND released_at IS NULL",assetId).intoMaps();
    }

    public Optional<Map<String,Object>> findCurrentAssignment(UUID assetId) {
        return dsl.fetch(SEL+" WHERE asset_id=? AND released_at IS NULL ORDER BY assigned_at DESC LIMIT 1",assetId).intoMaps().stream().findFirst();
    }
    
    @Transactional
    public UUID create(UUID assetId, UUID operatorId, String notes) {
        UUID id=UUID.randomUUID();
        dsl.execute("INSERT INTO asset_assignments(id,asset_id,operator_id,assigned_at,notes) VALUES(?,?,?,?,?)",
                id,assetId,operatorId,OffsetDateTime.now(),notes);
        return id;
    }

    @Transactional
    public boolean release(UUID id) {
        return dsl.execute("UPDATE asset_assignments SET released_at=? WHERE id=? AND released_at IS NULL",OffsetDateTime.now(),id)>0;
    }
}
