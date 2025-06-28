package com.admission_crm.lead_management.Payload;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselorWorkload {
    private String counselorId;
    private String counselorName;
    private String counselorEmail;
    private Integer currentLeadCount;
    private Integer maxCapacity;
    private Double utilizationPercentage;
    private String status; // AVAILABLE, BUSY, ON_BREAK, OFFLINE
    private LocalDateTime lastLeadAssignedAt;
    private List<String> assignedLeadIds;
    private Double averageCompletionTime; // in hours
    private Integer completedLeadsToday;
    private Integer completedLeadsThisWeek;
    private Integer completedLeadsThisMonth;
}
