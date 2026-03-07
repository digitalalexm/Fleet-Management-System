package com.fms.fleet.repository;

import com.fms.fleet.model.entity.Trip;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class TripRepository {

    @Inject DSLContext dsl;
    private static final String SEL =
        "SELECT id,asset_id,operator_id,contract_id,scheduled_start,scheduled_end,actual_start,actual_end,origin_name,destination_name,status,distance_covered_km,fuel_consumed_liters,created_at FROM trips";

    public List<Trip> findAll() { return dsl.fetch(SEL+" ORDER BY scheduled_start DESC NULLS LAST").map(this::map); }
    public List<Trip> findByAssetId(UUID assetId) { return dsl.fetch(SEL+" WHERE asset_id=?",assetId).map(this::map); }
    public Optional<Trip> findById(UUID id) { return dsl.fetch(SEL+" WHERE id=?",id).stream().findFirst().map(this::map); }

    @Transactional
    public Trip create(Trip t) {
        UUID id=UUID.randomUUID();
        dsl.execute("INSERT INTO trips(id,asset_id,operator_id,contract_id,scheduled_start,scheduled_end,origin_name,destination_name,status,created_at) VALUES(?,?,?,?,?,?,?,?,?,?)",
                id,t.assetId(),t.operatorId(),t.contractId(),t.scheduledStart(),t.scheduledEnd(),
                t.originName(),t.destinationName(),"SCHEDULED",OffsetDateTime.now());
        return findById(id).orElseThrow();
    }

    @Transactional
    public Optional<Trip> updateStatus(UUID id, String status, OffsetDateTime actualStart, OffsetDateTime actualEnd) {
        if (actualStart!=null&&actualEnd!=null) dsl.execute("UPDATE trips SET status=?,actual_start=?,actual_end=? WHERE id=?",status,actualStart,actualEnd,id);
        else if (actualStart!=null) dsl.execute("UPDATE trips SET status=?,actual_start=? WHERE id=?",status,actualStart,id);
        else if (actualEnd!=null) dsl.execute("UPDATE trips SET status=?,actual_end=? WHERE id=?",status,actualEnd,id);
        else dsl.execute("UPDATE trips SET status=? WHERE id=?",status,id);
        return findById(id);
    }

    @Transactional
    public boolean delete(UUID id) { return dsl.execute("DELETE FROM trips WHERE id=?",id)>0; }

    private Trip map(Record r) {
        return new Trip(r.get("id",UUID.class),r.get("asset_id",UUID.class),r.get("operator_id",UUID.class),
                r.get("contract_id",UUID.class),r.get("scheduled_start",OffsetDateTime.class),
                r.get("scheduled_end",OffsetDateTime.class),r.get("actual_start",OffsetDateTime.class),
                r.get("actual_end",OffsetDateTime.class),r.get("origin_name",String.class),
                r.get("destination_name",String.class),r.get("status",String.class),
                r.get("distance_covered_km",BigDecimal.class),r.get("fuel_consumed_liters",BigDecimal.class),
                r.get("created_at",OffsetDateTime.class));
    }
}
