package com.admissioncrm.authenticationservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String alternatePhone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pinCode;
    private String institutionId;
    private String leadSource;
    private String assignedCounselor;
    private String status;
    private Double leadScore;
    private Integer queuePosition;
    private String priority;
    private String qualification;
    private String courseInterestId;
    private String budgetRange;
    private List<String> communications;
    private List<String> followUps;
    private List<String> applications;
    private List<String> activities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
}
