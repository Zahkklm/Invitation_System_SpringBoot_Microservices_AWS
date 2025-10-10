# Health Endpoints Configuration

All microservices now use **Spring Boot Actuator** for health monitoring instead of custom `/healtz` endpoints.

## Health Endpoint URLs

When running locally:
- **Eureka Server**: http://localhost:8761/actuator/health
- **API Gateway**: http://localhost:8080/actuator/health
- **User Service**: http://localhost:8084/actuator/health
- **Organization Service**: http://localhost:8082/actuator/health
- **Invitation Service**: http://localhost:8085/actuator/health

## Configuration

All services have the following configuration in their `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

**Note**: API Gateway also exposes the `gateway` endpoint for additional monitoring.

## Dependencies

All services include the Spring Boot Actuator dependency:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

## Docker Health Checks

The `docker-compose.yml` uses `/actuator/health` for health checks:

```yaml
healthcheck:
  test: "curl --fail --silent localhost:8761/actuator/health | grep UP || exit 1"
  interval: 1s
  timeout: 5s
  retries: 5
  start_period: 40s
```

## Testing

After rebuilding and starting the services, test with:

```bash
# Test all services
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8085/actuator/health
```

Expected response format:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1000000000,
        "free": 500000000,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## Additional Actuator Endpoints

You can also access:
- `/actuator/info` - Application information
- `/actuator/` - List all available endpoints

For API Gateway specifically:
- `/actuator/gateway/routes` - View all configured routes
