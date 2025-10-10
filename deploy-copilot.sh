#!/bin/bash
set -e

echo "🚀 AWS Copilot Quick Deploy"
echo "============================"
echo ""

# Check if Copilot is installed
if ! command -v copilot &> /dev/null; then
    echo "📥 Installing AWS Copilot..."
    curl -Lo copilot https://github.com/aws/copilot-cli/releases/latest/download/copilot-linux
    chmod +x copilot
    sudo mv copilot /usr/local/bin/copilot
    echo "✅ Copilot installed!"
else
    echo "✅ Copilot already installed"
fi

echo ""
echo "🏗️  Initializing Digitopia application..."

# Initialize app (only if not already initialized)
if [ ! -d "copilot" ]; then
    copilot app init digitopia
    echo "✅ Application initialized"
else
    echo "✅ Application already initialized"
fi

echo ""
echo "🌍 Creating production environment..."
# Check if environment exists
if ! copilot env ls 2>/dev/null | grep -q "production"; then
    copilot env init \
        --name production \
        --profile default \
        --default-config
    copilot env deploy --name production
    echo "✅ Environment created"
else
    echo "✅ Environment already exists"
fi

echo ""
echo "📦 Initializing services..."

# Function to initialize and deploy a service
init_and_deploy_service() {
    local service_name=$1
    local service_type=$2
    local dockerfile=$3
    
    echo ""
    echo "🔧 Setting up $service_name..."
    
    # Check if service manifest exists
    if [ ! -f "copilot/$service_name/manifest.yml" ]; then
        copilot svc init \
            --name $service_name \
            --svc-type "$service_type" \
            --dockerfile $dockerfile
        echo "✅ $service_name initialized"
    else
        echo "✅ $service_name already initialized"
    fi
}

# Initialize all services
init_and_deploy_service "eureka-server" "Load Balanced Web Service" "eureka-server/Dockerfile"
init_and_deploy_service "api-gateway" "Load Balanced Web Service" "api-gateway/Dockerfile"
init_and_deploy_service "user-service" "Backend Service" "user-service/Dockerfile"
init_and_deploy_service "organization-service" "Backend Service" "organization-service/Dockerfile"
init_and_deploy_service "invitation-service" "Backend Service" "invitation-service/Dockerfile"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Services initialized!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Next Steps:"
echo ""
echo "1️⃣  Deploy Eureka Server (service discovery):"
echo "   copilot svc deploy --name eureka-server"
echo ""
echo "2️⃣  Deploy API Gateway:"
echo "   copilot svc deploy --name api-gateway"
echo ""
echo "3️⃣  Deploy Backend Services:"
echo "   copilot svc deploy --name user-service"
echo "   copilot svc deploy --name organization-service"
echo "   copilot svc deploy --name invitation-service"
echo ""
echo "🚀 Or deploy all at once:"
echo "   copilot svc deploy --all"
echo ""
echo "📊 View deployment status:"
echo "   copilot svc status"
echo ""
echo "🌐 Get service URL:"
echo "   copilot svc show --name api-gateway"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 Tip: Copilot will automatically create:"
echo "   - ECS Cluster with Fargate"
echo "   - Application Load Balancer"
echo "   - Service Discovery (Cloud Map)"
echo "   - CloudWatch Logs"
echo "   - Auto-scaling policies"
echo ""
echo "📚 Documentation: https://aws.github.io/copilot-cli/"
echo ""
