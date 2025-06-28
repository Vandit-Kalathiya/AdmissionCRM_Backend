package com.admission_crm.lead_management.Payload.Response;

import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String alternatePhone;
    private String city;
    private String state;
    private String country;
    private String address;
    private String qualification;
    private String budgetRange;
    private LocalDate dateOfBirth;
    private String gender;
    private String institutionId;
    private String institutionName;
    private String courseInterestId;
    private String courseInterestName;
    private String status;
    private String priority;
    private String source;
    private String assignedCounselorId;
    private String assignedCounselorName;
    private Double leadScore;
    private Integer queuePosition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    // Static method to convert Lead entity to LeadResponse
    public static LeadResponse fromEntity(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .alternatePhone(lead.getAlternatePhone())
                .city(lead.getCity())
                .state(lead.getState())
                .country(lead.getCountry())
                .address(lead.getAddress())
                .qualification(lead.getQualification())
                .budgetRange(lead.getBudgetRange())
                .dateOfBirth(lead.getDateOfBirth())
                .gender(lead.getGender() != null ? lead.getGender().name() : null)
                .institutionId(lead.getInstitutionId())
                .courseInterestId(lead.getCourseInterestId())
                .status(lead.getStatus() != null ? lead.getStatus().name() : null)
                .priority(lead.getPriority() != null ? lead.getPriority().name() : null)
                .source(lead.getLeadSource() != null ? lead.getLeadSource().name() : null)
                .assignedCounselorId(lead.getAssignedCounselor())
                .leadScore(lead.getLeadScore())
                .queuePosition(lead.getQueuePosition())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .assignedAt(lead.getAssignedAt())
                .completedAt(lead.getCompletedAt())
                .build();
    }
}

