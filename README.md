# Fleet Management System (FMS)

Java 21 · Quarkus 3.9 · PostgreSQL + PostGIS · Redis · Apache Kafka

## Architecture

```
Client → Microservice A (Fleet API :8080)
              │  PostgreSQL (assets, operators, trips)
              │  Kafka OUT: fms.trip.events
              ▼
         Microservice B (Car Simulator :8081)
              │  Kafka OUT: fms.car.heartbeats
              ▼
         Microservice C (Penalty Engine :8082)
              │  Redis (driver penalty points)
              └  Strategy Pattern: per-asset-type rules
```

## Quick Start

```bash
docker-compose up --build
```

## Swagger UI

| Service | URL |
|---|---|
| Fleet API | http://localhost:8080/swagger-ui |
| Simulator | http://localhost:8081/swagger-ui |
| Penalty Engine | http://localhost:8082/swagger-ui |
| Kafka UI | http://localhost:8090 |

## End-to-End Demo

```bash
# List assets
curl http://localhost:8080/api/v1/assets/land-vehicles

# Schedule a trip
curl -X POST http://localhost:8080/api/v1/trips \
  -H 'Content-Type: application/json' \
  -d '{"assetId":"a1000000-0000-0000-0000-000000000001",
       "operatorId":"b2000000-0000-0000-0000-000000000001",
       "contractId":"c0000000-0000-0000-0000-000000000001",
       "originName":"Athens","destinationName":"Piraeus"}'

# Start trip (use tripId from above)
curl -X POST http://localhost:8080/api/v1/trips/<tripId>/start

# Check driver penalty score after ~30s
curl http://localhost:8082/api/v1/penalties/driver/b2000000-0000-0000-0000-000000000001

# Complete trip
curl -X POST http://localhost:8080/api/v1/trips/<tripId>/complete
```

## Penalty Strategy And Configuration

Each asset type has its own penalty strategy, fully configurable via `application.properties` in Microservice C — no recompile needed.

### Thresholds

| Asset    | Mild limit      | Mild pts/km | Severe limit     | Severe pts/km |
|----------|-----------------|-------------|------------------|---------------|
| CAR      | 60 km/h         | 2           | 80 km/h          | 5             |
| TRUCK    | 50 km/h         | 3           | 70 km/h          | 8             |
| AIRCRAFT | 200 knots       | 4           | 250 knots        | 10            |
| SHIP     | 20 km/h         | 2           | 30 km/h          | 6             |

### Property Keys
```properties
# CAR
penalty.car.mild.speed=60.0
penalty.car.mild.points=2
penalty.car.severe.speed=80.0
penalty.car.severe.points=5

# TRUCK
penalty.truck.mild.speed=50.0
penalty.truck.mild.points=3
penalty.truck.severe.speed=70.0
penalty.truck.severe.points=8

# AIRCRAFT (knots)
penalty.aircraft.mild.knots=200.0
penalty.aircraft.mild.points=4
penalty.aircraft.severe.knots=250.0
penalty.aircraft.severe.points=10

# SHIP
penalty.ship.mild.speed=20.0
penalty.ship.mild.points=2
penalty.ship.severe.speed=30.0
penalty.ship.severe.points=6
```

### How Points Are Calculated

Points are assessed per heartbeat (every 5 seconds). The highest matching threshold wins:
```
points = ceil( (currentSpeed - limit) * (5s / 3600s) * pointsPerKm )
```

### Adding a New Asset Type

1. Create a new `@ApplicationScoped` class in `com.fms.penalty.strategy` extending `AbstractPenaltyStrategy`
2. Add your thresholds in `@PostConstruct` via `addThreshold(limitKmh, pointsPerKm, label)`
3. Add the corresponding properties to `application.properties`
4. `PenaltyStrategyRegistry` discovers it automatically via CDI — no other code changes needed

## Running Tests

```bash
cd microservice-a && mvn test
cd microservice-b && mvn test
cd microservice-c && mvn test
```

Tests use pure Mockito (`@ExtendWith(MockitoExtension.class)`) — no Docker needed.
