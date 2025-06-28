package com.admission_crm.lead_management.Payload.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionResponseDTO {
    private String id;
    private String name;
    private String instituteCode;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private String universityId;
    private List<String> instituteAdmin;
    private Integer maxCounselors;
    private Integer currentCounselors;
    private Boolean isActive;
    private List<String> counselors;
    private List<String> departments;
    private List<String> courses;
    private List<String> leads;
    private Integer queueSize;
    private Integer availableCounselorSlots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
