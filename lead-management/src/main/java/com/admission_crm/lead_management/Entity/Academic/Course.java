package com.admission_crm.lead_management.Entity.Academic;

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
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 20)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String duration;

    @Column(precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    private String institutionId;

    private String departmentId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "brochure_url", length = 255)
    private String brochureUrl;

    @ElementCollection
    private List<String> interestedLeads = new ArrayList<>();

    @ElementCollection
    private List<String> applications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
