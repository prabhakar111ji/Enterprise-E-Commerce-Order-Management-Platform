# Architecture Document

## Overview

This application follows a **modular monolith** architecture — a single deployable unit with clear internal module boundaries.

### Why Modular Monolith?

| Decision | Detail |
|----------|--------|
| **WHAT** | Single Spring Boot application with layered package structure |
| **WHY** | Simpler to develop, deploy, debug, and explain in interviews |
| **ALTERNATIVE** | Microservices (separate services for Product, Order, Cart, etc.) |
| **TRADE-OFF** | Monolith is less scalable but far simpler; can be split later |

## Layered Architecture

```mermaid
graph TD
    Client["React Frontend"] --> Controller
    Controller["Controller Layer\n@RestController"] --> Service
    Service["Service Layer\n@Service + @Transactional"] --> Repository
    Repository["Repository Layer\nSpring Data JPA"] --> Database["PostgreSQL"]
    Service --> Cache["Redis Cache"]
    Service --> Messaging["Kafka Producer"]
    
    style Controller fill:#3b82f6,color:white
    style Service fill:#10b981,color:white
    style Repository fill:#f59e0b,color:white
```

### Layer Responsibilities

| Layer | Responsibility | Spring Annotations |
|-------|---------------|-------------------|
| **Controller** | HTTP request/response handling, validation | `@RestController`, `@Valid` |
| **Service** | Business logic, transactions, caching | `@Service`, `@Transactional`, `@Cacheable` |
| **Repository** | Data access, queries | `@Repository`, `JpaRepository` |
| **Entity** | Database table mapping | `@Entity`, `@Table` |
| **DTO** | API request/response shapes | `@Data`, validation annotations |
| **Security** | Authentication, authorization | `@Configuration`, `OncePerRequestFilter` |

## Data Flow: Order Placement

```mermaid
sequenceDiagram
    participant U as User
    participant C as OrderController
    participant S as OrderService
    participant CS as CartService
    participant IS as InventoryService
    participant R as OrderRepository
    participant K as KafkaProducer
    participant KC as KafkaConsumer

    U->>C: POST /api/orders
    C->>S: placeOrder(userId)
    S->>CS: getCartEntity(userId)
    S->>IS: checkStock(productId, qty)
    S->>R: save(order)
    S->>IS: reduceStock(productId, qty)
    S->>CS: clearCart(userId)
    S->>K: publishOrderEvent()
    K->>KC: order-events topic
    KC->>KC: Log notification
    S->>C: OrderResponse
    C->>U: 201 Created
```

## Security Architecture

```mermaid
flowchart LR
    Request["HTTP Request"] --> Filter["JwtAuthFilter"]
    Filter -->|Has Token?| Validate["Validate JWT"]
    Filter -->|No Token| Chain["Continue to Security"]
    Validate -->|Valid| Context["Set SecurityContext"]
    Validate -->|Invalid| Chain
    Context --> Chain
    Chain --> Security["SecurityFilterChain"]
    Security -->|Authorized| Controller
    Security -->|Forbidden| Error["403 Response"]
```

## Technology Decisions

### PostgreSQL vs MongoDB
| | PostgreSQL | MongoDB |
|---|-----------|---------|
| Schema | Fixed schema, strong relationships | Flexible schema |
| Transactions | Full ACID | Limited multi-doc transactions |
| Our data | Highly relational (users → orders → items) | Would need denormalization |
| **Decision** | ✅ PostgreSQL — relational data needs relational DB |

### Redis vs In-Process Cache (Caffeine)
| | Redis | Caffeine |
|---|-------|---------|
| Location | External server | JVM heap |
| Shared | Yes (multi-instance) | No |
| Persistence | Optional | No |
| **Decision** | ✅ Redis — demonstrable, interview-relevant, production-realistic |

### Kafka vs REST Calls for Notifications
| | Kafka | REST |
|---|-------|------|
| Coupling | Loose (producer doesn't know consumers) | Tight (direct HTTP call) |
| Reliability | Messages persisted in topic | Lost if service is down |
| Scalability | Add consumers without changing producer | Each consumer needs integration |
| **Decision** | ✅ Kafka — demonstrates async, event-driven architecture |

### JWT vs Session-Based Auth
| | JWT | Sessions |
|---|-----|---------|
| State | Stateless (token contains claims) | Stateful (server stores session) |
| Scalability | Easy (no shared session store) | Needs sticky sessions or shared store |
| Revocation | Difficult (no built-in revoke) | Easy (delete from store) |
| **Decision** | ✅ JWT — standard for REST APIs, interview-relevant |
