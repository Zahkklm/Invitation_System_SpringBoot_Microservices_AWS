# 🧪 Testing Guide

## Running Tests in Docker

### Quick Start

**Windows (PowerShell):**
```powershell
.\run-tests.ps1
```

**Linux/Mac (Bash):**
```bash
chmod +x run-tests.sh
./run-tests.sh
```

---

## Manual Test Execution

### 1. Start Test Infrastructure

```bash
docker-compose -f docker-compose.test.yml up -d postgres-user-test postgres-org-test postgres-invitation-test kafka-test zookeeper-test
```

Wait for services to be healthy (~10 seconds).

### 2. Run Tests for Each Service

**User Service:**
```bash
docker-compose -f docker-compose.test.yml run --rm user-service-test
```

**Organization Service:**
```bash
docker-compose -f docker-compose.test.yml run --rm organization-service-test
```

**Invitation Service:**
```bash
docker-compose -f docker-compose.test.yml run --rm invitation-service-test
```

### 3. Cleanup

```bash
docker-compose -f docker-compose.test.yml down -v
```

---

## Running Tests Locally (Without Docker)

### Prerequisites
- JDK 17+
- PostgreSQL running on ports 5434, 5435, 5436
- Kafka running on port 9093

### Run All Tests
```bash
./gradlew test
```

### Run Specific Service Tests
```bash
./gradlew :user-service:test
./gradlew :organization-service:test
./gradlew :invitation-service:test
```

### Run Specific Test Class
```bash
./gradlew :user-service:test --tests "UserRepositoryTest"
./gradlew :user-service:test --tests "UserServiceTest"
```

---

## Test Structure

### User Service Tests
- ✅ `UserRepositoryTest` - Database operations
- ✅ `UserServiceTest` - Business logic
- ⏳ `UserControllerTest` - API endpoints (TODO)

### Organization Service Tests
- ✅ `OrganizationRepositoryTest` - Database operations
- ✅ `OrganizationServiceTest` - Business logic
- ⏳ `OrganizationControllerTest` - API endpoints (TODO)

### Invitation Service Tests
- ✅ `InvitationRepositoryTest` - Database operations
- ✅ `InvitationServiceTest` - Business logic
- ⏳ `InvitationControllerTest` - API endpoints (TODO)

---

## Test Databases

The test environment uses separate databases on different ports:

| Service | Database | Port | Credentials |
|---------|----------|------|-------------|
| User Service | user_service_test_db | 5434 | test_user / test_pass |
| Organization Service | organization_service_test_db | 5435 | test_user / test_pass |
| Invitation Service | invitation_service_test_db | 5436 | test_user / test_pass |
| Kafka (Test) | - | 9093 | - |

---

## Troubleshooting

### Tests Fail with Connection Refused

**Problem:** Test databases not ready
**Solution:** Wait longer or check container status
```bash
docker-compose -f docker-compose.test.yml ps
docker-compose -f docker-compose.test.yml logs postgres-user-test
```

### Port Already in Use

**Problem:** Test ports (5434-5436, 9093) already taken
**Solution:** Stop conflicting services or change ports in `docker-compose.test.yml`

### Gradle Build Fails

**Problem:** Dependencies not downloaded
**Solution:** Clean and rebuild
```bash
./gradlew clean build
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
```

---

## What's Tested

### Repository Tests
- ✅ CRUD operations
- ✅ Custom queries
- ✅ Unique constraints
- ✅ Indexes

### Service Tests
- ✅ Business logic
- ✅ Validation
- ✅ Exception handling
- ✅ Event publishing
- ✅ Soft/hard deletes

### Integration Tests (TODO)
- ⏳ API endpoints
- ⏳ End-to-end flows
- ⏳ Kafka event processing

---

## Test Coverage Goals

- **Unit Tests:** 70%+ coverage
- **Integration Tests:** Key workflows covered
- **API Tests:** All endpoints tested

---

## Next Steps

1. Add Controller/API tests
2. Add Kafka integration tests
3. Add performance tests
4. Set up continuous testing in CI/CD
