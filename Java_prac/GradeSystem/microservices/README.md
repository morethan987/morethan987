# Grade System - Microservices Architecture

Spring Boot 3.4 microservices backend for the Grade Management System.

## Architecture

```
                                    ┌─────────────────┐
                                    │    Frontend     │
                                    │   (React/Bun)   │
                                    │     :80         │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │  API Gateway    │
                                    │    :8080        │
                                    └────────┬────────┘
                                             │
        ┌────────────────┬───────────────────┼───────────────────┬────────────────┐
        │                │                   │                   │                │
┌───────▼───────┐ ┌──────▼──────┐  ┌─────────▼─────────┐ ┌───────▼───────┐ ┌──────▼──────┐
│ Auth Service  │ │User Service │  │ Academic Service  │ │ Grade Service │ │  Analytics  │
│    :8081      │ │   :8082     │  │      :8083        │ │    :8084      │ │    :8085    │
└───────┬───────┘ └──────┬──────┘  └─────────┬─────────┘ └───────┬───────┘ └──────┬──────┘
        │                │                   │                   │                │
        └────────────────┴───────────────────┼───────────────────┴────────────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
           ┌────────▼────────┐     ┌─────────▼─────────┐     ┌────────▼────────┐
           │    Consul       │     │    PostgreSQL     │     │      Redis      │
           │ Service Disc.   │     │     Database      │     │  Session/Cache  │
           │    :8500        │     │      :5432        │     │     :6379       │
           └─────────────────┘     └───────────────────┘     └─────────────────┘
```

## Services

| Service | Port | Schema | Description |
|---------|------|--------|-------------|
| grade-gateway | 8080 | - | API Gateway with routing and load balancing |
| grade-auth-service | 8081 | auth_schema | Authentication, sessions, roles, permissions |
| grade-user-service | 8082 | user_schema | Users, students, teachers management |
| grade-academic-service | 8083 | academic_schema | Courses, classes, enrollments |
| grade-grade-service | 8084 | grade_schema | Grades, GPA calculations |
| grade-analytics-service | 8085 | - | Dashboard aggregation (no database) |

## Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven (or use included wrapper)

### Local Development

```bash
# Start infrastructure (Consul, PostgreSQL, Redis, RabbitMQ)
docker compose up -d consul postgres redis rabbitmq

# Build all services
./mvnw package -DskipTests

# Run individual services
./mvnw spring-boot:run -pl grade-auth-service
./mvnw spring-boot:run -pl grade-user-service
# ... etc
```

### Docker Deployment

```bash
# Build and run all services
docker compose up -d --build

# View logs
docker compose logs -f

# Stop all
docker compose down
```

## API Routes (via Gateway :8080)

| Path | Service |
|------|---------|
| `/api/v1/auth/**`, `/api/v1/role/**` | auth-service |
| `/api/v1/user/**`, `/api/v1/student/**`, `/api/v1/teacher/**` | user-service |
| `/api/v1/courses/**` | academic-service |
| `/api/v1/grades/**`, `/api/v1/concurrency/**` | grade-service |
| `/api/v1/dashboard/**` | analytics-service |

## Technology Stack

- **Framework**: Spring Boot 3.4.1, Spring Cloud 2024.0.0
- **Language**: Java 21
- **Service Discovery**: Consul
- **API Gateway**: Spring Cloud Gateway
- **Inter-service Communication**: OpenFeign
- **Database**: PostgreSQL 16 (separate schema per service)
- **Session Store**: Redis 7
- **Message Queue**: RabbitMQ 3.13
- **API Documentation**: SpringDoc OpenAPI 2.7
- **Build**: Maven

## Project Structure

```
microservices/
├── grade-common/           # Shared DTOs, utilities
├── grade-gateway/          # API Gateway
├── grade-auth-service/     # Authentication
├── grade-user-service/     # User management
├── grade-academic-service/ # Course management
├── grade-grade-service/    # Grade management
├── grade-analytics-service/# Analytics aggregation
├── docker-compose.yml      # Full stack deployment
├── init-schemas.sql        # Database schema initialization
└── pom.xml                 # Parent POM
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| CONSUL_HOST | localhost | Consul server host |
| CONSUL_PORT | 8500 | Consul server port |
| DB_HOST | localhost | PostgreSQL host |
| DB_USER | grade_admin | Database user |
| DB_PASSWORD | grade_password | Database password |
| REDIS_HOST | localhost | Redis host |
| REDIS_PORT | 6379 | Redis port |
| RABBITMQ_HOST | localhost | RabbitMQ host |
| RABBITMQ_PORT | 5672 | RabbitMQ port |
| RABBITMQ_USER | admin | RabbitMQ user |
| RABBITMQ_PASSWORD | admin123 | RabbitMQ password |

## Service URLs (Local Development)

| Service | URL |
|---------|-----|
| Consul UI | http://localhost:8500 |
| RabbitMQ UI | http://localhost:15672 (admin/admin123) |
| API Gateway | http://localhost:8080 |
| Frontend | http://localhost:80 |

## Local Development (Running JARs Directly)

If you prefer running services directly without Docker (for faster iteration):

```bash
# 1. Start infrastructure only
docker compose up -d consul postgres redis rabbitmq

# 2. Wait for services to be healthy
docker compose ps

# 3. Build all services
./mvnw clean package -DskipTests

# 4. Start services in order (each in separate terminal, or background with &)
java -jar grade-auth-service/target/grade-auth-service-1.0.0-SNAPSHOT.jar &
sleep 10
java -jar grade-user-service/target/grade-user-service-1.0.0-SNAPSHOT.jar &
java -jar grade-academic-service/target/grade-academic-service-1.0.0-SNAPSHOT.jar &
java -jar grade-grade-service/target/grade-grade-service-1.0.0-SNAPSHOT.jar &
java -jar grade-analytics-service/target/grade-analytics-service-1.0.0-SNAPSHOT.jar &
java -jar grade-gateway/target/grade-gateway-1.0.0-SNAPSHOT.jar &

# 5. Check all services are healthy
for port in 8081 8082 8083 8084 8085 8080; do
  echo "Port $port: $(curl -s http://localhost:$port/actuator/health)"
done
```

## Testing API Endpoints

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123!","confirmPassword":"Test123!","email":"test@example.com"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"testuser","password":"Test123!"}'

# Access authenticated endpoints (using session cookie)
curl -b cookies.txt http://localhost:8080/api/v1/auth/me
```

## Troubleshooting

### RabbitMQ Authentication Failed
If you see `ACCESS_REFUSED - Login was refused`, verify RabbitMQ credentials:
```bash
docker exec grade-rabbitmq rabbitmqctl authenticate_user admin admin123
```

### Service Not Registering with Consul
Check if Consul is reachable and the service has actuator health endpoint:
```bash
curl http://localhost:8500/v1/catalog/services
curl http://localhost:8081/actuator/health
```

### Database Schema Issues
Each service uses its own schema. Verify schemas exist:
```bash
docker exec grade-postgres psql -U grade_admin -d grade_system -c "\dn"
```

### Killing All Java Services
```bash
pkill -f "grade-"
```
