package com.admission_crm.lead_management.Payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadQueueInfo {
    private Integer position;
    private String leadId;
    private String leadName;
    private String email;
    private String phone;
    private Double leadScore;
    private String priority;
    private String courseInterest;
    private String source;

    // Constructor for basic queue info
    public LeadQueueInfo(Integer position, String leadName, String email, Double leadScore, String priority) {
        this.position = position;
        this.leadName = leadName;
        this.email = email;
        this.leadScore = leadScore;
        this.priority = priority;
    }

    // Constructor with lead ID
    public LeadQueueInfo(Integer position, String leadId, String leadName, String email, Double leadScore, String priority) {
        this(position, leadName, email, leadScore, priority);
        this.leadId = leadId;
    }
}

