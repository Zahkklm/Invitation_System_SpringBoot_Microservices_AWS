# 🚀 Quick Start Guide - Event-Driven Microservices

## Start the System
```bash
docker-compose up --build
```

## Health Checks
```bash
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8084/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Organization Service
curl http://localhost:8085/actuator/health  # Invitation Service
```

## Test Event-Driven Flow

### 1. Create User
```bash
curl -X POST http://localhost:8084/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{"email":"test@test.com","fullName":"Test User","role":"USER"}'
```

### 2. Create Organization
```bash
curl -X POST http://localhost:8082/api/v1/organizations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{"name":"Test Org","registryNumber":"REG123","contactEmail":"org@test.com","companySize":50,"yearFounded":2023}'
```

### 3. Create Invitation
```bash
curl -X POST http://localhost:8085/api/v1/invitations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{"userId":"<USER_ID>","organizationId":"<ORG_ID>","message":"Join us!"}'
```

### 4. Accept Invitation (Triggers Kafka Event!)
```bash
curl -X PUT http://localhost:8085/api/v1/invitations/<INVITATION_ID>/status \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <USER_ID>" \
  -d '{"status":"ACCEPTED"}'
```

### 5. Check User's Organizations (Auto-updated via Kafka!)
```bash
curl http://localhost:8084/api/v1/users/<USER_ID>/organizations
# Should return: ["<ORG_ID>"]
```

## Monitor Kafka Events
```bash
# List topics
docker exec -it $(docker ps -qf "name=kafka") \
  kafka-topics --list --bootstrap-server localhost:9092

# Watch invitation events
docker exec -it $(docker ps -qf "name=kafka") \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic invitation-events \
  --from-beginning

# Watch user events
docker exec -it $(docker ps -qf "name=kafka") \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

## Stop the System
```bash
docker-compose down
```

## Key Features Implemented
✅ Event-Driven Architecture with Kafka  
✅ Automatic user-organization sync  
✅ GET /users/{id}/organizations endpoint  
✅ Scheduled invitation expiration  
✅ Spring Boot Actuator health checks  
✅ Service discovery with Eureka  
✅ API Gateway with Cognito JWT  
✅ Complete audit trail via events  

## Architecture
```
Invitation Service → Kafka → User Service
     (publishes)         (consumes & updates)
```

When invitation is accepted:
1. Invitation Service publishes `InvitationAcceptedEvent`
2. Kafka stores event in `invitation-events` topic
3. User Service consumes event automatically
4. User Service adds organization to user's list
5. User Service publishes `UserOrganizationAddedEvent` for audit

**Result**: Fully decoupled, event-driven microservices! 🎉
