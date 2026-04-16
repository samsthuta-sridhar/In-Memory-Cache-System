package com.imcs.service;

import com.imcs.entity.AuditLogEntity;
import com.imcs.repository.AuditLogRepository;
import com.imcs.security.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private AuthService authService;

    public void log(String operation, String cacheKey, String status) {
        String user = authService.getCurrentUser() != null
            ? authService.getCurrentUser() : "system";
        auditLogRepository.save(
            new AuditLogEntity(user, operation, cacheKey, status));
    }

    public List<AuditLogEntity> getRecentLogs() {
        return auditLogRepository.findTop25ByOrderByTimestampDesc();
    }
}