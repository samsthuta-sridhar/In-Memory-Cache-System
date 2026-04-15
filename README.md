## In-Memory Cache System — Banking

A desktop application built with Java Spring Boot and JavaFX that implements
an in-memory caching layer for banking data, reducing database load and
improving response times through intelligent cache management.


## What This Project Does

Banks process millions of transactions daily. Every balance check,
account lookup, or transaction query hits the database — causing latency
and load. This system places an in-memory cache between the application
and PostgreSQL:

- **Cache Hit** → data returned in <1ms from RAM
- **Cache Miss** → falls back to PostgreSQL, stores result in cache
- **TTL Expiry** → stale entries auto-removed after 30 minutes
- **Eviction** → when cache is full, LRU or LFU policy removes least useful entry

---

## Tech Stack

| Layer    | Technology                                         |
|----------|----------------------------------------------------|
| Backend  | Java 17, Spring Boot 3.3.4                         |
| Frontend | JavaFX 17 (FXML)                                   |
| Database | PostgreSQL                                         |
| ORM      | Spring Data JPA/Hibernate                          |
| Security | AES-128 encryption, BCrypt, Spring Security Crypto |
| Build    | Maven 3.9.x                                        |

---

## Architecture

**Modular Monolith** — single Spring Boot application with MVC architecture.

User → JavaFX UI → CacheController → CacheService → CacheManager (RAM)
↘ CacheDataRepository (PostgreSQL)

### Package Structure

com.imcs/
├── cache/          # CacheManager, CacheEntry, CacheEntryFactory
├── controller/     # CacheController (REST endpoints)
├── eviction/       # EvictionPolicy, LRUEvictionPolicy, LFUEvictionPolicy
├── service/        # CacheService, TTLManager, AuditLogService, CacheStatsService
├── repository/     # CacheDataRepository, UserRepository, AuditLogRepository
├── entity/         # CacheDataEntity, UserEntity, AuditLogEntity
├── security/       # AuthService, EncryptionService, SessionManager, RateLimiterService
├── events/         # CacheExpiryEvent, CacheExpiryListener
├── presentation/   # JavaFX controllers
├── response/       # CacheResponseView
└── config/         # CacheConfiguration, ApplicationConfiguration

---

## Design Patterns

| Pattern                 | Category   | Class                |
|-------------------------|------------|----------------------|
| Singleton               | Creational | `CacheManager`       |
| Factory Method          | Creational | `CacheEntryFactory`  |
| Proxy                   | Structural | `CacheService`       |
| Chain of Responsibility | Behavioral | Cache → DB fallback  |
| MVC                     | Framework  | Spring Boot + JavaFX |

## Design Principles

| Principle             | Type  | Applied In                                            |
|-----------------------|-------|-------------------------------------------------------|
| Information Expert    | GRASP | `CacheManager` owns all cache queries                 |
| Creator               | GRASP | `CacheEntryFactory` creates `CacheEntry`              |
| Polymorphism          | GRASP | `EvictionPolicy` interface + LRU/LFU                  |
| Low Coupling          | GRASP | `TTLManager` depends only on events                   |
| Single Responsibility | SOLID | `CacheEntry` — one job only                           |
| Dependency Inversion  | SOLID | `CacheService` depends on abstractions                |
| Open/Closed           | SOLID | New eviction policies without changing `CacheManager` |
| Interface Segregation | SOLID | Slim interfaces per consumer                          |

---

## Security Features

- AES-128 encryption on all cached values and DB entries
- Session timeout after 10 minutes of inactivity
- Account lockout after 3 failed login attempts
- Rate limiting — max 10 requests/second per key
- Full audit log of all operations stored in PostgreSQL
- Input validation on all key fields

---

## Setup Instructions

### Prerequisites
- Java 17
- Maven 3.9+
- PostgreSQL 14+

### Database Setup
1. Open pgAdmin
2. Create database: `imcs\_db`
3. Run the following in Query Tool:

```sql

CREATE TABLE IF NOT EXISTS cache\_data (

&#x20;   key VARCHAR(255) PRIMARY KEY,

&#x20;   value TEXT NOT NULL

);



CREATE TABLE IF NOT EXISTS users (

&#x20;   id SERIAL PRIMARY KEY,

&#x20;   username VARCHAR(100) UNIQUE NOT NULL,

&#x20;   password VARCHAR(255) NOT NULL,

&#x20;   failed\_attempts INTEGER DEFAULT 0,

&#x20;   locked BOOLEAN DEFAULT FALSE,

&#x20;   created\_at TIMESTAMP DEFAULT CURRENT\_TIMESTAMP

);



CREATE TABLE IF NOT EXISTS audit\_log (

&#x20;   id SERIAL PRIMARY KEY,

&#x20;   username VARCHAR(100),

&#x20;   operation VARCHAR(50),

&#x20;   cache\_key VARCHAR(255),

&#x20;   status VARCHAR(50),

&#x20;   timestamp TIMESTAMP DEFAULT CURRENT\_TIMESTAMP

);



INSERT INTO users (username, password, failed\_attempts, locked)

VALUES ('admin', 'admin123', 0, false)

ON CONFLICT (username) DO NOTHING;

```

### Configuration
Edit `src/main/resources/application.properties`:
```properties

spring.datasource.url=jdbc:postgresql://localhost:5432/imcs\_db

spring.datasource.username=postgres

spring.datasource.password=your\_password

cache.max-capacity=10

cache.ttl-ms=1800000

cache.eviction-policy=LRU

```

### Run
```bash

mvn clean compile -DskipTests

mvn spring-boot:run

```

### Login
- Username: `admin`
- Password: `admin123`

---

## Use Cases

| # | Major Use Case           | Minor Use Case            |
|---|--------------------------|---------------------------|
| 1 | Retrieve Data from Cache | Handle Cache Hit          |
| 2 | Handle Cache Miss        | Fetch Data from Database  |
| 3 | Store Data in Cache      | Apply Eviction Policy     |
| 4 | Expire Cache Entry (TTL) | Update/Delete Cache Entry |

---

## GitHub Repository
[https://github.com/samsthuta-sridhar/In-Memory-Cache-System](https://github.com/samsthuta-sridhar/In-Memory-Cache-System)