package com.admission_crm.lead_management.Entity.AnalyticsAndReporting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "system_logs")
@Table(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    // Fix: Remove the @Column annotation since Hibernate will automatically
    // map entityType to entity_type with snake_case naming strategy
    private String entityType;

    private String entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "additional_info", columnDefinition = "JSON")
    private String additionalInfo;

    @CreationTimestamp
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}