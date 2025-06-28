package com.admission_crm.lead_management.Entity.Application;

import com.admission_crm.lead_management.Entity.Academic.Course;
import com.admission_crm.lead_management.Entity.Academic.Intake;
import com.admission_crm.lead_management.Entity.FollowUp.ApplicationFollowUp;
import com.admission_crm.lead_management.Entity.FollowUp.LeadFollowUp;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String leadId;

    @Column(name = "application_number", unique = true, length = 20)
    private String applicationNumber;

    private String courseId;

    private String intakeId;

    @CollectionTable
    private List<String> followUps = new ArrayList<>();

    private ApplicationStatus applicationStatus;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Enumerated(EnumType.STRING)
    private ApplicationDecision decision;

    @Column(name = "decision_notes", columnDefinition = "TEXT")
    private String decisionNotes;

    @Column(columnDefinition = "JSON")
    private String documents;

    @Column(name = "fees_paid", precision = 10, scale = 2)
    private BigDecimal feesPaid;

    @Column(name = "total_fees", precision = 10, scale = 2)
    private BigDecimal totalFees;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @ElementCollection
    private List<String> uploadedDocuments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ApplicationDecision {
        PENDING, ACCEPTED, REJECTED, WAITLISTED
    }

    public enum PaymentStatus {
        PENDING, PARTIAL, PAID, REFUNDED
    }
}
