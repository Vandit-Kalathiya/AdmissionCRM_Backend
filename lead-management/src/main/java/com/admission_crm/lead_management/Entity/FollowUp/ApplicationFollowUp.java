package com.admission_crm.lead_management.Entity.FollowUp;


import com.admission_crm.lead_management.Entity.Application.Application;
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
@Table(name = "application_follow_ups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFollowUp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String applicationId;

    private String assignedTo;

    @Column(name = "follow_up_date", nullable = false)
    private LocalDateTime followUpDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowUpType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowUpStatus status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String outcome;

    @Column(name = "next_follow_up_date")
    private LocalDateTime nextFollowUpDate;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FollowUpType {
        CALL, EMAIL, SMS, MEETING, VISIT
    }

    public enum FollowUpStatus {
        PENDING, COMPLETED, CANCELLED, POSTPONED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
