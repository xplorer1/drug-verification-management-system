# Deployment Guide

## Production Deployment Checklist

### 1. Environment Setup

#### Database Configuration

```bash
# Create production database
createdb -U postgres drugverification_prod

# Run migrations (handled by Hibernate on startup)
# Ensure spring.jpa.hibernate.ddl-auto=validate in production
```

#### Redis Configuration

```bash
# Install Redis
sudo apt-get install redis-server

# Configure Redis for persistence
# Edit /etc/redis/redis.conf
appendonly yes
appendfsync everysec
```

#### HSM Configuration

For production, replace SoftHSM2 with a hardware HSM:

```yaml
application:
  hsm:
    pkcs11-library: /usr/lib/libpkcs11.so
    slot-index: 0
    pin: ${HSM_PIN}
```

### 2. Application Configuration

Create `application-prod.yml`:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

application:
  jwt:
    secret: ${JWT_SECRET}  # Must be 256-bit
    expiration: 3600000
    refresh-expiration: 604800000
  
  security:
    max-failed-attempts: 5
    lockout-duration-minutes: 30

logging:
  level:
    root: INFO
    com.pharma.drugverification: INFO
```

### 3. Security Hardening

#### Generate JWT Secret

```bash
# Generate a secure 256-bit secret
openssl rand -base64 32
```

#### SSL/TLS Configuration

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
```

#### Firewall Rules

```bash
# Allow only necessary ports
sudo ufw allow 8443/tcp  # HTTPS
sudo ufw allow 5432/tcp  # PostgreSQL (internal only)
sudo ufw allow 6379/tcp  # Redis (internal only)
sudo ufw enable
```

### 4. Build for Production

```bash
# Clean build
mvn clean package -DskipTests

# The JAR will be in target/drug-verification-1.0.0-SNAPSHOT.jar
```

### 5. Docker Deployment

#### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/drug-verification-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

#### Build Docker Image

```bash
docker build -t drug-verification:1.0.0 .
```

#### Docker Compose for Production

```yaml
version: '3.8'

services:
  app:
    image: drug-verification:1.0.0
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://db:5432/drugverification
      - DATABASE_USERNAME=postgres
      - DATABASE_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - db
      - redis
    restart: unless-stopped
  
  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=drugverification
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped
  
  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
```

### 6. Kubernetes Deployment

#### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: drug-verification-config
data:
  application.yml: |
    spring:
      profiles:
        active: prod
```

#### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: drug-verification
spec:
  replicas: 3
  selector:
    matchLabels:
      app: drug-verification
  template:
    metadata:
      labels:
        app: drug-verification
    spec:
      containers:
      - name: app
        image: drug-verification:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: drug-verification-secrets
              key: database-url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: drug-verification-secrets
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

#### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: drug-verification
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: drug-verification
```

### 7. Monitoring Setup

#### Prometheus Configuration

```yaml
scrape_configs:
  - job_name: 'drug-verification'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

#### Grafana Dashboard

Import the Spring Boot dashboard (ID: 4701) and customize for drug verification metrics.

### 8. Backup Strategy

#### Database Backup

```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U postgres drugverification > backup_$DATE.sql
aws s3 cp backup_$DATE.sql s3://backups/drugverification/
```

#### Redis Backup

```bash
# Redis AOF is automatically persisted
# Copy AOF file for backup
cp /var/lib/redis/appendonly.aof /backups/redis_$DATE.aof
```

### 9. Performance Tuning

#### JVM Options

```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar app.jar
```

#### Database Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 10. Post-Deployment Verification

```bash
# Health check
curl https://your-domain.com/actuator/health

# Test authentication
curl -X POST https://your-domain.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secure-password"}'

# Verify OpenAPI docs
curl https://your-domain.com/v3/api-docs
```

## Rollback Procedure

1. Stop the new version
2. Restore database from backup if schema changed
3. Deploy previous version
4. Verify health checks
5. Monitor logs for errors

## Troubleshooting

### Application Won't Start

- Check database connectivity
- Verify Redis is running
- Check JWT secret is configured
- Review application logs

### High Memory Usage

- Increase JVM heap size
- Check for memory leaks in logs
- Review connection pool settings

### Slow Performance

- Check database query performance
- Verify Redis cache hit rate
- Review connection pool utilization
- Check for N+1 query problems

## Support

For production issues, contact the development team or open a critical issue on GitHub.
