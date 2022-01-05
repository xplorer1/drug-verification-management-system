# Drug Verification Management System

Production-grade backend platform for pharmaceutical supply chain integrity using Java 21 with Spring Boot 3.2+.

## Overview

This system provides comprehensive drug verification and tracking capabilities for the pharmaceutical supply chain, including:

- Secure authentication and authorization with JWT tokens
- Drug registration and regulatory approval workflow
- Batch management with chain-of-custody tracking
- HSM-based cryptographic serialization for unit authenticity
- Real-time verification with sub-50ms response time
- Cold chain monitoring with IoT telemetry integration
- Recall management with stakeholder notifications
- Counterfeit detection using anomaly detection algorithms
- Immutable audit logs with blockchain anchoring

## Technology Stack

- Java 21 LTS
- Spring Boot 3.2.2
- PostgreSQL 16+ with TimescaleDB extension
- Redis 7+ for caching
- SoftHSM2 for cryptographic operations
- Docker and Docker Compose for local development
- Maven for build management

## Prerequisites

- JDK 21 or later
- Docker and Docker Compose
- Maven 3.9+

## Getting Started

### 1. Start Infrastructure Services

```bash
docker-compose up -d
```

This starts:
- PostgreSQL with TimescaleDB on port 5432
- Redis on port 6379
- SoftHSM2 simulator

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

### 4. Access Actuator Endpoints

- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus: http://localhost:8080/actuator/prometheus

## Testing

### Run All Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

## API Documentation

Once the application is running, access the OpenAPI documentation at:
http://localhost:8080/swagger-ui.html

## Configuration

Application configuration is in `src/main/resources/application.yml`. Key configuration areas:

- Database connection settings
- Redis cache configuration
- JWT token settings
- HSM integration parameters
- Verification service settings

## Performance Targets

- Verification API: <50ms p95 response time (cached)
- Verification API: <200ms p95 response time (uncached)
- Throughput: 1000 requests/second sustained load
- Code coverage: >80%

## Security Features

- TLS 1.3 for all endpoints
- AES-256 encryption at rest
- Argon2id password hashing
- JWT with RS256 signing
- HSM for cryptographic operations
- Rate limiting per user and endpoint
- PII redaction in logs

## Compliance

- Immutable audit logs with hash chains
- Blockchain anchoring of audit logs
- 7-year audit retention

## Project Structure

```
src/
├── main/
│   ├── java/com/pharma/drugverification/
│   │   ├── config/          # Configuration classes
│   │   ├── domain/          # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST endpoints
│   │   └── security/        # Security configuration
│   └── resources/
│       ├── application.yml  # Application configuration
│       ├── logback-spring.xml  # Logging configuration
│       └── db/migration/    # Flyway database migrations
└── test/
    └── java/                # Unit and integration tests
```

## License

Proprietary - All rights reserved
