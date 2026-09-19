# 10-Day Learning Roadmap

## Day 1: Spring Boot Architecture
**Goal:** Understand how the application starts and handles requests.

**Study:**
- What is `@SpringBootApplication`? How does auto-configuration work?
- What is dependency injection? Constructor injection vs field injection?
- What are `@Component`, `@Service`, `@Repository`, `@Controller`?

**Files to read:**
- `EcommerceApplication.java` — entry point
- `pom.xml` — dependencies
- `application.yml` — configuration

**Modify:**
- Add a `GET /api/health` endpoint that returns `{"status": "UP"}`
- Change the server port to 9090, verify it works

**Questions to answer:**
1. What does `@SpringBootApplication` combine?
2. Where does Spring scan for beans?
3. What is the purpose of `application.yml`?

---

## Day 2: REST API + JPA + PostgreSQL
**Goal:** Understand how data flows from HTTP request to database and back.

**Study:**
- REST conventions (GET, POST, PUT, DELETE)
- JPA annotations (`@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue`)
- Repository pattern (`JpaRepository`)

**Files to read:**
- `entity/Product.java`, `entity/User.java`
- `repository/ProductRepository.java`
- `controller/ProductController.java`
- `service/ProductService.java`
- `dto/product/ProductRequest.java`, `ProductResponse.java`
- `mapper/ProductMapper.java`

**Modify:**
- Add a `brand` field to Product entity and DTO
- Add a custom query: `findByPriceLessThan(BigDecimal price)`
- Enable `show-sql: true` in `application.yml`, read the generated SQL

**Questions to answer:**
1. What SQL does `findAll(Pageable)` generate?
2. Why use DTOs instead of entities in controllers?
3. What is `@Query` used for?

---

## Day 3: JWT + Spring Security
**Goal:** Understand how authentication and authorization work.

**Study:**
- What is JWT? Structure (header.payload.signature)
- What is BCrypt? How salting works
- Spring Security filter chain
- `@PreAuthorize` for role-based access

**Files to read:**
- `security/JwtTokenProvider.java`
- `security/JwtAuthenticationFilter.java`
- `security/SecurityConfig.java`
- `security/CustomUserDetailsService.java`
- `service/AuthService.java`
- `controller/AuthController.java`

**Modify:**
- Add token expiry time to `AuthResponse`
- Try accessing `/api/admin/orders` with a USER token — verify 403
- Decode a JWT at [jwt.io](https://jwt.io) — see the claims

**Questions to answer:**
1. Why is CSRF disabled?
2. What does `OncePerRequestFilter` guarantee?
3. What happens if JWT secret changes?

---

## Day 4: Products + Cart
**Goal:** Understand product management and cart operations.

**Study:**
- CRUD operations with validation
- Cart data model (one-to-many with cascade)
- Redis caching annotations

**Files to read:**
- `service/ProductService.java` — all methods
- `service/CartService.java` — all methods
- `controller/CartController.java`
- `entity/Cart.java`, `entity/CartItem.java`

**Modify:**
- Add product sorting by name (ascending)
- Add a "move to wishlist" endpoint (even as a stub)
- Test adding the same product twice to cart — verify quantity increases

**Questions to answer:**
1. What is `orphanRemoval = true`?
2. What happens if you add a product that doesn't exist to cart?
3. How does `getOrCreateCart()` work?

---

## Day 5: Orders + Transactions
**Goal:** This is the MOST IMPORTANT day. Understand transactional order processing.

**Study:**
- `@Transactional` — what it does, when it rolls back
- Order placement flow (all 11 steps)
- Inventory management
- Order cancellation with stock restoration

**Files to read:**
- `service/OrderService.java` — read EVERY line of `placeOrder()`
- `service/InventoryService.java`
- `entity/Order.java`, `entity/OrderItem.java`
- `mapper/OrderMapper.java`

**Modify:**
- Remove `@Transactional` from `placeOrder()`, intentionally create a failure scenario. What happens?
- Add a `PROCESSING` status to `OrderStatus` enum
- Add order total validation (reject orders over $10,000)

**Questions to answer:**
1. What happens if `reduceStock()` throws an exception?
2. Why is price captured in OrderItem?
3. What is the difference between CREATED and CONFIRMED?

---

## Day 6: Redis
**Goal:** Understand caching strategy and Redis integration.

**Study:**
- Cache-aside pattern
- `@Cacheable`, `@CachePut`, `@CacheEvict`
- Redis data types, TTL, serialization

**Files to read:**
- `config/RedisConfig.java`
- `service/ProductService.java` — caching annotations
- `service/CartService.java` — caching annotations

**Modify:**
- Add `@Cacheable` to `getProductById()` with a custom key
- Connect to Redis CLI: `docker exec -it ecommerce-redis redis-cli`
- Run `KEYS *` and `GET <key>` to see cached data
- Stop Redis container, verify app still works

**Questions to answer:**
1. What is TTL? What is ours set to?
2. What serialization format do we use?
3. What happens on cache miss?

---

## Day 7: Kafka
**Goal:** Understand asynchronous event-driven communication.

**Study:**
- Kafka concepts: topic, producer, consumer, consumer group, offset
- KRaft mode (no Zookeeper)
- JSON serialization/deserialization

**Files to read:**
- `config/KafkaConfig.java`
- `kafka/OrderEvent.java`
- `kafka/OrderEventProducer.java`
- `kafka/OrderEventConsumer.java`

**Modify:**
- Add `userName` field to `OrderEvent`
- Add a second consumer that prints a different message
- Place an order, check backend logs for consumer output
- Stop Kafka, place an order, verify it still succeeds

**Questions to answer:**
1. What is a consumer group?
2. What happens if consumer crashes and restarts?
3. Why is Kafka publish outside the transaction's critical path?

---

## Day 8: React + Docker
**Goal:** Understand frontend-backend integration and containerization.

**Study:**
- React components, state, props, hooks (useState, useEffect)
- React Router (routes, navigation, protected routes)
- Axios interceptors for JWT
- Docker multi-stage builds

**Files to read:**
- `frontend/src/App.jsx` — routing
- `frontend/src/context/AuthContext.jsx` — JWT management
- `frontend/src/api/axios.js` — interceptors
- `frontend/src/pages/Cart.jsx` — API calls
- `docker-compose.yml`
- `backend/Dockerfile`, `frontend/Dockerfile`

**Modify:**
- Add a "Welcome, {name}!" message on the Home page
- Add loading spinner component
- Change frontend port in Docker Compose from 3000 to 4000

**Questions to answer:**
1. Where is JWT stored in the browser?
2. What does the Axios response interceptor do on 401?
3. What is the benefit of multi-stage Docker builds?

---

## Day 9: Testing + Debugging
**Goal:** Understand testing strategy and debug common issues.

**Study:**
- JUnit 5: `@Test`, `@BeforeEach`, `@DisplayName`
- Mockito: `@Mock`, `@InjectMocks`, `when().thenReturn()`, `verify()`
- MockMvc for controller tests
- Common Spring Boot errors and fixes

**Files to read:**
- `test/.../service/AuthServiceTest.java`
- `test/.../service/OrderServiceTest.java`
- `test/.../controller/ProductControllerTest.java`

**Modify:**
- Write a test for `InventoryService.checkStock()` — insufficient stock case
- Write a test for `AuthService.login()` with wrong password
- Run all tests: `./mvnw test` — all must pass

**Questions to answer:**
1. What is the difference between `@Mock` and `@MockBean`?
2. Why use `verify()` in tests?
3. What does `assertThatThrownBy()` check?

---

## Day 10: Interview Preparation
**Goal:** Be able to explain everything without looking at code.

**Study:**
- Read `docs/INTERVIEW_GUIDE.md` — memorize the 30-second and 1-minute explanations
- Read `docs/INTERVIEW_QUESTIONS.md` — answer all 120 questions
- Practice explaining the architecture diagram on a whiteboard

**Practice:**
- Explain the project in 30 seconds to a friend
- Explain the order flow step-by-step
- Explain why you chose each technology
- Answer: "What happens if Redis/Kafka/DB goes down?"
- Answer: "How would you scale this?"
- Answer: "What would you improve?"

**Final checklist:**
- [ ] Can explain architecture in 30 seconds
- [ ] Can draw ER diagram from memory
- [ ] Can explain JWT flow without notes
- [ ] Can explain `@Transactional` in `placeOrder()`
- [ ] Can explain Redis caching strategy
- [ ] Can explain Kafka producer-consumer flow
- [ ] Can answer "what if X fails?" questions
- [ ] Can explain Docker Compose services
- [ ] Know 5+ things you'd improve
- [ ] Ran, tested, and debugged the project yourself
