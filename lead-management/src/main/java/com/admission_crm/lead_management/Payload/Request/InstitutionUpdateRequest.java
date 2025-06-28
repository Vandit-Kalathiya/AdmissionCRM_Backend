package com.admission_crm.lead_management.Payload.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionUpdateRequest {
    @Size(max = 100, message = "Institution name cannot exceed 100 characters")
    private String name;

    @Size(max = 10, message = "Institute code cannot exceed 10 characters")
    private String instituteCode;

    private String address;

    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]*$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^(https?://).*", message = "Website must start with http:// or https://")
    private String website;

    private String logoUrl;
    private String universityId;

    @Min(value = 1, message = "Maximum counselors must be at least 1")
    @Max(value = 50, message = "Maximum counselors cannot exceed 50")
    private Integer maxCounselors;

    private Boolean isActive;
}

