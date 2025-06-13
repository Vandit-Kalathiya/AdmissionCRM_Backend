package com.admission_crm.lead_management.Entity.Communication;

import com.admission_crm.lead_management.Entity.CoreEntities.User;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledCommunication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Communication.CommunicationType type;

    private String communicationTemplate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    @Column(name = "personalization_data", columnDefinition = "JSON")
    private String personalizationData;

    @Column(name = "custom_subject", length = 255)
    private String customSubject;

    @Column(name = "custom_content", columnDefinition = "TEXT")
    private String customContent;

    private String createdBy;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    private String sentCommunicationId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ScheduleStatus {
        SCHEDULED, SENT, FAILED, CANCELLED
    }
}
