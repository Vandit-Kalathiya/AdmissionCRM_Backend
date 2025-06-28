package com.admission_crm.lead_management.Payload.Response;

import com.admission_crm.lead_management.Payload.LeadQueueInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueStatusResponse {
    private String institutionId;
    private String institutionName;
    private Integer totalLeadsInQueue;
    private Integer availableCounselors;
    private Integer busyCounselors;
    private Double averageWaitingTime; // in minutes
    private String estimatedProcessingTime;
    private List<LeadQueueInfo> queuedLeads;
    private LocalDateTime lastUpdated;

    public static QueueStatusResponse create(String institutionId, String institutionName,
                                             List<LeadQueueInfo> queuedLeads, Integer availableCounselors,
                                             Integer busyCounselors) {
//        double avgWaitTime = queuedLeads.stream()
//                .mapToLong(LeadQueueInfo::getWaitingTimeInMinutes)
//                .average()
//                .orElse(0.0);

        return QueueStatusResponse.builder()
                .institutionId(institutionId)
                .institutionName(institutionName)
                .totalLeadsInQueue(queuedLeads.size())
                .availableCounselors(availableCounselors)
                .busyCounselors(busyCounselors)
                .estimatedProcessingTime(calculateEstimatedProcessingTime(queuedLeads.size(), availableCounselors))
                .queuedLeads(queuedLeads)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private static String calculateEstimatedProcessingTime(int queueSize, int availableCounselors) {
        if (availableCounselors == 0) return "Waiting for counselor availability";

        // Assume average 30 minutes per lead processing
        int avgProcessingTimeMinutes = 30;
        int totalMinutes = (queueSize * avgProcessingTimeMinutes) / Math.max(1, availableCounselors);

        if (totalMinutes < 60) {
            return totalMinutes + " minutes";
        } else {
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            return hours + " hours " + (minutes > 0 ? minutes + " minutes" : "");
        }
    }
}
