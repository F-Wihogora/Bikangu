@echo off
REM Bikangu - One-Command Startup Script (Windows)
REM Usage: start.bat

@echo off
setlocal enabledelayedexpansion

echo.
echo ============================================================
echo   🚀 Starting Bikangu FinTech Platform...
echo ============================================================
echo.

REM Check Docker
echo [INFO] Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)
echo [OK] Docker is running

REM Check docker-compose
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] docker-compose not found. Please install Docker Compose.
    pause
    exit /b 1
)

REM Check .env
if not exist .env (
    echo [WARNING] .env not found. Creating from example...
    copy .env.example .env
    echo [WARNING] Please edit .env with your settings (JWT_SECRET, MAIL credentials, etc.)
    echo Then run this script again.
    pause
    exit /b 1
)

echo [OK] Environment ready

REM Stop any existing containers
echo [INFO] Cleaning up old containers...
docker-compose down --remove-orphans >nul 2>&1 || true

REM Start services
echo [INFO] Starting services...
docker-compose up -d

REM Wait for PostgreSQL
echo [INFO] Waiting for PostgreSQL...
:wait_postgres
docker exec bikangu-postgres pg_isready -U bikangu -d bikangu >nul 2>&1
if errorlevel 1 (
    echo.
    timeout /t 2 /nobreak >nul
    goto wait_postgres
)
echo [OK] PostgreSQL ready

REM Wait for Redis
echo [INFO] Waiting for Redis...
:wait_redis
docker exec bikangu-redis redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo.
    timeout /t 1 /nobreak >nul
    goto wait_redis
)
echo [OK] Redis ready

REM Wait for Backend
echo [INFO] Waiting for Backend API...
set ATTEMPTS=0
:wait_backend
set /a ATTEMPTS+=1
curl -s http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    if !%ATTEMPTS%==60 (
        echo.
        timeout /t 2 /nobreak >nul
        goto wait_backend
    )
    echo [ERROR] Backend failed to start. Check logs: docker-compose logs backend
    pause
    exit /b 1
)
echo.
echo [OK] Backend API ready

REM Wait for Admin
echo [INFO] Waiting for Admin Dashboard...
:wait_admin
curl -s http://localhost:3000 >nul 2>&1
if errorlevel 1 (
    echo.
    timeout /t 2 /nobreak >nul
    goto wait_admin
)
echo [OK] Admin Dashboard ready

echo.
echo ============================================================
echo   🎉 BIKANGU IS READY! 🎉
echo ============================================================
echo.
echo [ACCESS POINTS]
echo   Admin Dashboard:  http://localhost:3000
echo   Backend API:      http://localhost:8080/api/v1
echo   API Health:       http://localhost:8080/actuator/health
echo   API Docs:         http://localhost:8080/swagger-ui.html
echo   Mock Provider:    http://localhost:8081
echo.
echo FIRST-TIME SETUP:
echo   1. Register: curl -X POST http://localhost:8080/api/v1/auth/register -H "Content-Type: application/json" -d "{\"email\":\"admin@bikangu.com\",\"password\":\"SecurePass123!\",\"firstName\":\"Admin\",\"lastName\":\"User\"}"
echo.
echo   2. Promote to admin (in PostgreSQL):
echo   docker exec -it bikangu-postgres psql -U bikangu -d bikangu
echo   UPDATE users SET is_active=true, is_email_verified=true WHERE email='admin@bikangu.com';
echo   INSERT INTO user_roles (user_id, role_id) SELECT u.id, r.id FROM users u, roles r WHERE u.email='admin@bikangu.com' AND r.name='SUPER_ADMIN';
echo.
echo Mobile (Expo Go):
echo   cd mobile && flutter run
echo   Then scan QR with Expo Go app
echo.
echo Happy saving! 💰🚀
pause