CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_type VARCHAR(20) NOT NULL,
    internal_name VARCHAR(100) NOT NULL,
    manufacturer VARCHAR(100),
    model_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    purchase_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS land_vehicle_details (
    asset_id UUID PRIMARY KEY REFERENCES assets(id) ON DELETE CASCADE,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    distance_counter DECIMAL(12,2) DEFAULT 0,
    fuel_type VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS aircraft_details (
    asset_id UUID PRIMARY KEY REFERENCES assets(id) ON DELETE CASCADE,
    tail_number VARCHAR(20) UNIQUE NOT NULL,
    total_flight_hours DECIMAL(12,2) DEFAULT 0,
    next_inspection_date DATE
);

CREATE TABLE IF NOT EXISTS ship_details (
    asset_id UUID PRIMARY KEY REFERENCES assets(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    vessel_type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS operators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_type VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    contact_info JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS operator_licenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operator_id UUID NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    license_type VARCHAR(50) NOT NULL,
    license_id VARCHAR(100) NOT NULL,
    issued_date DATE NOT NULL,
    expiry_date DATE,
    metadata JSONB
);

CREATE TABLE IF NOT EXISTS asset_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    operator_id UUID NOT NULL REFERENCES operators(id),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP WITH TIME ZONE,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    operator_id UUID NOT NULL REFERENCES operators(id),
    contract_id UUID NOT NULL,
    scheduled_start TIMESTAMP WITH TIME ZONE,
    scheduled_end TIMESTAMP WITH TIME ZONE,
    actual_start TIMESTAMP WITH TIME ZONE,
    actual_end TIMESTAMP WITH TIME ZONE,
    origin_name VARCHAR(255),
    destination_name VARCHAR(255),
    route_geometry GEOMETRY(LINESTRING,4326),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    distance_covered_km DECIMAL(10,2),
    fuel_consumed_liters DECIMAL(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS telemetry_logs (
    id BIGSERIAL PRIMARY KEY,
    asset_id UUID NOT NULL REFERENCES assets(id),
    trip_id UUID NOT NULL REFERENCES trips(id),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    location GEOGRAPHY(POINT,4326),
    speed_knots DECIMAL(6,2),
    raw_data JSONB
);

CREATE INDEX IF NOT EXISTS idx_telemetry_asset ON telemetry_logs(asset_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_trip  ON telemetry_logs(trip_id);
CREATE INDEX IF NOT EXISTS idx_trips_status    ON trips(status);
CREATE INDEX IF NOT EXISTS idx_assets_status   ON assets(status);

INSERT INTO assets (id,asset_type,internal_name,manufacturer,model_name,status,purchase_date) VALUES
 ('a1000000-0000-0000-0000-000000000001','CAR',  'FLEET-CAR-001','Toyota','Corolla','ACTIVE','2022-01-15'),
 ('a1000000-0000-0000-0000-000000000002','CAR',  'FLEET-CAR-002','Honda', 'Civic',  'ACTIVE','2022-03-20'),
 ('a1000000-0000-0000-0000-000000000003','TRUCK','FLEET-TRK-001','Ford',  'F-250',  'ACTIVE','2021-06-10'),
 ('a1000000-0000-0000-0000-000000000004','AIRCRAFT','FLEET-AIR-001','Cessna','172 Skyhawk','ACTIVE','2020-09-05'),
 ('a1000000-0000-0000-0000-000000000005','SHIP','FLEET-SHP-001','Damen','Stan Tug 1606','ACTIVE','2019-11-30')
ON CONFLICT DO NOTHING;

INSERT INTO land_vehicle_details (asset_id,license_plate,distance_counter,fuel_type) VALUES
 ('a1000000-0000-0000-0000-000000000001','ABC-1234',15420.50,'PETROL'),
 ('a1000000-0000-0000-0000-000000000002','XYZ-5678', 8900.00,'HYBRID'),
 ('a1000000-0000-0000-0000-000000000003','TRK-9999',45200.75,'DIESEL')
ON CONFLICT DO NOTHING;

INSERT INTO aircraft_details (asset_id,tail_number,total_flight_hours,next_inspection_date) VALUES
 ('a1000000-0000-0000-0000-000000000004','N-12345',1250.30,'2025-06-01')
ON CONFLICT DO NOTHING;

INSERT INTO ship_details (asset_id,name,vessel_type) VALUES
 ('a1000000-0000-0000-0000-000000000005','Sea Wolf I','CARGO')
ON CONFLICT DO NOTHING;

INSERT INTO operators (id,operator_type,first_name,last_name,employee_id,status,contact_info) VALUES
 ('b2000000-0000-0000-0000-000000000001','DRIVER', 'John',  'Doe',   'EMP-001','AVAILABLE','{"phone":"+1-555-0101","email":"john.doe@fms.com"}'),
 ('b2000000-0000-0000-0000-000000000002','DRIVER', 'Jane',  'Smith', 'EMP-002','AVAILABLE','{"phone":"+1-555-0102","email":"jane.smith@fms.com"}'),
 ('b2000000-0000-0000-0000-000000000003','PILOT',  'Robert','Brown', 'EMP-003','AVAILABLE','{"phone":"+1-555-0103","email":"robert.brown@fms.com"}'),
 ('b2000000-0000-0000-0000-000000000004','CAPTAIN','Maria', 'Garcia','EMP-004','AVAILABLE','{"phone":"+1-555-0104","email":"maria.garcia@fms.com"}')
ON CONFLICT DO NOTHING;
