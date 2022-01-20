# Drug Verification Management System

A production-grade backend system for pharmaceutical supply chain integrity using cryptographic serialization and blockchain-based audit logging.

## Features

- **Authentication & Authorization**: JWT-based authentication with role-based access control (RBAC)
- **Drug Registration**: Manufacturer drug registration with regulatory approval workflow
- **Batch Management**: Track manufacturing batches with expiration dates and quantities
- **Cryptographic Serialization**: HSM-integrated unit serialization with crypto-tails
- **Verification Service**: Real-time drug authenticity verification with geolocation
- **Aggregation**: Hierarchical packaging aggregation (units → cases → pallets)
- **Recall Management**: Automated recall workflows with batch-level granularity
- **Telemetry**: IoT sensor integration for temperature monitoring
- **Alert System**: Real-time alerts for temperature excursions and anomalies
- **Audit Logging**: Blockchain-based immutable audit trail
- **OpenAPI Documentation**: Interactive Swagger UI for API exploration

## Technology Stack

- **Framework**: Spring Boot 3.2+
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Security**: JWT (RS256), Argon2id password hashing
- **Cryptography**: SoftHSM2 for cryptographic operations
- **Documentation**: OpenAPI 3.0 (Springdoc)
- **Monitoring**: Micrometer + Prometheus
- **Build**: Maven 3.9+

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven 3.9+
- Git

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd drug-verification-management-system
```

### 2. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port 5432
- Redis on port 6379
- SoftHSM2 container

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Access Swagger UI

Navigate to: `http://localhost:8080/swagger-ui.html`

## API Documentation

### Authentication

**Login**
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "manufacturer",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 3600000,
  "userId": 1,
  "username": "manufacturer",
  "role": "MANUFACTURER"
}
```

### Drug Registration

```bash
POST /api/v1/drugs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Aspirin",
  "ndc": "12345-678-90",
  "manufacturer": "PharmaCorp",
  "manufacturerId": 1,
  "description": "Pain reliever",
  "dosageForm": "Tablet",
  "strength": "500mg"
}
```

### Batch Creation

```bash
POST /api/v1/batches
Authorization: Bearer <token>
Content-Type: application/json

{
  "drugId": 1,
  "batchNumber": "BATCH-001",
  "manufacturingDate": "2026-01-01",
  "expirationDate": "2028-01-01",
  "quantity": 1000
}
```

### Unit Verification

```bash
POST /api/v1/verify
Content-Type: application/json

{
  "serialNumber": "SN1234567890",
  "latitude": 43.65,
  "longitude": -79.38,
  "location": "Toronto Pharmacy",
  "deviceId": "SCANNER-001"
}
```

## User Roles

- **MANUFACTURER**: Register drugs, create batches, serialize units
- **REGULATOR**: Approve/reject drugs, manage recalls
- **DISTRIBUTOR**: View supply chain data, manage aggregations
- **PHARMACIST**: Verify units, decommission units
- **ADMIN**: Full system access

## Configuration

### Application Properties

Key configuration in `src/main/resources/application.yml`:

```yaml
application:
  jwt:
    secret: your-256-bit-secret-key
    expiration: 3600000  # 1 hour
    refresh-expiration: 604800000  # 7 days
  
  security:
    max-failed-attempts: 3
    lockout-duration-minutes: 15
```

### Environment Variables

For production deployment:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/drugverification
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=<secure-password>
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export APPLICATION_JWT_SECRET=<256-bit-secret>
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test

```bash
mvn test -Dtest=SecurityValidationTest
```

### Test Coverage

- Unit Tests: 21 tests covering services, controllers, and security
- Integration Tests: Infrastructure created (requires debugging)

## Security Features

- **JWT Authentication**: RS256 algorithm with access and refresh tokens
- **Password Hashing**: Argon2id with salt
- **RBAC**: Role-based access control on all endpoints
- **Secure Headers**: CSP, HSTS, X-Frame-Options
- **Input Validation**: Bean Validation with size and format constraints
- **Audit Logging**: Blockchain-based immutable audit trail
- **Idempotency**: Request deduplication for critical operations

## Monitoring

### Health Check

```bash
GET /actuator/health
```

### Metrics (Prometheus)

```bash
GET /actuator/prometheus
```

## Project Structure

```
src/
├── main/
│   ├── java/com/pharma/drugverification/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── domain/          # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── repository/      # Spring Data repositories
│   │   ├── security/        # Security components
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.yml  # Application configuration
└── test/
    └── java/com/pharma/drugverification/
        ├── config/          # Test configuration
        ├── controller/      # Controller tests
        ├── integration/     # Integration tests
        └── service/         # Service tests
```

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

## License

Apache 2.0

## Support

For issues and questions, please open a GitHub issue.
