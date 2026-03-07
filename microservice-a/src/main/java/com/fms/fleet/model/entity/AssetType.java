package com.fms.fleet.model.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Fleet asset types.
 *  - @JsonValue   → serialises enum to String in JSON responses
 *  - @JsonCreator → deserialises String from JSON requests (case-insensitive)
 * When persisted via jOOQ: use assetType().name() → writes "CAR", "TRUCK" etc.
 * When reading from DB:   AssetType.valueOf(rs.getString("asset_type"))
 */
public enum AssetType {
    CAR,
    TRUCK,
    AIRCRAFT,
    SHIP;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static AssetType fromJson(String value) {
        if (value == null) throw new IllegalArgumentException("assetType must not be null");
        return AssetType.valueOf(value.toUpperCase());
    }
}
