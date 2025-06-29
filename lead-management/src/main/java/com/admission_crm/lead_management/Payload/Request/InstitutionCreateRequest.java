package com.admission_crm.lead_management.Payload.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionCreateRequest {
    private String name;
    private String instituteCode;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private String universityId;
}
