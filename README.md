# Spring Boot Microservices Project

This project implements a microservices architecture using Spring Boot and Kotlin, focusing on user management, organization management, and invitation handling through a secure API Gateway.

## Architecture

The project consists of the following microservices:

- **Eureka Server** (Port: 8761)
  - Service discovery and registration
  - Centralized service registry

- **API Gateway** (Port: 8080)
  - Route management
  - JWT authentication
  - Role-based access control
  - Request filtering

- **Auth Service** (Port: 8083)
  - AWS Cognito integration
  - User authentication
  - JWT token management
  - Role-based authorization

## Technology Stack

- **Language:** Kotlin 1.9.10
- **Framework:** Spring Boot 3.1.5
- **Build Tool:** Gradle 8.4 with Kotlin DSL
- **Cloud Services:** AWS Cognito
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Security:** JWT, Spring Security

## Project Setup

### Prerequisites

- JDK 17 or later
- Gradle 8.4+
- AWS Account with Cognito User Pool
- Environment Variables:
  ```
  AWS_COGNITO_USER_POOL_ID=your-user-pool-id
  AWS_COGNITO_CLIENT_ID=your-client-id
  AWS_COGNITO_CLIENT_SECRET=your-client-secret
  ```

### Building the Project

```bash
./gradlew clean build
```

### Running the Services

Start the services in the following order:

1. Eureka Server:
```bash
./gradlew :eureka-server:bootRun
```

2. Auth Service:
```bash
./gradlew :auth-service:bootRun
```

3. API Gateway:
```bash
./gradlew :api-gateway:bootRun
```

## Authentication Flow

1. **User Registration:**
```http
POST /api/v1/auth/signup
{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "role": "USER"
}
```

2. **User Login:**
```http
POST /api/v1/auth/signin
{
    "email": "user@example.com",
    "password": "password123"
}
```

## Role-Based Access Control

- **ADMIN**
  - Full access to all endpoints
  - Can create ACTIVE users
  - Can perform DELETE operations

- **MANAGER**
  - Cannot perform DELETE operations
  - Can only create PENDING users
  - Access to management endpoints

- **USER**
  - Access to own records only
  - Can manage own profile
  - Can handle invitations

## TODO List

### High Priority
- [ ] Implement User Service
  - [ ] CRUD operations
  - [ ] User search endpoints
  - [ ] Organization membership handling

- [ ] Implement Organization Service
  - [ ] CRUD operations
  - [ ] Organization search
  - [ ] User membership management

- [ ] Implement Invitation Service
  - [ ] Invitation creation and management
  - [ ] Automatic expiration handling
  - [ ] Email notifications

### Medium Priority
- [ ] Add Input Validation
  - [ ] Request validation
  - [ ] Data sanitization
  - [ ] Error handling

- [ ] Implement Database Layer
  - [ ] Set up PostgreSQL
  - [ ] Create database schemas
  - [ ] Add indexing

### Low Priority
- [ ] Add Documentation
  - [ ] API documentation using OpenAPI/Swagger
  - [ ] Postman collection
  - [ ] Integration guides

- [ ] Add Testing
  - [ ] Unit tests
  - [ ] Integration tests
  - [ ] API tests

## Project Structure
```
.
├── api-gateway/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/digitopia/gateway/
│   │   └── resources/
│   └── build.gradle.kts
├── auth-service/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/digitopia/auth/
│   │   └── resources/
│   └── build.gradle.kts
├── eureka-server/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/digitopia/eureka/
│   │   └── resources/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Health Checks

Each service provides a health check endpoint at `/healtz`:
- Eureka Server: http://localhost:8761/healtz
- API Gateway: http://localhost:8080/healtz
- Auth Service: http://localhost:8083/healtz

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Submit a pull request

## License

This project is proprietary and confidential.