# Interview Questions — E-Commerce Platform (100+)

## SECTION 1: Project Basics (10)

**Q1. What is this project about?**
A full-stack e-commerce platform where users browse products, manage carts, place orders; admins manage products and orders. Built with Spring Boot, React, PostgreSQL, Redis, Kafka, Docker.

**Q2. Why did you choose a modular monolith over microservices?**
Simpler to develop, deploy, and debug. For a small team/learning project, microservices add unnecessary operational complexity (service discovery, distributed tracing, data consistency). Monolith can be split later.

**Q3. How many entities/tables does the system have?**
7: User, Product, Inventory, Cart, CartItem, Order, OrderItem.

**Q4. What are the two user roles?**
USER (browse, cart, order) and ADMIN (manage products, update order status).

**Q5. How does the order flow work at a high level?**
Validate cart → check inventory → create order → reduce stock → simulate payment → mark CONFIRMED → clear cart → publish Kafka event.

**Q6. What is the tech stack?**
Java 21, Spring Boot 3.3, Spring Security + JWT, JPA/Hibernate, PostgreSQL, Redis, Kafka, React + Vite, Docker Compose.

**Q7. How do you run the entire application?**
`docker compose up --build` — starts PostgreSQL, Redis, Kafka, backend, and frontend.

**Q8. What does the frontend do?**
React SPA with pages for login, register, product listing, product detail, cart, orders, and admin dashboard. Uses Axios with JWT interceptors.

**Q9. Is payment real?**
No, payment is simulated. The system always marks payment as successful. Real payment gateway (e.g., Stripe) is a planned future improvement.

**Q10. What testing framework do you use?**
JUnit 5 for assertions, Mockito for mocking dependencies, MockMvc for integration testing controllers.

---

## SECTION 2: Architecture (10)

**Q11. Explain the layered architecture.**
Controller (HTTP) → Service (business logic) → Repository (data access) → Database. DTOs transfer data between controller and service layers.

**Q12. Why use DTOs instead of exposing entities directly?**
Security: don't expose passwords or internal IDs. Flexibility: API response shape can differ from DB schema. Validation: DTOs carry validation annotations.

**Q13. What design patterns do you use?**
Repository Pattern, DTO Pattern, Builder Pattern, Singleton (Spring beans), Observer (Kafka pub/sub), Template Method (JpaRepository).

**Q14. How does dependency injection work in this project?**
Spring's `@RequiredArgsConstructor` (Lombok) creates constructor injection. Spring container manages bean lifecycle and injects dependencies automatically.

**Q15. What is the role of the mapper classes?**
Convert between Entity and DTO objects. E.g., `ProductMapper.toResponse()` converts a Product entity + Inventory into ProductResponse DTO.

**Q16. Why separate config classes for Redis, Kafka, Security?**
Separation of concerns. Each config is independent and can be modified without affecting others.

**Q17. How would you convert this to microservices?**
Extract packages into separate services: Product Service, Order Service, Cart Service, Auth Service. Use API Gateway for routing, Kafka for inter-service communication, each with its own database.

**Q18. What is the Single Responsibility Principle and where is it applied?**
Each class has one reason to change. Controllers only handle HTTP, services only contain business logic, repositories only access data.

**Q19. What package structure do you follow?**
Feature-agnostic layered: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `security/`, `config/`, `exception/`, `mapper/`, `kafka/`.

**Q20. Why is `open-in-view` set to false?**
Prevents lazy loading in the view layer (controller), which can cause N+1 queries and hide performance issues. Forces explicit fetch strategies.

---

## SECTION 3: Java (10)

**Q21. What Java version do you use and why?**
Java 21 (LTS). Provides records, sealed classes, pattern matching, virtual threads, and is the latest long-term support release.

**Q22. What are the main Java features you use?**
Generics (Collections, JpaRepository), Streams (mapping cart items to order items), Lambdas (repository queries), Enums (Role, OrderStatus), Builder pattern.

**Q23. Where do you use Streams in this project?**
OrderService: mapping CartItems to OrderItems, calculating total amount. OrderMapper: converting entity lists to DTO lists.

**Q24. What is the Builder pattern and where is it used?**
Creates objects step-by-step. Used with `@Builder` (Lombok) on entities and DTOs: `User.builder().name("John").email("...").build()`.

**Q25. What is Lombok and why use it?**
Code generation library. `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor` reduce boilerplate. Less code = less bugs.

**Q26. Difference between checked and unchecked exceptions in your project?**
All custom exceptions (ResourceNotFoundException, BadRequestException) extend RuntimeException (unchecked). Spring's `@Transactional` only rolls back on unchecked exceptions by default.

**Q27. What is BigDecimal and why use it for price?**
Arbitrary-precision decimal. `double` has floating-point errors (e.g., 0.1 + 0.2 ≠ 0.3). Financial calculations must be exact.

**Q28. How do enums help in this project?**
`Role` (USER, ADMIN) and `OrderStatus` (CREATED, CONFIRMED, etc.) provide type safety. Stored as strings in DB via `@Enumerated(EnumType.STRING)`.

**Q29. What is the Optional class and where do you use it?**
Container that may or may not have a value. Used by repository methods: `findByEmail()` returns `Optional<User>`. Prevents NullPointerException.

**Q30. What is the difference between `==` and `.equals()` for your enums?**
Both work for enums because enum constants are singletons. `==` is faster and null-safe. We use `!=` for status checks.

---

## SECTION 4: Spring Boot (10)

**Q31. What is Spring Boot auto-configuration?**
Spring Boot automatically configures beans based on classpath dependencies. Adding `spring-boot-starter-data-jpa` auto-configures DataSource, EntityManager, TransactionManager.

**Q32. What is `@SpringBootApplication`?**
Combines `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`. Entry point that bootstraps the application.

**Q33. What is the difference between `@Component`, `@Service`, `@Repository`?**
All register beans in Spring container. `@Service` indicates business logic, `@Repository` translates DB exceptions, `@Component` is generic. Semantic difference.

**Q34. What is `application.yml` used for?**
External configuration: database URLs, Redis host, Kafka brokers, JWT secret, logging levels. Supports profiles (e.g., `application-docker.yml`).

**Q35. What are Spring Profiles?**
Environment-specific configurations. `application-docker.yml` overrides database host to `postgres` (Docker service name). Activated via `SPRING_PROFILES_ACTIVE=docker`.

**Q36. What is `@Transactional` and where do you use it?**
Wraps method in a database transaction. Used in `OrderService.placeOrder()` — if any step fails, all changes rollback. Also used for cart updates and inventory changes.

**Q37. What is `@Valid` and how does validation work?**
Triggers Jakarta Bean Validation on request body. Annotations like `@NotBlank`, `@Email`, `@Positive` validate fields. Invalid data returns 400 with field-level messages.

**Q38. How does pagination work?**
`Pageable` parameter in controller, Spring Data builds SQL with LIMIT/OFFSET. URL: `?page=0&size=10&sort=price,asc`.

**Q39. What is `@RestControllerAdvice`?**
Global exception handler. Catches exceptions from all controllers and returns consistent JSON error responses. Single place for all error handling.

**Q40. What happens if the database is down on startup?**
Application fails to start (DataSource cannot be created). Spring Boot's health checks won't pass. Docker Compose retries via `restart: on-failure`.

---

## SECTION 5: Spring Security / JWT (10)

**Q41. How does JWT authentication work in your app?**
User logs in → server verifies credentials → generates JWT → client stores in localStorage → sends in Authorization header → server validates on every request.

**Q42. What is inside a JWT token?**
Header (algorithm), Payload (subject/email, issuedAt, expiration), Signature (HMAC-SHA256 with secret key).

**Q43. What is `OncePerRequestFilter`?**
Guarantees the filter runs exactly once per request. `JwtAuthenticationFilter` extends it to validate tokens without double-processing on forwards.

**Q44. How does `SecurityFilterChain` work?**
Defines which URLs require authentication. `/api/auth/**` and `GET /api/products/**` are public. `/api/admin/**` requires ADMIN role. Everything else requires authentication.

**Q45. What is `@PreAuthorize("hasRole('ADMIN')")`?**
Method-level security. Spring checks if authenticated user has ROLE_ADMIN before executing the method. Throws 403 if not.

**Q46. Why is CSRF disabled?**
CSRF protection is for cookie-based sessions. JWT is sent in Authorization header, not cookies. CSRF attacks only work with cookies.

**Q47. How are passwords stored?**
BCrypt hash with random salt. `$2a$10$...` format. BCrypt is intentionally slow to resist brute-force attacks.

**Q48. What happens if JWT expires?**
`JwtTokenProvider.validateToken()` catches `ExpiredJwtException`, returns false. Request proceeds without authentication. Client receives 401, redirects to login.

**Q49. How do you protect admin endpoints?**
Two layers: `SecurityFilterChain` requires ADMIN role for `/api/admin/**`. `@PreAuthorize` on controller methods for defense-in-depth.

**Q50. Could someone decode the JWT and see user data?**
Yes, JWT payload is Base64-encoded (not encrypted). But they cannot modify it — the signature verification would fail. Never put sensitive data (passwords) in JWT.

---

## SECTION 6: JPA / Hibernate / PostgreSQL (10)

**Q51. What is JPA and Hibernate?**
JPA is a specification (interface). Hibernate is the implementation. JPA defines annotations like `@Entity`, Hibernate translates them to SQL.

**Q52. What is `ddl-auto: update`?**
Hibernate auto-updates DB schema based on entity changes. Good for development, dangerous for production (use migrations like Flyway instead).

**Q53. Explain `@OneToMany` and `@ManyToOne`.**
User has many Orders (`@OneToMany`), Order belongs to one User (`@ManyToOne`). The foreign key is on the "many" side (orders table has `user_id`).

**Q54. What is the N+1 problem?**
Loading a list of Orders, then lazily loading User for each order = 1 query for orders + N queries for users. Solved with `JOIN FETCH` or `@EntityGraph`.

**Q55. How do you solve N+1 in your project?**
Custom JPQL with `JOIN FETCH`: `@Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")`.

**Q56. What is the difference between `FetchType.LAZY` and `EAGER`?**
LAZY loads related data on-demand (first access). EAGER loads immediately with the parent. Default: `@OneToMany` is LAZY, `@ManyToOne` is EAGER.

**Q57. Why capture price in OrderItem instead of referencing Product?**
Product price can change after purchase. OrderItem stores the price at purchase time. Historical accuracy for receipts and accounting.

**Q58. What is `cascade = CascadeType.ALL`?**
Operations on parent propagate to children. Saving an Order also saves its OrderItems. Deleting a Cart deletes its CartItems.

**Q59. What is `orphanRemoval = true`?**
When a CartItem is removed from Cart's items list, it's automatically deleted from the database. Prevents orphan records.

**Q60. Why PostgreSQL over MySQL?**
Better ACID compliance, advanced features (JSONB, window functions, CTEs), better concurrency control (MVCC). Standard in enterprise Java development.

---

## SECTION 7: Redis (10)

**Q61. Why use Redis in this project?**
Caching frequently read data (products, carts). Redis provides sub-millisecond reads vs PostgreSQL's 5-10ms.

**Q62. What is the cache-aside pattern?**
Read: check cache → if miss, read DB, store in cache. Write: update DB, invalidate cache. This is what `@Cacheable` + `@CacheEvict` implement.

**Q63. What happens if Redis goes down?**
Application continues working. Spring's cache abstraction handles the failure gracefully — requests go directly to PostgreSQL. Performance degrades slightly.

**Q64. What is TTL and why set it?**
Time To Live — cache entries expire after 10 minutes. Prevents serving stale data. Balance between freshness and performance.

**Q65. What serialization format does Redis use?**
JSON (`GenericJackson2JsonRedisSerializer`). Human-readable in Redis CLI. Alternative: Java serialization (faster but unreadable).

**Q66. What is `@Cacheable`?**
Checks cache before method execution. If cached, returns cached value (method body skipped). If not, executes method and caches result.

**Q67. What is `@CacheEvict`?**
Removes entries from cache. Used on write operations (create/update/delete product) to invalidate stale data.

**Q68. When is `@CacheEvict(allEntries = true)` used?**
When creating/updating products, we clear ALL product list cache entries because the list content changes. Individual product cache is evicted by key.

**Q69. Could you use Redis for sessions instead of JWT?**
Yes. Store session data in Redis, send session ID as cookie. Redis makes sessions shareable across instances. But JWT is more common for REST APIs.

**Q70. What data structure does Redis use for caching?**
String (key-value). Key: `products::list-null-null-0`, Value: JSON-serialized PageResponse.

---

## SECTION 8: Kafka (10)

**Q71. Why use Kafka instead of a REST call for notifications?**
Decoupling: OrderService doesn't know about notification logic. Reliability: messages persist in topic. Scalability: add more consumers without changing producer.

**Q72. What is a Kafka Topic?**
A named channel for messages. Like a queue but supports multiple consumers. Our topic: `order-events`.

**Q73. What is a Kafka Producer?**
Component that publishes messages to a topic. `OrderEventProducer` sends `OrderEvent` after successful order creation.

**Q74. What is a Kafka Consumer?**
Component that reads messages from a topic. `OrderEventConsumer` listens on `order-events` and logs notifications.

**Q75. What is a Consumer Group?**
Set of consumers that share the work of reading from a topic. Each message goes to exactly one consumer in the group. Our group: `ecommerce-group`.

**Q76. What is KRaft mode?**
Kafka without Zookeeper. Kafka manages its own metadata using the Raft consensus protocol. Simpler deployment.

**Q77. What happens if Kafka is down when placing an order?**
Order still succeeds. Kafka publish is in a try-catch. The event is lost, but the order is committed. In production, we'd add a retry mechanism or outbox pattern.

**Q78. What serialization does the producer use?**
JSON (`JsonSerializer`). The `OrderEvent` is serialized to JSON and sent as the message value. Key is the orderId as a String.

**Q79. What is `auto-offset-reset: earliest`?**
When a consumer starts for the first time, it reads from the beginning of the topic. Alternative: `latest` (only new messages).

**Q80. How would you add email notifications?**
Create an `EmailNotificationConsumer` in the same consumer group (or a new one). It listens on `order-events` and sends emails. No changes to producer needed.

---

## SECTION 9: REST API / Backend (10)

**Q81. What HTTP methods do you use and why?**
GET (read), POST (create), PUT (update), DELETE (remove). Following REST conventions for predictable APIs.

**Q82. What is the difference between `@RequestBody` and `@RequestParam`?**
`@RequestBody`: JSON in request body (POST/PUT). `@RequestParam`: query string parameter (`?search=macbook`).

**Q83. Why return `ResponseEntity` instead of direct objects?**
Control over HTTP status code and headers. `new ResponseEntity<>(data, HttpStatus.CREATED)` returns 201 instead of default 200.

**Q84. How do you handle authentication in controllers?**
`@AuthenticationPrincipal UserDetails userDetails` injects the currently authenticated user. We extract userId from email.

**Q85. What is `@PathVariable`?**
Extracts values from URL path. `@GetMapping("/{id}")` with `@PathVariable Long id` extracts the ID from `/api/products/5`.

**Q86. How does global exception handling work?**
`@RestControllerAdvice` class catches exceptions thrown by any controller. Each exception type has a handler method returning an `ErrorResponse` DTO.

**Q87. What validation annotations do you use?**
`@NotBlank` (non-empty string), `@NotNull`, `@Email`, `@Positive` (> 0), `@PositiveOrZero` (≥ 0), `@Size(min=6)`.

**Q88. How do you implement search and filtering?**
Custom JPQL query with optional parameters: `WHERE (:category IS NULL OR p.category = :category) AND (:search IS NULL OR LOWER(p.name) LIKE ...)`.

**Q89. What is idempotency and how do you handle duplicate orders?**
Idempotency: same request produces same result. Currently, duplicate POST to /orders creates two orders. Fix: add idempotency key (UUID) in request header.

**Q90. What is CORS and why configure it?**
Cross-Origin Resource Sharing. Browser blocks requests from `localhost:5173` (React) to `localhost:8080` (API) unless CORS headers are set.

---

## SECTION 10: Docker / Testing / Deployment (10)

**Q91. What is Docker and why use it?**
Containerization platform. Packages application + dependencies into portable containers. Ensures "works on my machine" = "works everywhere".

**Q92. What is Docker Compose?**
Tool for defining multi-container applications. Our `docker-compose.yml` defines 5 services (PostgreSQL, Redis, Kafka, backend, frontend) with a single command.

**Q93. What is a multi-stage Docker build?**
Two stages: build stage (Maven + JDK) compiles code, runtime stage (JRE only) runs it. Final image is smaller (no build tools).

**Q94. How do health checks work in Docker Compose?**
`healthcheck` defines a command to test if a service is ready. `depends_on: condition: service_healthy` ensures backend starts only after PostgreSQL is healthy.

**Q95. What is the difference between unit and integration tests?**
Unit: test one class in isolation (mock dependencies with Mockito). Integration: test multiple layers together (MockMvc tests controller + security).

**Q96. What is `@MockBean`?**
Spring Boot Test annotation. Replaces a real Spring bean with a Mockito mock in the application context. Used in integration tests.

**Q97. What does Mockito's `when().thenReturn()` do?**
Defines mock behavior: "when this method is called with these args, return this value." Isolates the class under test.

**Q98. How would you deploy this to AWS?**
EC2 or ECS for containers. RDS for PostgreSQL. ElastiCache for Redis. MSK for Kafka. ALB for load balancing. S3 for frontend static files.

**Q99. What would you monitor in production?**
API response times, error rates (4xx/5xx), JVM metrics (heap, GC), database connection pool, Redis hit/miss ratio, Kafka consumer lag.

**Q100. How would you add CI/CD?**
GitHub Actions: on push → run tests → build Docker images → push to ECR → deploy to ECS. Separate pipelines for staging and production.

---

## BONUS: Scenario-Based Questions (20)

**Q101. What happens when stock is insufficient?**
`InventoryService.checkStock()` throws `InsufficientStockException` with product name, requested qty, and available qty. Transaction rolls back. User sees 400 error.

**Q102. What if two users order the last item simultaneously?**
Database transaction isolation prevents double-booking. The first transaction reduces stock; the second finds insufficient stock and fails. For high concurrency, use pessimistic locking (`@Lock(PESSIMISTIC_WRITE)`).

**Q103. What happens if the database transaction fails mid-order?**
`@Transactional` ensures atomicity. If stock reduction fails after order creation, both operations roll back. No partial state.

**Q104. How do you prevent unauthorized admin access?**
Two layers: `SecurityFilterChain` blocks non-ADMIN from `/api/admin/**`. `@PreAuthorize("hasRole('ADMIN')")` on methods for defense-in-depth.

**Q105. What if a user sends the same order request twice?**
Currently, two orders are created. Fix: implement idempotency key — client sends UUID, server checks if order with that key exists.

**Q106. What if JWT secret is compromised?**
All tokens become invalid when secret is changed. Users must re-login. In production: rotate secrets, use short-lived tokens + refresh tokens.

**Q107. Why PostgreSQL instead of MongoDB?**
Our data is highly relational (users → orders → items → products). PostgreSQL provides strong ACID transactions, foreign keys, and JOIN operations naturally.

**Q108. Why Redis instead of database for caching?**
Redis is in-memory: ~0.1ms reads vs ~5ms for PostgreSQL. Perfect for hot data (product listings read thousands of times per minute).

**Q109. Why Kafka instead of RabbitMQ?**
Kafka retains messages (replayable), higher throughput, better for event sourcing. RabbitMQ is better for task queues. Both work; Kafka is more interview-relevant.

**Q110. How would you handle concurrent orders for the same product?**
Pessimistic locking: `SELECT ... FOR UPDATE` locks the inventory row during transaction. Or optimistic locking: `@Version` column, retry on conflict.

**Q111. How would you secure JWT better?**
Short expiration (15 min) + refresh tokens. Store refresh token in HttpOnly cookie. Blacklist on logout. Use RS256 instead of HS256.

**Q112. What happens if JWT expires during a user session?**
Axios interceptor catches 401 response, removes token from localStorage, redirects to login. User must re-authenticate.

**Q113. How would you improve performance?**
Database indexing (category, email), connection pooling tuning, Redis for more cache keys, CDN for frontend, database read replicas.

**Q114. How would you scale this application?**
Load balancer → multiple backend instances (stateless JWT). Redis and PostgreSQL handle shared state. Kafka partitions for parallel event processing.

**Q115. What would you do if Redis memory is full?**
Configure maxmemory policy (e.g., `allkeys-lru` — evict least recently used). Monitor with `INFO memory`. Scale Redis or reduce TTL.

**Q116. How would you handle payment failures?**
Implement retry logic. Use a state machine: PAYMENT_PENDING → PAYMENT_FAILED/PAYMENT_SUCCESS. Show appropriate UI feedback. Don't reduce stock until payment succeeds.

**Q117. What if Kafka consumer crashes?**
Kafka retains messages. When consumer restarts, it reads from last committed offset and processes missed messages. No message loss.

**Q118. Why modular monolith instead of microservices?**
For a learning project: simpler deployment (1 process), no distributed transactions, no service discovery needed, easier debugging, faster development.

**Q119. How would you convert this to microservices later?**
Extract each service (Auth, Product, Cart, Order) into separate Spring Boot apps. Each with own database (database-per-service). Use Kafka for inter-service events. Add API Gateway.

**Q120. What would you monitor in production?**
Application: Micrometer + Prometheus + Grafana. Logs: ELK Stack. Kafka: consumer lag, throughput. Database: slow queries, connection pool. Alerts: PagerDuty/Slack.
