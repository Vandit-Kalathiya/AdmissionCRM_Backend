package com.admission_crm.lead_management.Entity.LeadManagement;

import com.admission_crm.lead_management.Entity.Academic.Course;
import com.admission_crm.lead_management.Entity.Application.Application;
import com.admission_crm.lead_management.Entity.Communication.Communication;
import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import com.admission_crm.lead_management.Entity.CoreEntities.User;
import com.admission_crm.lead_management.Entity.FollowUp.LeadFollowUp;
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
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "alternate_phone", length = 15)
    private String alternatePhone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 50)
    private String country;

    @Column(length = 10)
    private String pinCode;

    private String institutionId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "lead_source_id")
//    private LeadSource leadSource;

    private String assignedCounselor;

    private LeadStatus status;
    private Integer leadScore;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(length = 100)
    private String qualification;

    private String courseInterest;

    @Column(name = "budget_range", length = 50)
    private String budgetRange;

    @Column(name = "preferred_intake", length = 20)
    private String preferredIntake;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "custom_fields", columnDefinition = "JSON")
    private String customFields;

    @CollectionTable
    private List<String> communications = new ArrayList<>();

    @CollectionTable
    private List<String> followUps = new ArrayList<>();

    @CollectionTable
    private List<String> applications = new ArrayList<>();

    @CollectionTable
    private List<String> activities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}