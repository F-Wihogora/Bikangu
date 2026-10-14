#!/bin/bash
# Bikangu - One-Command Startup Script
# Usage: ./start.sh

set -e

echo "🚀 Starting Bikangu FinTech Platform..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check Docker
echo -e "${BLUE}🔍 Checking Docker...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker Desktop first.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker is running${NC}"

# Check docker-compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ docker-compose not found. Please install Docker Compose.${NC}"
    exit 1
fi

# Check .env
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env not found. Creating from example...${NC}"
    cp .env.example .env
    echo -e "${YELLOW}⚠️  Please edit .env with your settings (JWT_SECRET, MAIL credentials, etc.)${NC}"
    echo "Then run this script again."
    exit 1
fi

echo -e "${GREEN}✅ Environment ready${NC}"

# Stop any existing containers
echo -e "${BLUE}🧹 Cleaning up old containers...${NC}"
docker-compose down --remove-orphans 2>/dev/null || true

# Start services
echo -e "${BLUE}🚀 Starting services...${NC}"
docker-compose up -d

# Wait for PostgreSQL
echo -e "${BLUE}⏳ Waiting for PostgreSQL...${NC}"
until docker exec bikangu-postgres pg_isready -U bikangu -d bikangu >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e "\n${GREEN}✅ PostgreSQL ready${NC}"

# Wait for Redis
echo -e "${BLUE}⏳ Waiting for Redis...${NC}"
until docker exec bikangu-redis redis-cli ping >/dev/null 2>&1; do
    echo -n "."
    sleep 1
done
echo -e "\n${GREEN}✅ Redis ready${NC}"

# Wait for backend
echo -e "${BLUE}⏳ Waiting for Backend API...${NC}"
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo -e "\n${GREEN}✅ Backend API ready${NC}"
        break
    fi
    echo -n "."
    sleep 2
    if [ $i -eq 60 ]; then
        echo -e "\n${RED}❌ Backend failed to start. Check logs: docker-compose logs backend${NC}"
        exit 1
    fi
done

# Wait for admin
echo -e "${BLUE}⏳ Waiting for Admin Dashboard...${NC}"
until curl -s http://localhost:3000 >/dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e "\n${GREEN}✅ Admin Dashboard ready${NC}"

echo ""
echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                    🎉 BIKANGU IS READY! 🎉                    ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📱 Access Points:${NC}"
echo -e "  ${GREEN}Admin Dashboard:${NC} http://localhost:3000"
echo -e "  ${GREEN}Backend API:${NC}     http://localhost:8080/api/v1"
echo -e "  ${GREEN}API Health:${NC}      http://localhost:8080/actuator/health"
echo -e "  ${GREEN}API Docs:${NC}        http://localhost:8080/swagger-ui.html"
echo -e "  ${GREEN}Mock Provider:${NC}   http://localhost:8081"
echo ""
echo -e "${BLUE}📱 Mobile App (Expo Go):${NC}"
echo -e "  ${BLUE}cd mobile && flutter run${NC}"
echo -e "  Then scan QR code with Expo Go app"
echo ""
echo -e "${YELLOW}🔐 First-time setup:${NC}"
echo -e "  1. Register: ${BLUE}curl -X POST http://localhost:8080/api/v1/auth/register \\${NC}"
echo -e "     ${BLUE}-H \"Content-Type: application/json\" \\${NC}"
echo -e "     ${BLUE}-d '{\"email\":\"admin@bikangu.com\",\"password\":\"SecurePass123!\",\"firstName\":\"Admin\",\"lastName\":\"User\"}'${NC}"
echo ""
echo -e "${YELLOW}🔐 Then promote to admin:${NC}"
echo -e "  ${BLUE}docker exec -it bikangu-postgres psql -U bikangu -d bikangu${NC}"
echo -e "  ${BLUE}UPDATE users SET is_active=true, is_email_verified=true WHERE email='admin@bikangu.com';${NC}"
echo -e "  ${BLUE}INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email='admin@bikangu.com' AND r.name='SUPER_ADMIN';${NC}"
echo ""
echo -e "${BLUE}📱 Mobile (Expo Go):${NC}"
echo -e "  ${BLUE}cd mobile && flutter run${NC}"
echo -e "  Then scan QR with Expo Go app"
echo ""
echo -e "${GREEN}Happy saving! 💰🚀${NC}"