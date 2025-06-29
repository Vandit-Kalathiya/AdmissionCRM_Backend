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
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
}
