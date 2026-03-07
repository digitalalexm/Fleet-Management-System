package com.fms.penalty.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum for fleet asset types.
 * - @JsonValue  → serialises to String when written to Kafka / JSON response
 * - @JsonCreator → deserialises from String when consumed from Kafka
 */
public enum AssetType {
    CAR,
    TRUCK,
    AIRCRAFT,
    SHIP;

    /** Serialise to lowercase String (e.g. for JSON responses). */
    @JsonValue
    public String toJson() {
        return this.name();
    }

    /** Deserialise from String — case-insensitive, falls back to CAR if unknown. */
    @JsonCreator
    public static AssetType fromJson(String value) {
        if (value == null) return CAR;
        try {
            return AssetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CAR;
        }
    }
}
