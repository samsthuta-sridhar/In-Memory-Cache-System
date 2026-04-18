package com.imcs.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String operation;
    private String cacheKey;
    private String status;
    private LocalDateTime timestamp;

    public AuditLogEntity() {}

    public AuditLogEntity(String username, String operation, String cacheKey, String status) {
        this.username = username;
        this.operation = operation;
        this.cacheKey = cacheKey;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getOperation() { return operation; }
    public String getCacheKey() { return cacheKey; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
}