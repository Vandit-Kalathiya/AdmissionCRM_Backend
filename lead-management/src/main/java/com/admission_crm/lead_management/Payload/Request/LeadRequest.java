package com.admission_crm.lead_management.Payload.Request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeadRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String state;
    private String alternatePhone;
    private String courseInterested;
    private String source;
    private String status;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String pinCode;
    private String institutionId;
    private String qualification;
    private String budgetRange;

}
