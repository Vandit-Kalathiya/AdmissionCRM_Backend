package com.admission_crm.lead_management.Payload.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponseDTO {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private List<String> admins;
    private List<String> institutions;
    private Integer totalInstitutions;
    private Integer totalAdmins;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
