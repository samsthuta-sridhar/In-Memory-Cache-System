## In-Memory Cache System 

A high-performance desktop application that implements an in-memory caching layer for banking data, reducing database load and improving response times through intelligent cache management.

## Overview

Every balance check, account lookup, or transaction query typically hits the database — causing latency under load. This system places a bounded in-memory cache between the application and PostgreSQL, serving hot data directly from RAM and falling back to the database only when necessary.

- **Cache Hit** — data returned in <1ms from RAM
- **Cache Miss** — falls back to PostgreSQL, result stored in cache automatically
- **TTL Expiry** — stale entries removed after 30 minutes
- **Eviction** — LRU or LFU policy removes least useful entry when cache is full

---

## Tech Stack

| Layer    | Technology                                         |
|----------|----------------------------------------------------|
| Backend  | Java 17, Spring Boot 3.3.4                         |
| Frontend | JavaFX 17 (FXML)                                   |
| Database | PostgreSQL                                         |
| ORM      | Spring Data JPA/Hibernate                          |
| Security | AES-128 encryption                                 |
| Build    | Maven 3.9.x                                        |

---

## Features

- Bounded in-memory key-value store using 'ConcurrentHashMap'
- Switchable LRU/LFU eviction policies at runtime
- TTL-based automatic cache expiry
- Automatic DB fallback and re-caching on miss
- AES-128 encryption on all stored values
- Admin login with account lockout and session timeout
- Rate limiting per key
- Full audit log of all operations
- Live cache statistics dashboard with capacity indicator

## Architecture

**Modular Monolith** — single Spring Boot + JavFX desktop application.

'''
User → JavaFX UI → CacheController → CacheService → CacheManager (RAM) ↘ CacheDataRepository (PostgreSQL)
'''

**Package Structure**

| Package         | Classes                                                            |
|-----------------|--------------------------------------------------------------------|
| `cache/`        | CacheManager, CacheEntry, CacheEntryFactory                        |
| `controller/`   | CacheController                                                    |
| `eviction/`     | EvictionPolicy, LRUEvictionPolicy, LFUEvictionPolicy               |
| `service/`      | CacheService, TTLManager, AuditLogService, CacheStatsService       |
| `repository/`   | CacheDataRepository, UserRepository, AuditLogRepository            |
| `entity/`       | CacheDataEntity, UserEntity, AuditLogEntity                        |
| `security/`     | AuthService, EncryptionService, SessionManager, RateLimiterService |
| `events/`       | CacheExpiryEvent, CacheExpiryListener                              |
| `presentation/` | JavaFX controllers                                                 |
| `response/`     | CacheResponseView                                                  |
| `config/`       | CacheConfiguration, ApplicationConfiguration                       |

---

## Getting Started

### Prerequisites
- Java 17
- Maven 3.9+
- PostgreSQL 14+

### Database Setup

1. Install PostgreSQL and open pgAdmin
2. Create a new database called `imcs_db`
3. Open the Query Tool for `imcs_db` and run the following:

```sql
CREATE TABLE IF NOT EXISTS cache_data (
    key VARCHAR(255) PRIMARY KEY,
    value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_log (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100),
    operation VARCHAR(50),
    cache_key VARCHAR(255),
    status VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, failed_attempts, locked)
VALUES ('admin', 'admin123', 0, false)
ON CONFLICT (username) DO NOTHING;
```

4. Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/imcs_db
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
```

### Login Credentials
| Field    | Value    |
|----------|----------|
| Username | admin    |
| Password | admin123 |

---

## Design Patterns

| Pattern                 | Category     | Implementation                                           |
|-------------------------|--------------|----------------------------------------------------------|
| Singleton               | Creational   | `CacheManager` — single shared cache instance            |
| Factory Method          | Creational   | `CacheEntryFactory` — centralised entry creation         |
| Proxy                   | Structural   | `CacheService` — adds encryption, logging, rate limiting |
| Chain of Responsibility | Behavioral   | Cache → DB fallback chain                                |
| MVC                     | Architecture | Spring Boot (Controller/Model) + JavaFX (View)           |

---


