# Data Models

This page contains the database schemas and entity relationships for all microservices.

## User Service

### User Entity
- id (UUID, Primary Key)
- email (String, unique, indexed)
- fullName (String)
- role (Enum: USER, ADMIN)
- createdAt (Timestamp)
- updatedAt (Timestamp)

## Organization Service

### Organization Entity
- id (UUID, Primary Key)
- name (String)
- registryNumber (String, unique)
- contactEmail (String)
- companySize (Integer)
- yearFounded (Integer)
- createdAt (Timestamp)
- updatedAt (Timestamp)

## Invitation Service

### Invitation Entity
- id (UUID, Primary Key)
- userId (UUID, Foreign Key)
- organizationId (UUID, Foreign Key)
- status (Enum: PENDING, ACCEPTED, REJECTED)
- message (String)
- createdAt (Timestamp)
- updatedAt (Timestamp)

## Database Design

Each microservice has its own PostgreSQL database:
- user_service_db - User management
- organization_service_db - Organization data
- invitation_service_db - Invitation data

**Flyway** is used for database migrations across all services.
