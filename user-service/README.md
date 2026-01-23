# User Service
A comprehensive microservice for user profile management in the Soulmate dating platform. 
This service handles user registration, profile management, photo storage, 
and integrates with facial recognition services for intelligent matching.

## 🚀 Features

### Core Functionality
- **User Registration**: Complete profile creation with validation
- **Profile Management**: Update user information and preferences
- **Photo Management**: Upload, store, and manage profile photos
- **Soft/Hard Delete**: Flexible user deletion options
- **Feed Generation**: Personalized user discovery feeds
- **Event Publishing**: Outbox pattern for reliable event delivery

### Advanced Features
- **Facial Recognition Integration**: Face landmark detection for matching
- **Object Storage**: MinIO integration for efficient photo storage
- **Audit Trail**: Complete history tracking with Hibernate Envers
- **Security**: OAuth2/JWT authentication with Keycloak
- **Observability**: Full monitoring with OpenTelemetry and Prometheus

## 🏗️ Architecture

### Technology Stack
- **Java 24** with Spring Boot 3.5.0
- **Spring Security** with OAuth2 Resource Server
- **PostgreSQL** with Hibernate Envers for auditing
- **Liquibase** for database migrations
- **MinIO** for object storage
- **Kafka** for event streaming
- **OpenTelemetry** for observability
- **Testcontainers** for integration testing

### Service Components
- **Controllers**: REST API endpoints for profiles and photos
- **Services**: Business logic and orchestration
- **Repositories**: Data access layer with JPA
- **Configuration**: Security, storage, and external service integration
- **Exception Handling**: Comprehensive error management

## 📡 API Endpoints

### Profile Management
```http
POST   /api/v1/profiles/registration          # Register new user
GET    /api/v1/profiles/{id}                 # Get profile by ID
PUT    /api/v1/profiles                       # Update current user profile
DELETE /api/v1/profiles/{id}                  # Soft delete profile
DELETE /api/v1/profiles/{id}/hard            # Hard delete profile
```

### Photo Management
```http
POST   /api/v1/photos                         # Upload profile photo
DELETE /api/v1/photos/{photoId}               # Delete photo
```

### Feed Management
```http
GET    /api/v1/feed                           # Get personalized feed
```

### Admin/Compensation
```http
DELETE /api/v1/profiles/{id}/compensation     # Compensate failed registration
```

## 🔧 Configuration

### Application Properties
The service uses environment-based configuration with the following key settings:

```yaml
server:
  port: 8082

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/profile_db
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
  liquibase:
    enabled: true
    change-log: db/changelog/master.xml
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${RESOURCE_SERVER_ISSUER_URI:http://localhost:8080/realms/customer}

minio:
  endpoint: ${MINIO_ENDPOINT:http://127.0.0.1:9000}
  accessKey: ${MINIO_ACCESS_KEY:user}
  secretKey: ${MINIO_SECRET_KEY:password}

face:
  url: ${FACE_API_URL:https://api-us.faceplusplus.com}
  key: ${FACE_API_KEY:test}
  secret: ${FACE_API_SECRET:secret}
```

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_HOST` | PostgreSQL server host | `localhost` |
| `POSTGRES_PORT` | PostgreSQL server port | `5432` |
| `POSTGRES_USER` | Database username | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `postgres` |
| `MINIO_ENDPOINT` | MinIO server endpoint | `http://127.0.0.1:9000` |
| `MINIO_ACCESS_KEY` | MinIO access key | `user` |
| `MINIO_SECRET_KEY` | MinIO secret key | `password` |
| `FACE_API_URL` | Face recognition service URL | `https://api-us.faceplusplus.com` |
| `RESOURCE_SERVER_ISSUER_URI` | OAuth2 issuer URI | `http://localhost:8080/realms/customer` |

## 🗄️ Database Schema

### Core Tables
- **profile**: User profile information
- **profile_history**: Audit trail for profile changes
- **outbox**: Event publishing table

### Key Features
- **Schema**: `profile` for application data
- **Audit Schema**: `profile_service_history` for change tracking
- **Soft Deletes**: Logical deletion with timestamps
- **Event Sourcing**: Outbox pattern for reliable messaging

## 🧪 Testing

### Test Structure
- **Unit Tests**: Service and repository layer testing
- **Integration Tests**: Full stack testing with Testcontainers
- **API Tests**: Controller endpoint testing
- **Event Tests**: Kafka event verification

### Running Tests
```bash
# Run all tests
./gradlew test

# Run integration tests only
./gradlew test --tests "*IntegrationTest"

# Run with specific test profile
./gradlew test -Dspring.profiles.active=test
```

### Test Coverage
- ✅ User registration and validation
- ✅ Profile CRUD operations
- ✅ Photo upload and management
- ✅ Soft/hard delete functionality
- ✅ Event publishing and verification
- ✅ Error handling and edge cases
- ✅ Security and authentication

## 📊 Monitoring & Observability

### Metrics
- **Prometheus**: `/actuator/prometheus`
- **Health Checks**: `/actuator/health`
- **Custom Metrics**: Business and performance indicators

### Tracing
- **OpenTelemetry**: Distributed tracing
- **OTLP Export**: Standardized trace format
- **Correlation IDs**: Request tracking across services

### Logging
- **Structured JSON**: Logstash format
- **Correlation**: Request-scoped logging
- **Levels**: Configurable per environment

## 🔐 Security

### Authentication
- **OAuth2/JWT**: Token-based authentication
- **Keycloak Integration**: Identity and access management
- **Resource Server**: Spring Security configuration

### Authorization
- **Role-based Access**: Profile ownership validation
- **Security Context**: Current user extraction
- **Input Validation**: Comprehensive request validation

## 🚦 Getting Started

### Prerequisites
- Java 24
- Docker & Docker Compose
- PostgreSQL 15+
- MinIO
- Kafka (for event processing)

### Local Development

1. **Clone and Build**
   ```bash
   git clone https://github.com/maxbpro/soulmate-system.git
   cd soulmate-system/user-service
   ./gradlew build
   ```

2. **Start Infrastructure**
   ```bash
   docker-compose up -d postgres minio kafka
   ```

3. **Run the Service**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the Service**
   - API: `http://localhost:8082`
   - Health: `http://localhost:8082/actuator/health`
   - Metrics: `http://localhost:8082/actuator/prometheus`

## 📦 Deployment

### Docker
```bash
# Build image
docker build -t soulmate/user-service .

# Run container
docker run -p 8082:8082 soulmate/user-service
```

### Kubernetes
```bash
# Deploy using Helm
helm install user-service ./k8s

# Or apply manifests directly
kubectl apply -f k8s/templates/
```

### Environment Configuration
- **Development**: Local configuration with default values
- **Staging**: Pre-production environment with external services
- **Production**: Full configuration with monitoring and security

## 🔗 External Integrations

### Face Recognition Service
- **Purpose**: Extract facial landmarks for matching
- **Provider**: Face++ API (or compatible service)
- **Features**: Landmark detection, face analysis

### Object Storage
- **Service**: MinIO S3-compatible storage
- **Usage**: Profile photo storage and retrieval
- **Features**: Multi-part upload, metadata management

### Message Broker
- **Service**: Apache Kafka
- **Topics**: Profile events for downstream processing
- **Pattern**: Outbox pattern for reliable delivery

## 🛠️ Development Guidelines

### Code Style
- **Java 24**: Modern Java features and patterns
- **Spring Boot**: Convention over configuration
- **Lombok**: Reduce boilerplate code
- **MapStruct**: Type-safe mapping

### Best Practices
- **Transactional**: Proper transaction management
- **Validation**: Input validation at all layers
- **Error Handling**: Consistent exception management
- **Testing**: Comprehensive test coverage
- **Documentation**: Clear API documentation

## 📝 API Documentation

### OpenAPI/Swagger
- **Specification**: Auto-generated from API interfaces
- **UI Available**: `/swagger-ui.html` (when enabled)
- **Contract**: Shared API definitions in common module

### Request/Response Examples
```json
// Registration Request
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "birthDate": "1990-11-14",
  "gender": "MALE",
  "interestedIn": "FEMALE",
  "ageMin": 18,
  "ageMax": 99,
  "radius": 10,
  "phoneNumber": "1234567890",
  "photo": "base64-encoded-image"
}

// Profile Response
{
  "id": "uuid",
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "photos": ["uuid1", "uuid2"],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

## 🐛 Troubleshooting

### Common Issues
- **Database Connection**: Check PostgreSQL configuration
- **MinIO Access**: Verify credentials and endpoint
- **Face API**: Validate API keys and quotas
- **Authentication**: Ensure Keycloak is accessible

### Health Checks
```bash
# Service health
curl http://localhost:8082/actuator/health

# Database connectivity
curl http://localhost:8082/actuator/health/db

# External service health
curl http://localhost:8082/actuator/health/minio
```

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch
3. **Implement** your changes with tests
4. **Run** the full test suite
5. **Submit** a pull request with description

### Development Workflow
- **Code Reviews**: All changes require review
- **Testing**: Maintain high test coverage
- **Documentation**: Update relevant documentation
- **Compatibility**: Ensure backward compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../../LICENSE) file for details.

## 📞 Support

For questions and support:
- **Issues**: Create GitHub issues in the repository
- **Documentation**: Check the main project README
- **Examples**: Review integration tests for usage patterns

---

**Part of the Soulmate microservices platform**