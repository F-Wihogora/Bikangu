# Bikangu Deployment Guide

## Overview

This guide covers deploying Bikangu to production environments using Docker, Kubernetes, or traditional VMs.

---

## Prerequisites

### Infrastructure Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 2 vCPU | 4+ vCPU |
| RAM | 4 GB | 8+ GB |
| Storage | 50 GB SSD | 100+ GB SSD |
| Network | 1 Gbps | 10 Gbps |

### External Services
- **PostgreSQL 16+** (managed: AWS RDS, GCP Cloud SQL, Azure Database)
- **Redis 7+** (managed: AWS ElastiCache, GCP Memorystore)
- **Object Storage** (AWS S3, GCP Cloud Storage, MinIO)
- **Email** (SendGrid, Mailgun, AWS SES)
- **Push Notifications** (Firebase Cloud Messaging)
- **SSL Certificates** (Let's Encrypt, CloudFlare, AWS ACM)
- **DNS** (Route53, CloudFlare, Google Cloud DNS)

---

## Environment Configuration

### Required Environment Variables

```bash
# Database
DATABASE_URL=jdbc:postgresql://db-host:5432/bikangu
DATABASE_USERNAME=bikangu
DATABASE_PASSWORD=secure-password

# Redis
REDIS_HOST=redis-host
REDIS_PORT=6379
REDIS_PASSWORD=secure-password

# JWT (Generate with: openssl rand -base64 64)
JWT_SECRET=your-256-bit-secret-key-minimum
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Security
BCRYPT_COST=12
MAX_LOGIN_ATTEMPTS=5
LOCKOUT_DURATION_MINUTES=15

# Email
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key

# Firebase
FCM_SERVER_KEY=your-fcm-server-key
FCM_SENDER_ID=your-fcm-sender-id

# Webhook
WEBHOOK_SECRET=your-webhook-secret

# Frontend
FRONTEND_URL=https://app.bikangu.com
ADMIN_URL=https://admin.bikangu.com

# Provider Keys (per provider)
MTN_API_KEY=your-mtn-api-key
MTN_API_SECRET=your-mtn-api-secret
AIRTEL_CLIENT_ID=your-airtel-client-id
AIRTEL_CLIENT_SECRET=your-airtel-client-secret
```

### Kubernetes Secrets
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: bikangu-secrets
  namespace: bikangu
type: Opaque
stringData:
  DATABASE_PASSWORD: "secure-password"
  REDIS_PASSWORD: "secure-password"
  JWT_SECRET: "your-256-bit-secret"
  MAIL_PASSWORD: "sendgrid-api-key"
  FCM_SERVER_KEY: "fcm-key"
  WEBHOOK_SECRET: "webhook-secret"
  MTN_API_SECRET: "mtn-secret"
  AIRTEL_CLIENT_SECRET: "airtel-secret"
```

---

## Docker Deployment

### Build Images

```bash
# Backend
cd backend
docker build -t bikangu/backend:1.0.0 .

# Admin
cd admin
docker build -t bikangu/admin:1.0.0 .
```

### Docker Compose (Production)

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  backend:
    image: bikangu/backend:1.0.0
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
      restart_policy:
        condition: on-failure
        delay: 10s
        max_attempts: 3
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/bikangu
      - REDIS_HOST=redis
    networks:
      - bikangu-network
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  admin:
    image: bikangu/admin:1.0.0
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
    networks:
      - bikangu-network

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.prod.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - backend
      - admin
    networks:
      - bikangu-network

networks:
  bikangu-network:
    external: true
```

### Nginx Production Config
```nginx
# nginx.prod.conf
upstream backend {
    least_conn;
    server backend:8080 max_fails=3 fail_timeout=30s;
}

upstream admin {
    least_conn;
    server admin:80 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name api.bikangu.com admin.bikangu.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.bikangu.com;

    ssl_certificate /etc/nginx/ssl/api.bikangu.com.crt;
    ssl_certificate_key /etc/nginx/ssl/api.bikangu.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 90s;
        proxy_send_timeout 90s;
    }

    location /ws {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400;
    }
}

server {
    listen 443 ssl http2;
    server_name admin.bikangu.com;

    ssl_certificate /etc/nginx/ssl/admin.bikangu.com.crt;
    ssl_certificate_key /etc/nginx/ssl/admin.bikangu.com.key;

    location / {
        proxy_pass http://admin;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Deploy with Docker Compose
```bash
# Start production stack
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f backend

# Scale backend
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale backend=5

# Update images
docker-compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## Kubernetes Deployment

### Namespace
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: bikangu
  labels:
    name: bikangu
```

### ConfigMap
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: bikangu-config
  namespace: bikangu
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DATABASE_URL: "jdbc:postgresql://postgres:5432/bikangu"
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
  JWT_ACCESS_TOKEN_EXPIRATION: "900000"
  JWT_REFRESH_TOKEN_EXPIRATION: "604800000"
  BCRYPT_COST: "12"
  MAX_LOGIN_ATTEMPTS: "5"
  LOCKOUT_DURATION_MINUTES: "15"
  FRONTEND_URL: "https://app.bikangu.com"
  ADMIN_URL: "https://admin.bikangu.com"
  MOCK_PROVIDER_ENABLED: "false"
```

### Backend Deployment
```yaml
# k8s/backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bikangu-backend
  namespace: bikangu
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bikangu-backend
  template:
    metadata:
      labels:
        app: bikangu-backend
    spec:
      containers:
        - name: backend
          image: bikangu/backend:1.0.0
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: bikangu-config
            - secretRef:
                name: bikangu-secrets
          resources:
            requests:
              cpu: "1"
              memory: "1Gi"
            limits:
              cpu: "2"
              memory: "2Gi"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          securityContext:
            runAsNonRoot: true
            runAsUser: 1000
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop: ["ALL"]
      securityContext:
        fsGroup: 1000
```

### Backend Service
```yaml
# k8s/backend-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: bikangu-backend
  namespace: bikangu
spec:
  selector:
    app: bikangu-backend
  ports:
    - port: 8080
      targetPort: 8080
  type: ClusterIP
```

### Admin Deployment
```yaml
# k8s/admin-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bikangu-admin
  namespace: bikangu
spec:
  replicas: 2
  selector:
    matchLabels:
      app: bikangu-admin
  template:
    metadata:
      labels:
        app: bikangu-admin
    spec:
      containers:
        - name: admin
          image: bikangu/admin:1.0.0
          ports:
            - containerPort: 80
          resources:
            requests:
              cpu: "250m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
          securityContext:
            runAsNonRoot: true
            runAsUser: 101
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop: ["ALL"]
```

### Ingress
```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: bikangu-ingress
  namespace: bikangu
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "90"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "90"
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - api.bikangu.com
        - admin.bikangu.com
      secretName: bikangu-tls
  rules:
    - host: api.bikangu.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: bikangu-backend
                port:
                  number: 8080
    - host: admin.bikangu.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: bikangu-admin
                port:
                  number: 80
```

### Horizontal Pod Autoscaler
```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: bikangu-backend-hpa
  namespace: bikangu
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: bikangu-backend
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
        - type: Percent
          value: 100
          periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
```

### Apply Kubernetes Manifests
```bash
# Apply all manifests
kubectl apply -f k8s/

# Check status
kubectl get all -n bikangu

# View logs
kubectl logs -n bikangu -l app=bikangu-backend -f

# Scale manually
kubectl scale deployment bikangu-backend -n bikangu --replicas=5
```

---

## Database Setup

### PostgreSQL Configuration
```sql
-- Create database and user
CREATE DATABASE bikangu;
CREATE USER bikangu WITH ENCRYPTED PASSWORD 'secure-password';
GRANT ALL PRIVILEGES ON DATABASE bikangu TO bikangu;

-- Extensions
\c bikangu
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Performance settings (postgresql.conf)
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 16MB
maintenance_work_mem = 128MB
max_connections = 200
random_page_cost = 1.1
```

### Run Migrations
```bash
# Using Flyway (in backend container)
./gradlew flywayMigrate

# Or manually with Flyway CLI
flyway -url=jdbc:postgresql://host:5432/bikangu \
       -user=bikangu \
       -password=secure-password \
       -locations=filesystem:src/main/resources/db/migration \
       migrate
```

### Backup Strategy
```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U bikangu -d bikangu \
  --no-owner --no-acl --format=custom \
  > /backups/bikangu_${DATE}.dump

# Upload to S3
aws s3 cp /backups/bikangu_${DATE}.dump s3://bikangu-backups/database/

# Retain 30 days
find /backups -name "bikangu_*.dump" -mtime +30 -delete
```

---

## Monitoring Setup

### Prometheus Rules
```yaml
# prometheus/rules.yml
groups:
  - name: bikangu
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate on {{ $labels.instance }}"

      - alert: DatabaseConnectionsHigh
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Database connections at 80% capacity"

      - alert: SavingsEngineFailures
        expr: increase(savings_transaction_failed_total[15m]) > 10
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High savings transaction failure rate"

      - alert: ProviderDown
        expr: up{job="financial-providers"} == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Provider {{ $labels.provider }} is down"
```

### Grafana Dashboards
- **System Overview**: CPU, Memory, Disk, Network
- **Application Metrics**: Request rate, latency, errors
- **Business Metrics**: Savings volume, active users, transactions
- **Provider Health**: API latency, success rates, webhook processing
- **Security**: Failed logins, security events, blocked IPs

---

## CI/CD Pipeline

### GitHub Actions
```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]
  release:
    types: [published]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run backend tests
        run: |
          cd backend
          ./gradlew test
      
      - name: Run mobile tests
        run: |
          cd mobile
          flutter test

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Build backend image
        run: |
          cd backend
          docker build -t ${{ secrets.REGISTRY }}/bikangu/backend:${{ github.sha }} .
          docker push ${{ secrets.REGISTRY }}/bikangu/backend:${{ github.sha }}
      
      - name: Build admin image
        run: |
          cd admin
          docker build -t ${{ secrets.REGISTRY }}/bikangu/admin:${{ github.sha }} .
          docker push ${{ secrets.REGISTRY }}/bikangu/admin:${{ github.sha }}

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/bikangu-backend \
            backend=${{ secrets.REGISTRY }}/bikangu/backend:${{ github.sha }} \
            -n bikangu
          kubectl set image deployment/bikangu-admin \
            admin=${{ secrets.REGISTRY }}/bikangu/admin:${{ github.sha }} \
            -n bikangu
          kubectl rollout status deployment/bikangu-backend -n bikangu
          kubectl rollout status deployment/bikangu-admin -n bikangu
```

---

## SSL/TLS Certificates

### Let's Encrypt (Cert-Manager)
```yaml
# k8s/cert-manager.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@bikangu.com
    privateKeySecretRef:
      name: letsencrypt-prod-key
    solvers:
      - http01:
          ingress:
            class: nginx
```

### Manual Certificates
```bash
# Generate CSR
openssl req -new -newkey rsa:2048 -nodes \
  -keyout bikangu.key \
  -out bikangu.csr \
  -subj "/C=RW/ST=Kigali/L=Kigali/O=Bikangu/CN=api.bikangu.com"

# Submit to CA, then combine
cat bikangu.crt intermediate.crt > bikangu.fullchain.crt
```

---

## Rollback Procedures

### Application Rollback
```bash
# Kubernetes
kubectl rollout undo deployment/bikangu-backend -n bikangu
kubectl rollout undo deployment/bikangu-admin -n bikangu

# Docker Compose
docker-compose -f docker-compose.yml -f docker-compose.prod.yml \
  up -d --scale backend=3
# Then update image tag to previous version
```

### Database Rollback
```bash
# Restore from backup
pg_restore -h localhost -U bikangu -d bikangu \
  --clean --if-exists --no-owner \
  /backups/bikangu_20240115_120000.dump

# Or Flyway rollback (if supported)
./gradlew flywayUndo
```

---

## Disaster Recovery

### RTO/RPO Targets
| Tier | RTO | RPO |
|------|-----|-----|
| Database | 1 hour | 15 minutes |
| Application | 15 minutes | 0 (stateless) |
| File Storage | 4 hours | 1 hour |

### DR Checklist
- [ ] Cross-region database replication
- [ ] Automated backup verification
- [ ] Runbook documentation
- [ ] Quarterly DR drills
- [ ] Contact tree for incidents
- [ ] Stakeholder communication plan

---

## Security Hardening Checklist

- [ ] All secrets in Vault/Secrets Manager
- [ ] TLS 1.3 everywhere
- [ ] Certificate pinning on mobile
- [ ] WAF rules configured
- [ ] Rate limiting active
- [ ] Security headers present
- [ ] Database encrypted at rest
- [ ] Backups encrypted
- [ ] Audit logging enabled
- [ ] Vulnerability scanning in CI
- [ ] Dependency updates automated
- [ ] Penetration test scheduled

---

## Performance Tuning

### JVM Options
```bash
JAVA_OPTS="
  -Xms2g -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=100
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
  -Dfile.encoding=UTF-8
  -Djava.security.egd=file:/dev/./urandom
"
```

### Database Tuning
```sql
-- Indexes for common queries
CREATE INDEX CONCURRENTLY idx_transactions_user_created 
  ON transactions(user_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_income_transactions_account_received 
  ON income_transactions(financial_account_id, received_at DESC);

-- Partition large tables (monthly)
ALTER TABLE transactions SET (partition_strategy = 'monthly');
```

### Redis Tuning
```redis
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

---

## Maintenance Windows

### Scheduled Maintenance
- **Daily**: 02:00-03:00 UTC (low traffic)
- **Weekly**: Sunday 01:00-04:00 UTC
- **Monthly**: First Saturday 00:00-06:00 UTC

### Maintenance Tasks
| Frequency | Task |
|-----------|------|
| Daily | Log rotation, backup verification |
| Weekly | Security patch review, dependency updates |
| Monthly | Database vacuum, index rebuild, capacity planning |
| Quarterly | DR drill, penetration test, certificate renewal |

---

## Support Contacts

| Role | Contact | Escalation |
|------|---------|------------|
| DevOps | devops@bikangu.com | 15 min |
| Backend Lead | backend-lead@bikangu.com | 30 min |
| Security | security@bikangu.com | 1 hour |
| Management | cto@bikangu.com | 2 hours |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-01-15 | Initial deployment guide |