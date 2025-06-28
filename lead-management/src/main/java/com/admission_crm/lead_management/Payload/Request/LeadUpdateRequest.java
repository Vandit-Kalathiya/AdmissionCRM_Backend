package com.admission_crm.lead_management.Payload.Request;

import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadUpdateRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private String alternatePhone;
    private String city;
    private String state;
    private String address;
    private String qualification;
    private String budgetRange;
    private LeadStatus status;
    private Lead.LeadPriority priority;
    private String notes;
    private String courseInterestId;

}
