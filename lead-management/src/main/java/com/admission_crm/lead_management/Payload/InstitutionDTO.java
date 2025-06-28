package com.admission_crm.lead_management.Payload;

import lombok.Data;

import java.util.Deque;
import java.util.List;

@Data
public class InstitutionDTO {

    private String id;
    private String name;
    private String instituteCode;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String universityId;
    private Integer maxCounselors;
    private Integer currentCounselors;
    private List<String> counselors;
    private List<String> courses;
    private List<String> departments;
    private List<String> leads;
    private Deque<String> queuedLeads;
}
