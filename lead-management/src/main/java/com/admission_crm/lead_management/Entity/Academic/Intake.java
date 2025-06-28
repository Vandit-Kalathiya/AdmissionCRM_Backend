package com.admission_crm.lead_management.Entity.Academic;


import com.admission_crm.lead_management.Entity.Application.Application;
import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intakes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Intake {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 50)
    private String name; // Fall 2024, Spring 2025

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_enrolled")
    private Integer currentEnrolled = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private String institutionId;

    @ElementCollection
    private List<String> applications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isCapacityAvailable() {
        return currentEnrolled < maxCapacity;
    }

    public boolean isApplicationOpen() {
        return applicationDeadline.isAfter(LocalDate.now());
    }
}
