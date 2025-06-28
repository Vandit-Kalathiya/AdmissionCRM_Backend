package com.admissioncrm.authenticationservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private String id;
    private String name;
    private String code;
    private String description;
    private String duration;
    private BigDecimal fees;
    private String eligibility;
    private String institutionId;
    private String departmentId;
    private Boolean isActive;
    private String brochureUrl;
    private List<String> interestedLeads;
    private List<String> applications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}