# Run all tests in Docker
Write-Host "🧪 Running Tests in Docker..." -ForegroundColor Cyan

# Start test databases and infrastructure
Write-Host "`n📦 Starting test databases..." -ForegroundColor Yellow
docker-compose -f docker-compose.test.yml up -d postgres-user-test postgres-org-test postgres-invitation-test kafka-test zookeeper-test

# Wait for services to be healthy
Write-Host "`n⏳ Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Run User Service tests
Write-Host "`n🔵 Running User Service Tests..." -ForegroundColor Blue
docker-compose -f docker-compose.test.yml run --rm user-service-test

# Run Organization Service tests
Write-Host "`n🟢 Running Organization Service Tests..." -ForegroundColor Green
docker-compose -f docker-compose.test.yml run --rm organization-service-test

# Run Invitation Service tests
Write-Host "`n🟡 Running Invitation Service Tests..." -ForegroundColor Magenta
docker-compose -f docker-compose.test.yml run --rm invitation-service-test

# Cleanup
Write-Host "`n🧹 Cleaning up..." -ForegroundColor Yellow
docker-compose -f docker-compose.test.yml down -v

Write-Host "`n✅ Test execution completed!" -ForegroundColor Green
