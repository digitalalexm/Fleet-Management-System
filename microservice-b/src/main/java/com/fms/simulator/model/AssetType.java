package com.fms.simulator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Fleet asset types used by the simulator.
 *  - @JsonValue   → enum serialises to String in heartbeat JSON published to Kafka
 *  - @JsonCreator → enum deserialised from String in trip-event JSON consumed from Kafka
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
        if (value == null) return CAR;
        try {
            return AssetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CAR;  // unknown type falls back to CAR behaviour
        }
    }
}
