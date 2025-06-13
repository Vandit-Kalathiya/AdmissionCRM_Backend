package com.admission_crm.lead_management.Entity.FollowUp;

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
@Table(name = "follow_ups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

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