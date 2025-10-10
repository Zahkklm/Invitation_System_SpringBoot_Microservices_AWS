#!/bin/bash

# Run all tests in Docker
echo "🧪 Running Tests in Docker..."

# Start test databases and infrastructure
echo ""
echo "📦 Starting test databases..."
docker-compose -f docker-compose.test.yml up -d postgres-user-test postgres-org-test postgres-invitation-test kafka-test zookeeper-test

# Wait for services to be healthy
echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Run User Service tests
echo ""
echo "🔵 Running User Service Tests..."
docker-compose -f docker-compose.test.yml run --rm user-service-test

# Run Organization Service tests
echo ""
echo "🟢 Running Organization Service Tests..."
docker-compose -f docker-compose.test.yml run --rm organization-service-test

# Run Invitation Service tests
echo ""
echo "🟡 Running Invitation Service Tests..."
docker-compose -f docker-compose.test.yml run --rm invitation-service-test

# Cleanup
echo ""
echo "🧹 Cleaning up..."
docker-compose -f docker-compose.test.yml down -v

echo ""
echo "✅ Test execution completed!"
