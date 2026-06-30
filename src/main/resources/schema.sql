-- Schema managed manually via pgAdmin
-- Tables: cache_data, users, audit_log
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

-- Default admin user (password: admin123, hashed with Argon2id)
INSERT INTO users (username, password, failed_attempts, locked)
VALUES ('admin', '$argon2id$v=19$m=16384,t=2,p=1$eg6edoSYbd9fdmRDG1hCsQ$j63te2IRl3fp5F50UqcDK+/yHgobKdCoEgkzH43lGvw', 0, false)
ON CONFLICT (username) DO NOTHING;