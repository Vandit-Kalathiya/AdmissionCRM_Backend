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
import lombok.Getter;
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

    // Fix: Remove @Column annotations for fields that follow naming convention
    // Hibernate will automatically map firstName -> first_name
    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 15)
    private String alternatePhone;

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

    @Enumerated(EnumType.STRING)
    private LeadSource leadSource;

    private String assignedCounselor;

    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;

    private Double leadScore = 0.0;

    private Integer queuePosition;

    @Enumerated(EnumType.STRING)
    private LeadPriority priority = LeadPriority.LOW;

    @Column(length = 100)
    private String qualification;

    private String courseInterestId;

    @Column(length = 50)
    private String budgetRange;

    @ElementCollection
    @CollectionTable(name = "lead_communications", joinColumns = @JoinColumn(name = "lead_id"))
    @Column(name = "communication_id")
    private List<String> communications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "lead_follow_ups", joinColumns = @JoinColumn(name = "lead_id"))
    @Column(name = "follow_up_id")
    private List<String> followUps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "lead_applications", joinColumns = @JoinColumn(name = "lead_id"))
    @Column(name = "application_id")
    private List<String> applications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "lead_activities", joinColumns = @JoinColumn(name = "lead_id"))
    @Column(name = "activity_id")
    private List<String> activities = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @Getter
    public enum LeadPriority {
        LOW(1), MEDIUM(2), HIGH(3), URGENT(4);

        private final int value;
        LeadPriority(int value) { this.value = value; }
    }

    public enum LeadSource {
        WEBSITE, REFERRAL, SOCIAL_MEDIA, ADVERTISEMENT, PHONE_CALL, WALK_IN, EMAIL_CAMPAIGN, OTHER
    }
}