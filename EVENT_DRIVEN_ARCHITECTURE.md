# Event-Driven Architecture Implementation

## 🎯 Overview

This project implements **Event-Driven Architecture (EDA)** using **Apache Kafka** for asynchronous, loosely-coupled communication between microservices.

---

## 🏗️ Architecture

### Event Flow Diagram

```
┌─────────────────┐       ┌──────────────┐       ┌──────────────┐
│   Invitation    │──────>│    Kafka     │──────>│     User     │
│    Service      │ event │   Topics     │ event │   Service    │
└─────────────────┘       └──────────────┘       └──────────────┘
        │                                               │
        │ publishes                           consumes │
        ▼                                               ▼
 InvitationAcceptedEvent            Updates user.organizationIds
```

### Key Components

1. **Kafka (Message Broker)**
   - Distributed event streaming platform
   - Topics: `invitation-events`, `user-events`
   - Runs on port `9092` (external), `29092` (internal)

2. **Event Publishers**
   - `UserEventPublisher` - Publishes user domain events
   - `InvitationEventPublisher` - Publishes invitation domain events

3. **Event Listeners**
   - `UserEventListener` - Consumes invitation events
   - Updates user-organization relationships automatically

---

## 📨 Domain Events

### Invitation Events (Published by Invitation Service)

#### `InvitationCreatedEvent`
```kotlin
data class InvitationCreatedEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val message: String,
    val createdBy: UUID
)
```
**Triggers**: When a new invitation is created  
**Purpose**: Audit trail, email notifications

---

#### `InvitationAcceptedEvent` ⭐
```kotlin
data class InvitationAcceptedEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val acceptedBy: UUID
)
```
**Triggers**: When user accepts an invitation  
**Purpose**: **Automatically adds organization to user's organization list**  
**Consumer**: `UserEventListener` in User Service

---

#### `InvitationRejectedEvent`
```kotlin
data class InvitationRejectedEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val rejectedBy: UUID
)
```
**Triggers**: When user rejects an invitation  
**Purpose**: Audit trail, analytics

---

#### `InvitationExpiredEvent`
```kotlin
data class InvitationExpiredEvent(
    val invitationId: UUID,
    val userId: UUID,
    val organizationId: UUID
)
```
**Triggers**: Scheduled job marks old invitations as expired  
**Purpose**: Audit trail, cleanup notifications

---

### User Events (Published by User Service)

#### `UserCreatedEvent`
```kotlin
data class UserCreatedEvent(
    val userId: UUID,
    val email: String,
    val fullName: String,
    val role: String,
    val createdBy: UUID
)
```
**Triggers**: When a new user is created  
**Purpose**: Audit trail, welcome emails, analytics

---

#### `UserUpdatedEvent`
```kotlin
data class UserUpdatedEvent(
    val userId: UUID,
    updatedFields: Map<String, Any>,
    val updatedBy: UUID
)
```
**Triggers**: When user information is updated  
**Purpose**: Audit trail, change tracking

---

#### `UserOrganizationAddedEvent`
```kotlin
data class UserOrganizationAddedEvent(
    val userId: UUID,
    val organizationId: UUID,
    val addedBy: UUID
)
```
**Triggers**: When organization is added to user (via invitation acceptance)  
**Purpose**: Audit trail, analytics, notifications

---

##🔄 Event Processing Flow

### Scenario: User Accepts Invitation

```
1. User clicks "Accept" on invitation
   │
   ▼
2. Invitation Service:
   - Updates invitation.status = ACCEPTED
   - Publishes InvitationAcceptedEvent to Kafka
   │
   ▼
3. Kafka:
   - Stores event in 'invitation-events' topic
   - Maintains ordering and durability
   │
   ▼
4. User Service:
   - UserEventListener consumes event
   - Finds user by userId
   - Adds organizationId to user.organizationIds
   - Saves user
   - Publishes UserOrganizationAddedEvent
   │
   ▼
5. Result: User is now a member of the organization!
```

---

## 🔧 Configuration

### Docker Compose

```yaml
# Kafka Infrastructure
zookeeper:
  image: confluentinc/cp-zookeeper:7.5.0
  ports: ["2181:2181"]

kafka:
  image: confluentinc/cp-kafka:7.5.0
  ports: ["9092:9092"]  # External access
  environment:
    KAFKA_ADVERTISED_LISTENERS: 
      - PLAINTEXT://kafka:29092      # Internal (container-to-container)
      - PLAINTEXT_HOST://localhost:9092  # External (host access)
```

### Application Configuration

**user-service/application.yml**:
```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: user-service-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

**invitation-service/application.yml**:
```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

---

## 🎯 Benefits of Event-Driven Architecture

### 1. **Loose Coupling**
- Services don't need direct HTTP calls
- Invitation Service doesn't need to know about User Service API
- Services can evolve independently

### 2. **Eventual Consistency**
- User-organization relationship is updated asynchronously
- System remains responsive even if User Service is temporarily down
- Events are persisted and replayed when service comes back

### 3. **Audit Trail**
- Every action emits an event
- Complete history of what happened and when
- Can build audit log service by consuming all events

### 4. **Scalability**
- Kafka can handle millions of events per second
- Multiple consumers can process events in parallel
- Easy to add new event consumers without modifying publishers

### 5. **Fault Tolerance**
- Kafka persists events to disk
- Events are not lost if consumer is down
- Automatic retry mechanism for failed processing

### 6. **Asynchronous Processing**
- Non-blocking operations
- Faster API response times
- Background processing without user waiting

---

## 📊 API Endpoints Enhanced with Events

### POST /api/v1/invitations
**Creates invitation + Publishes `InvitationCreatedEvent`**

```bash
curl -X POST http://localhost:8085/api/v1/invitations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <creator-uuid>" \
  -d '{
    "userId": "<user-uuid>",
    "organizationId": "<org-uuid>",
    "message": "Join our organization!"
  }'
```

---

### PUT /api/v1/invitations/{id}/status
**Updates status + Publishes `InvitationAcceptedEvent` or `InvitationRejectedEvent`**

```bash
curl -X PUT http://localhost:8085/api/v1/invitations/<invitation-id>/status \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <user-uuid>" \
  -d '{
    "status": "ACCEPTED"
  }'
```

**Result**: User Service automatically receives event and adds organization to user!

---

### GET /api/v1/users/{id}/organizations ⭐ NEW!
**Returns all organizations a user belongs to**

```bash
curl http://localhost:8084/api/v1/users/<user-id>/organizations
```

**Response**:
```json
[
  "550e8400-e29b-41d4-a716-446655440001",
  "550e8400-e29b-41d4-a716-446655440002"
]
```

---

## 🧪 Testing Event-Driven Flow

### Step 1: Create User
```bash
curl -X POST http://localhost:8084/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }'
```

### Step 2: Create Organization
```bash
curl -X POST http://localhost:8082/api/v1/organizations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "name": "Acme Corp",
    "registryNumber": "REG123",
    "contactEmail": "contact@acme.com",
    "companySize": 100,
    "yearFounded": 2020
  }'
```

### Step 3: Send Invitation
```bash
curl -X POST http://localhost:8085/api/v1/invitations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "userId": "<john-user-id>",
    "organizationId": "<acme-org-id>",
    "message": "Join Acme Corp!"
  }'
```

### Step 4: Accept Invitation
```bash
curl -X PUT http://localhost:8085/api/v1/invitations/<invitation-id>/status \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <john-user-id>" \
  -d '{"status": "ACCEPTED"}'
```

### Step 5: Verify User's Organizations
```bash
curl http://localhost:8084/api/v1/users/<john-user-id>/organizations
```

**Expected**: Should return `[<acme-org-id>]` 🎉

---

## 🔍 Monitoring Events

### View Kafka Topics
```bash
docker exec -it <kafka-container-id> kafka-topics --list --bootstrap-server localhost:9092
```

### Consume Events (Debug)
```bash
# Listen to invitation events
docker exec -it <kafka-container-id> \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic invitation-events \
  --from-beginning

# Listen to user events
docker exec -it <kafka-container-id> \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

---

## 🚀 Running the System

### Start All Services
```bash
docker-compose up --build
```

**Services Started**:
- Zookeeper (port 2181)
- Kafka (port 9092)
- PostgreSQL × 3
- Eureka Server
- API Gateway
- User Service (with Kafka consumer)
- Organization Service
- Invitation Service (with Kafka producer)

---

## 📈 Future Enhancements

### 1. Dead Letter Queue (DLQ)
- Route failed events to DLQ topic
- Manual retry or investigation

### 2. Event Sourcing
- Store all events as source of truth
- Rebuild state by replaying events

### 3. CQRS (Command Query Responsibility Segregation)
- Separate read and write models
- Optimized queries using event-driven projections

### 4. Saga Pattern
- Distributed transactions across services
- Compensating transactions for rollbacks

### 5. Event Replay
- Replay historical events for analytics
- Rebuild projections or caches

---

## 📚 Technologies Used

- **Apache Kafka 7.5.0** - Distributed event streaming
- **Spring Kafka** - Kafka integration for Spring Boot
- **Spring Cloud Stream** - Event-driven microservices
- **Docker Compose** - Container orchestration

---

## ✅ Compliance with Case Study

This implementation satisfies the **optional requirement**:

> "Event Driven Architecture & Queues"

**What we implemented**:
✅ Kafka message broker  
✅ Event publishers in both services  
✅ Event consumers for cross-service communication  
✅ Asynchronous invitation acceptance flow  
✅ Eventual consistency between services  
✅ Audit trail through domain events  

**Benefits**:
- Loose coupling between services
- Scalable architecture
- Fault-tolerant communication
- Complete audit trail
- Non-blocking operations
