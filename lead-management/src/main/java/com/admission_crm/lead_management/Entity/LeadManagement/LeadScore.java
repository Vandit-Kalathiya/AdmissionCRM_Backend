package com.admission_crm.lead_management.Entity.LeadManagement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "lead_id")
    private String leadId;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "demographic_score")
    private Integer demographicScore;

    @Column(name = "engagement_score")
    private Integer engagementScore;

    @Column(name = "academic_score")
    private Integer academicScore;

    @Column(name = "financial_score")
    private Integer financialScore;

    @Column(name = "behavioral_score")
    private Integer behavioralScore;

    @Column(name = "score_factors", columnDefinition = "JSON")
    private String scoreFactors;

    @Column(name = "last_calculated")
    private LocalDateTime lastCalculated;

    @Column(name = "calculation_version", length = 10)
    private String calculationVersion;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Column(name = "prediction_notes", columnDefinition = "TEXT")
    private String predictionNotes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}