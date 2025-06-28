package com.admission_crm.lead_management.Payload.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityCreateRequest {
    @NotBlank(message = "University name is required")
    @Size(max = 100, message = "University name cannot exceed 100 characters")
    private String name;

    private String address;

    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]*$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^(https?://).*", message = "Website must start with http:// or https://")
    private String website;

    @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
    private String logoUrl;

    private List<String> admins;
}
