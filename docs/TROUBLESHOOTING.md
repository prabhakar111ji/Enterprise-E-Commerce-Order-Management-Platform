# Troubleshooting Guide

## Common Issues

### 1. Backend won't start — "Connection refused to PostgreSQL"
**Cause:** PostgreSQL is not running or not ready yet.
**Fix:**
```bash
# If using Docker Compose
docker compose up postgres -d
# Wait 10 seconds
docker compose up backend

# If running locally
# Ensure PostgreSQL is installed and running on port 5432
# Create database: createdb ecommerce_db
```

### 2. "Table 'users' already exists" error on startup
**Cause:** `data.sql` tries to insert duplicate data.
**Fix:** Already handled with `ON CONFLICT DO NOTHING`. If still failing:
```yaml
# application.yml
spring.sql.init.mode: never  # Disable auto-seed after first run
```

### 3. Redis connection refused
**Cause:** Redis is not running.
**Fix:**
```bash
docker compose up redis -d
# Or install Redis locally
```
**Note:** The application works WITHOUT Redis — it just doesn't cache. You'll see warnings in logs but no crashes.

### 4. Kafka connection error
**Cause:** Kafka is not running or not ready.
**Fix:**
```bash
docker compose up kafka -d
# Wait 30 seconds (Kafka takes time to start in KRaft mode)
```
**Note:** Orders still work without Kafka. The event publish fails silently (logged as error), but the order is saved.

### 5. Frontend shows "Network Error"
**Cause:** Backend is not running, or CORS is misconfigured.
**Fix:**
- Verify backend is running: `curl http://localhost:8080/api/products`
- If using Vite dev server, proxy is configured in `vite.config.js`
- If using Docker, nginx proxies `/api/` to `backend:8080`

### 6. "403 Forbidden" when accessing admin endpoints
**Cause:** You're logged in as USER, not ADMIN.
**Fix:** Login with admin credentials: `admin@ecommerce.com` / `admin123`

### 7. "401 Unauthorized" on all requests
**Cause:** JWT token is expired or invalid.
**Fix:**
- Login again to get a new token
- Check if token is being sent: browser DevTools → Network → Headers → Authorization
- Default expiry is 24 hours

### 8. BCrypt password mismatch on seed data
**Cause:** The BCrypt hashes in `data.sql` don't match the passwords.
**Fix:** Generate new hashes:
```java
System.out.println(new BCryptPasswordEncoder().encode("admin123"));
```
Replace the hash in `data.sql`.

### 9. Maven build fails — "Java 21 not found"
**Fix:**
```bash
java -version  # Must show 21
# Set JAVA_HOME to JDK 21
export JAVA_HOME=/path/to/jdk-21
```

### 10. Docker Compose build fails — "npm not found" in frontend
**Cause:** Docker isn't using the correct base image.
**Fix:** Ensure `frontend/Dockerfile` uses `node:20-alpine` as base image.

### 11. "Port already in use"
**Fix:**
```bash
# Find and kill process on port
# Windows:
netstat -aon | findstr :8080
taskkill /PID <pid> /F

# Or change port in application.yml / docker-compose.yml
```

### 12. Kafka consumer not receiving messages
**Cause:** Consumer group offset, or topic not created.
**Fix:**
```bash
# Check topic exists
docker exec ecommerce-kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Check consumer group
docker exec ecommerce-kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

## Useful Commands

```bash
# Start all services
docker compose up --build

# Start only infrastructure (for local dev)
docker compose up postgres redis kafka -d

# View backend logs
docker compose logs -f backend

# Access PostgreSQL
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce_db

# Access Redis CLI
docker exec -it ecommerce-redis redis-cli

# Run tests
cd backend && ./mvnw test

# Build without tests
cd backend && ./mvnw package -DskipTests
```
