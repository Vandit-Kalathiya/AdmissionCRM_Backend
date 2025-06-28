package com.admission_crm.lead_management.Payload;

import lombok.Data;

import java.util.List;

@Data
public class UniversityDTO {

    private String id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private List<String> admins;
    private List<String> institutions;
}
