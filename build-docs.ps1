# Build Documentation Site Script

Write-Host "Building MkDocs documentation site..." -ForegroundColor Cyan
Write-Host ""

# Copy markdown files to docs folder
Write-Host "Organizing documentation files..." -ForegroundColor Yellow

# Getting Started
Copy-Item "QUICK_START.md" "docs\getting-started\quick-start.md" -Force
Copy-Item "SIMPLE_START.md" "docs\getting-started\simple-start.md" -Force
Copy-Item "TESTING.md" "docs\getting-started\testing.md" -Force

# Architecture
Copy-Item "README.md" "docs\architecture\overview.md" -Force
Copy-Item "EVENT_DRIVEN_ARCHITECTURE.md" "docs\architecture\event-driven.md" -Force
Copy-Item "HEALTH_ENDPOINTS.md" "docs\architecture\health-endpoints.md" -Force

# Create data models page from README
Write-Host "   Creating data-models.md..." -ForegroundColor Gray
@"
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
- `user_service_db` - User management
- `organization_service_db` - Organization data
- `invitation_service_db` - Invitation data

**Flyway** is used for database migrations across all services.
"@ | Out-File "docs\architecture\data-models.md" -Encoding UTF8

# AWS Deployment
Copy-Item "AWS_DEPLOYMENT_COMPARISON.md" "docs\aws\deployment-comparison.md" -Force
Copy-Item "DEPLOYMENT_REALITY_CHECK.md" "docs\aws\deployment-reality.md" -Force
Copy-Item "AWS_SETUP_GUIDE.md" "docs\aws\setup-guide.md" -Force
Copy-Item "AWS_ECS_FARGATE_DEPLOYMENT.md" "docs\aws\ecs-fargate.md" -Force
Copy-Item "AWS_COPILOT_ISSUES.md" "docs\aws\copilot-issues.md" -Force
Copy-Item "COST_CALCULATOR.md" "docs\aws\cost-calculator.md" -Force
Copy-Item "COST_QUICK_REFERENCE.md" "docs\aws\cost-reference.md" -Force

# Implementation
Copy-Item "FINAL_IMPLEMENTATION_SUMMARY.md" "docs\implementation\final-summary.md" -Force
Copy-Item "TESTING.md" "docs\implementation\testing.md" -Force

Write-Host "✅ Documentation files organized!" -ForegroundColor Green
Write-Host ""

# Build the site
Write-Host "Building static site..." -ForegroundColor Yellow
mkdocs build

Write-Host ""
Write-Host "✅ Documentation site built successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Documentation is ready!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To view locally:" -ForegroundColor Yellow
Write-Host "   mkdocs serve" -ForegroundColor White
Write-Host "   Then open: http://127.0.0.1:8000" -ForegroundColor Gray
Write-Host ""
Write-Host "To deploy to GitHub Pages:" -ForegroundColor Yellow
Write-Host "   mkdocs gh-deploy" -ForegroundColor White
Write-Host ""
Write-Host "Files are in 'site/' folder" -ForegroundColor Gray
Write-Host ""
