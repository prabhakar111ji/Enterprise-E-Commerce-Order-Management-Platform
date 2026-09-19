# Interview Guide — E-Commerce Platform

## A. 2-Line Project Introduction

> I built a full-stack e-commerce platform using **Java 21, Spring Boot, React, PostgreSQL, Redis, and Kafka**. It features JWT authentication, product management, shopping cart with Redis caching, transactional order processing with Kafka-based notifications, and is fully containerized with Docker.

## B. 30-Second Explanation

> This is a full-stack e-commerce application where users can browse products, add them to a shopping cart, and place orders. The backend is built with Spring Boot 3 and Java 21, using PostgreSQL for persistence, Redis for cart caching, and Kafka for asynchronous order notifications. Authentication is handled with JWT tokens and Spring Security with role-based access control. The frontend is a React SPA, and the entire application is containerized using Docker Compose.

## C. 1-Minute Explanation

> I built an e-commerce platform as a modular monolith using Spring Boot 3 and Java 21. The application has two user roles — regular users who can browse products, manage a shopping cart, and place orders, and admins who manage products and order statuses.
>
> For the backend, I used a clean layered architecture: Controllers handle HTTP requests, Services contain business logic, and Repositories manage data access through JPA/Hibernate with PostgreSQL.
>
> Security is implemented using Spring Security with JWT tokens. When a user logs in, the server generates a JWT containing their identity and role. This token is sent with every subsequent request and validated by a custom filter.
>
> I used Redis to cache product listings and cart data, reducing database load for frequently accessed data. For order processing, the entire checkout flow — inventory validation, order creation, stock reduction, and cart clearing — happens in a single database transaction to ensure consistency. After a successful order, a Kafka event is published to an order-events topic, which a consumer picks up to handle notifications asynchronously.
>
> The frontend is built with React and Vite, using Axios with interceptors for automatic JWT attachment. Everything runs in Docker Compose — PostgreSQL, Redis, Kafka, the backend, and the frontend.

## D. 3-Minute Detailed Explanation

> *(Include everything from the 1-minute version, plus:)*
>
> **Database Design:** I have 7 tables with proper relationships — Users have a one-to-one Cart, a Cart has many CartItems, each referencing a Product. Orders have a many-to-one relationship with Users, and contain OrderItems that capture the product price at purchase time. Products have a one-to-one Inventory tracking stock.
>
> **Order Transaction:** The `placeOrder()` method is annotated with `@Transactional`. It first validates the cart isn't empty, then checks inventory for ALL items before creating anything. It creates the order and order items, reduces stock, simulates payment, marks the order as CONFIRMED, and clears the cart. If any step fails, the entire transaction rolls back — so we never end up with an order but unreduced stock, or reduced stock but no order.
>
> **Kafka:** After the transaction commits, we publish an `OrderEvent` to the `order-events` topic. A consumer listens on this topic and logs a notification. In production, this would send emails or push notifications. The Kafka publish is wrapped in a try-catch — so even if Kafka is down, the order succeeds. The event can be replayed later.
>
> **Redis:** I use Spring's `@Cacheable` annotation on product and cart reads. When a product list is requested, Redis serves it if cached. Any write operation (`@CacheEvict`) invalidates the cache. If Redis goes down, the application falls back to PostgreSQL — the cache annotations handle failures gracefully.
>
> **Testing:** I wrote unit tests using JUnit 5 and Mockito for all service classes — AuthService, ProductService, CartService, and OrderService. I also have an integration test for the ProductController using MockMvc. Tests verify core business logic like registration with duplicate email handling, order placement with inventory checks, and order cancellation with stock restoration.
>
> **Docker:** The application uses Docker Compose with 5 services. Kafka runs in KRaft mode — no Zookeeper needed. The backend Dockerfile uses a multi-stage build: Maven builds the JAR, then a slim JRE image runs it. Health checks ensure services start in the correct order.

## E–Q. Specific Topic Explanations

### E. Architecture
- Modular monolith with layered architecture (Controller → Service → Repository)
- Clean separation of concerns with DTOs between layers
- Can be split into microservices later by extracting each package into a separate service

### F. Complete Request Flow
Request → JwtAuthFilter → SecurityFilterChain → Controller → Service → Repository → Database → Response

### G. Database
- 7 tables with proper foreign keys, constraints, timestamps
- JPA/Hibernate with PostgreSQLDialect
- `ddl-auto: update` for development

### H. Authentication Flow
Register → BCrypt hash → Save to DB → Generate JWT → Return to client
Login → Verify credentials → Generate JWT → Return to client

### I. JWT Flow
- Token contains: subject (email), issuedAt, expiration
- Signed with HMAC-SHA256 using a secret key
- Validated on every request by JwtAuthenticationFilter
- Expiry: 24 hours (configurable)

### J. Redis
- Cache-aside pattern with Spring annotations
- TTL: 10 minutes
- JSON serialization for human-readable cache entries
- Graceful fallback to PostgreSQL on Redis failure

### K. Kafka
- Single topic: `order-events`
- JSON serialized OrderEvent (orderId, userId, email, status, amount, timestamp)
- Consumer group: `ecommerce-group`
- KRaft mode (no Zookeeper dependency)

### L. Order Transaction
- `@Transactional` ensures atomicity
- Pessimistic flow: check ALL inventory → create order → reduce stock
- If any step fails, automatic rollback via Spring's transaction manager

### M. Error Handling
- `@RestControllerAdvice` catches all exceptions globally
- Custom exceptions: ResourceNotFoundException, InsufficientStockException, etc.
- Consistent JSON error format with timestamp, status, message, path

### N. Docker
- 5 containers: PostgreSQL, Redis, Kafka, Backend (JRE 21), Frontend (Nginx)
- Multi-stage builds for small images
- Health checks for startup ordering
- `docker compose up --build` starts everything

### O. Testing
- JUnit 5 + Mockito for unit tests
- MockMvc for controller integration tests
- Tested: registration, login, product CRUD, cart operations, order placement/cancellation

### P. Scaling Discussion
- **Current:** Single instance handles moderate traffic
- **Horizontal:** Add load balancer → multiple backend instances (JWT is stateless, Redis is shared)
- **Database:** Read replicas, connection pooling (HikariCP already included)
- **Kafka:** Add partitions + consumer instances for parallel processing
- **Future:** Split into microservices if specific services need independent scaling

### Q. Security Discussion
- BCrypt password hashing (cost factor 10)
- JWT with HMAC-SHA256 signing
- CORS configured for frontend origin
- CSRF disabled (JWT-based, not cookie-based)
- Role-based access with `@PreAuthorize`
- Secrets via environment variables (not hardcoded)
- Input validation with Jakarta Validation
