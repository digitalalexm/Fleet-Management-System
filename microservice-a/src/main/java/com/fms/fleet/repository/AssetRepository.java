package com.fms.fleet.repository;

import com.fms.fleet.model.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@ApplicationScoped
public class AssetRepository {

    @Inject DSLContext dsl;

    private static final String JOIN =
        "SELECT a.id, a.asset_type, a.internal_name, a.manufacturer, a.model_name, " +
        "       a.status, a.purchase_date, a.created_at, " +
        "       lv.license_plate, lv.distance_counter, lv.fuel_type, " +
        "       ac.tail_number, ac.total_flight_hours, ac.next_inspection_date, " +
        "       sh.name AS ship_name, sh.vessel_type " +
        "FROM assets a " +
        "LEFT JOIN land_vehicle_details lv ON a.id = lv.asset_id " +
        "LEFT JOIN aircraft_details     ac ON a.id = ac.asset_id " +
        "LEFT JOIN ship_details         sh ON a.id = sh.asset_id";

    public List<Asset> findAll() {
        return dsl.fetch(JOIN).map(this::map);
    }

    public List<Asset> findAllLandVehicles() {
        return dsl.fetch(JOIN + " WHERE a.asset_type IN ('CAR','TRUCK')").map(this::map);
    }

    public Optional<Asset> findById(UUID id) {
        return dsl.fetch(JOIN + " WHERE a.id = ?", id).stream().findFirst().map(this::map);
    }

    @Transactional
    public Asset create(Asset a) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        // enum → .name() → String written to DB
        dsl.execute(
            "INSERT INTO assets(id,asset_type,internal_name,manufacturer,model_name,status,purchase_date,created_at) VALUES(?,?,?,?,?,?,?,?)",
            id, a.assetType().name(), a.internalName(), a.manufacturer(), a.modelName(),
            a.status() != null ? a.status() : "ACTIVE", a.purchaseDate(), now);

        switch (a.assetType()) {
            case CAR, TRUCK -> dsl.execute(
                "INSERT INTO land_vehicle_details(asset_id,license_plate,distance_counter,fuel_type) VALUES(?,?,?,?)",
                id, a.licensePlate(),
                a.distanceCounter() != null ? a.distanceCounter() : BigDecimal.ZERO,
                a.fuelType());
            case AIRCRAFT -> dsl.execute(
                "INSERT INTO aircraft_details(asset_id,tail_number,total_flight_hours,next_inspection_date) VALUES(?,?,?,?)",
                id, a.tailNumber(),
                a.totalFlightHours() != null ? a.totalFlightHours() : BigDecimal.ZERO,
                a.nextInspectionDate());
            case SHIP -> dsl.execute(
                "INSERT INTO ship_details(asset_id,name,vessel_type) VALUES(?,?,?)",
                id, a.shipName(), a.vesselType());
        }
        return findById(id).orElseThrow();
    }

    @Transactional
    public Optional<Asset> update(UUID id, Asset a) {
        int rows = dsl.execute(
            "UPDATE assets SET internal_name=?,manufacturer=?,model_name=?,status=?,purchase_date=? WHERE id=?",
            a.internalName(), a.manufacturer(), a.modelName(), a.status(), a.purchaseDate(), id);
        if (rows == 0) return Optional.empty();

        switch (a.assetType()) {
            case CAR, TRUCK -> dsl.execute(
                "UPDATE land_vehicle_details SET license_plate=?,distance_counter=?,fuel_type=? WHERE asset_id=?",
                a.licensePlate(), a.distanceCounter(), a.fuelType(), id);
            case AIRCRAFT -> dsl.execute(
                "UPDATE aircraft_details SET tail_number=?,total_flight_hours=?,next_inspection_date=? WHERE asset_id=?",
                a.tailNumber(), a.totalFlightHours(), a.nextInspectionDate(), id);
            case SHIP -> dsl.execute(
                "UPDATE ship_details SET name=?,vessel_type=? WHERE asset_id=?",
                a.shipName(), a.vesselType(), id);
        }
        return findById(id);
    }

    @Transactional
    public boolean delete(UUID id) {
        return dsl.execute("DELETE FROM assets WHERE id=?", id) > 0;
    }

    /** DB String → AssetType enum when reading rows */
    private Asset map(Record r) {
        return new Asset(
                r.get("id",           UUID.class),
                AssetType.valueOf(r.get("asset_type", String.class)),  // String → enum
                r.get("internal_name",       String.class),
                r.get("manufacturer",        String.class),
                r.get("model_name",          String.class),
                r.get("status",              String.class),
                r.get("purchase_date",       LocalDate.class),
                r.get("created_at",          OffsetDateTime.class),
                r.get("license_plate",       String.class),
                r.get("distance_counter",    BigDecimal.class),
                r.get("fuel_type",           String.class),
                r.get("tail_number",         String.class),
                r.get("total_flight_hours",  BigDecimal.class),
                r.get("next_inspection_date",LocalDate.class),
                r.get("ship_name",           String.class),
                r.get("vessel_type",         String.class));
    }
}
