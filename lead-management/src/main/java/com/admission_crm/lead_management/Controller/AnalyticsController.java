package com.admission_crm.lead_management.Controller;

import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import com.admission_crm.lead_management.Payload.Response.ApiResponse;
import com.admission_crm.lead_management.Payload.CounselorWorkload;
import com.admission_crm.lead_management.Payload.Response.LeadResponse;
import com.admission_crm.lead_management.Payload.LeadStatistics;
import com.admission_crm.lead_management.Service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final LeadService leadService;

    /**
     * Get comprehensive lead statistics for an institution
     */
    @GetMapping("/statistics/{institutionId}")
    public ResponseEntity<?> getLeadStatistics(@PathVariable String institutionId) {
        try {
            LeadStatistics statistics = leadService.getLeadStatistics(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Lead statistics retrieved successfully", statistics));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving lead statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve lead statistics", "An unexpected error occurred"));
        }
    }

    /**
     * Get lead conversion funnel data
     */
    @GetMapping("/funnel/{institutionId}")
    public ResponseEntity<?> getLeadConversionFunnel(@PathVariable String institutionId) {
        try {
            Map<String, Long> funnel = leadService.getLeadConversionFunnel(institutionId);

            // Calculate conversion rates between stages
            Map<String, Object> funnelWithRates = calculateConversionRates(funnel);

            return ResponseEntity.ok(ApiResponse.success("Lead conversion funnel retrieved successfully", funnelWithRates));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving lead conversion funnel: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve conversion funnel", "An unexpected error occurred"));
        }
    }

    /**
     * Get daily lead creation statistics
     */
    @GetMapping("/daily-stats/{institutionId}")
    public ResponseEntity<?> getDailyLeadCreationStats(@PathVariable String institutionId,
                                                       @RequestParam(defaultValue = "30") int days) {
        try {
            if (days < 1 || days > 365) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid days parameter", "Days must be between 1 and 365"));
            }

            Map<String, Long> dailyStats = leadService.getDailyLeadCreationStats(institutionId, days)
                    .entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));

            // Calculate additional metrics
            double averageDaily = dailyStats.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            long totalLeads = dailyStats.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();

            String peakDay = dailyStats.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            Map<String, Object> response = Map.of(
                    "dailyStats", dailyStats,
                    "summary", Map.of(
                            "totalLeads", totalLeads,
                            "averageDaily", Math.round(averageDaily * 100.0) / 100.0,
                            "peakDay", peakDay,
                            "periodDays", days
                    )
            );

            return ResponseEntity.ok(ApiResponse.success("Daily lead statistics retrieved successfully", response));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving daily lead statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve daily statistics", "An unexpected error occurred"));
        }
    }

    /**
     * Export leads data with filtering options
     */
    @GetMapping("/export/{institutionId}")
    public ResponseEntity<?> exportLeadsData(@PathVariable String institutionId,
                                             @RequestParam(required = false) LeadStatus status,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        try {
            List<LeadResponse> leadsData = leadService.exportLeadsData(institutionId, status, fromDate, toDate);

            Map<String, Object> exportData = Map.of(
                    "leads", leadsData,
                    "exportInfo", Map.of(
                            "totalRecords", leadsData.size(),
                            "institutionId", institutionId,
                            "status", status != null ? status.toString() : "ALL",
                            "fromDate", fromDate != null ? fromDate.toString() : "N/A",
                            "toDate", toDate != null ? toDate.toString() : "N/A",
                            "exportedAt", LocalDateTime.now().toString()
                    )
            );

            return ResponseEntity.ok(ApiResponse.success("Leads data exported successfully", exportData));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error exporting leads data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to export leads data", "An unexpected error occurred"));
        }
    }

    /**
     * Get performance dashboard data
     */
    @GetMapping("/dashboard/{institutionId}")
    public ResponseEntity<?> getPerformanceDashboard(@PathVariable String institutionId,
                                                     @RequestParam(defaultValue = "30") int days) {
        try {
            // Get various analytics data
            LeadStatistics statistics = leadService.getLeadStatistics(institutionId);
            Map<String, Long> funnel = leadService.getLeadConversionFunnel(institutionId);

            // Get daily stats as Map<LocalDate, Long> and convert to Map<String, Long>
            Map<LocalDate, Long> dailyStatsRaw = leadService.getDailyLeadCreationStats(institutionId, days);
            Map<String, Long> dailyStats = dailyStatsRaw.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            entry -> entry.getKey().toString(), // Convert LocalDate to String
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new // Maintain chronological order
                    ));

            var counselorWorkloads = leadService.getCounselorWorkloads(institutionId);

            // Calculate trending data using the raw LocalDate map for calculations
            double conversionTrend = calculateConversionTrend(dailyStats);

            // Calculate average daily leads using raw data
            double averageDaily = dailyStatsRaw.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            Map<String, Object> dashboard = Map.of(
                    "overview", Map.of(
                            "totalLeads", statistics.getTotalLeads(),
                            "conversionRate", statistics.getConversionRate(),
                            "newLeads", statistics.getNewLeads(),
                            "convertedLeads", statistics.getConvertedLeads()
                    ),
                    "funnel", funnel,
                    "trends", Map.of(
                            "dailyStats", dailyStats, // Now properly converted to Map<String, Long>
                            "conversionTrend", Math.round(conversionTrend * 100.0) / 100.0,
                            "averageDaily", Math.round(averageDaily * 100.0) / 100.0
                    ),
                    "counselorSummary", Map.of(
                            "totalCounselors", counselorWorkloads.size(),
                            "availableCounselors", counselorWorkloads.stream()
                                    .filter(w -> "AVAILABLE".equals(w.getStatus()))
                                    .count(),
                            "averageUtilization", Math.round(counselorWorkloads.stream()
                                    .mapToDouble(CounselorWorkload::getUtilizationPercentage)
                                    .average()
                                    .orElse(0.0) * 100.0) / 100.0
                    ),
                    "generatedAt", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success("Performance dashboard data retrieved successfully", dashboard));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving performance dashboard: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve performance dashboard", "An unexpected error occurred"));
        }
    }

    /**
     * Calculate conversion trend (updated to handle LocalDate keys)
     */
    private double calculateConversionTrend(Map<String, Long> dailyStats) {
        // Simple trend calculation - in reality, you'd want more sophisticated analysis
        var values = dailyStats.values().stream().mapToLong(Long::longValue).toArray();
        if (values.length < 2) return 0.0;

        long recent = java.util.Arrays.stream(values, Math.max(0, values.length - 7), values.length).sum();
        long previous = java.util.Arrays.stream(values, Math.max(0, values.length - 14), Math.max(0, values.length - 7)).sum();

        if (previous == 0) return 0.0;
        return ((double) (recent - previous) / previous) * 100.0;
    }

    /**
     * Get lead source analysis
     */
    @GetMapping("/sources/{institutionId}")
    public ResponseEntity<?> getLeadSourceAnalysis(@PathVariable String institutionId,
                                                   @RequestParam(defaultValue = "30") int days) {
        try {
            // This would require additional service methods, or we can derive from existing data
            List<LeadResponse> leads = leadService.exportLeadsData(institutionId, null,
                    LocalDateTime.now().minusDays(days), LocalDateTime.now());

            Map<String, Long> sourceCount = leads.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            lead -> lead.getSource() != null ? lead.getSource() : "UNKNOWN",
                            java.util.stream.Collectors.counting()
                    ));

            // Calculate conversion rates by source
            Map<String, Double> sourceConversion = leads.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            lead -> lead.getSource() != null ? lead.getSource() : "UNKNOWN",
                            java.util.stream.Collectors.averagingDouble(lead ->
                                    "COMPLETED".equals(lead.getStatus()) ? 1.0 : 0.0)
                    ));

            Map<String, Object> analysis = Map.of(
                    "sourceDistribution", sourceCount,
                    "sourceConversionRates", sourceConversion,
                    "totalLeads", leads.size(),
                    "periodDays", days,
                    "topPerformingSource", sourceConversion.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A")
            );

            return ResponseEntity.ok(ApiResponse.success("Lead source analysis retrieved successfully", analysis));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving lead source analysis: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve source analysis", "An unexpected error occurred"));
        }
    }

    /**
     * Get counselor performance analytics
     */
    @GetMapping("/counselor-performance/{institutionId}")
    public ResponseEntity<?> getCounselorPerformanceAnalytics(@PathVariable String institutionId,
                                                              @RequestParam(defaultValue = "30") int days) {
        try {
            var counselorWorkloads = leadService.getCounselorWorkloads(institutionId);

            // Get detailed performance for each counselor
            List<Map<String, Object>> counselorPerformance = counselorWorkloads.stream()
                    .map(workload -> {
                        // Get counselor's leads for performance calculation
                        var counselorLeads = leadService.getLeadsByCounselor(workload.getCounselorId(),
                                org.springframework.data.domain.Pageable.unpaged());

                        long totalHandled = counselorLeads.getTotalElements();
                        long completed = counselorLeads.getContent().stream()
                                .filter(lead -> "COMPLETED".equals(lead.getStatus().name()) ||
                                        "REJECTED".equals(lead.getStatus().name()))
                                .count();

                        double completionRate = totalHandled > 0 ? (double) completed / totalHandled * 100 : 0.0;

                        return Map.<String, Object>of(
                                "counselorId", workload.getCounselorId(),
                                "counselorName", workload.getCounselorName(),
                                "currentLoad", workload.getCurrentLeadCount(),
                                "utilization", workload.getUtilizationPercentage(),
                                "totalHandled", totalHandled,
                                "completionRate", Math.round(completionRate * 100.0) / 100.0,
                                "status", workload.getStatus()
                        );
                    })
                    .toList();

            // Calculate team averages
            double avgUtilization = counselorPerformance.stream()
                    .mapToDouble(cp -> (Double) cp.get("utilization"))
                    .average()
                    .orElse(0.0);

            double avgCompletionRate = counselorPerformance.stream()
                    .mapToDouble(cp -> (Double) cp.get("completionRate"))
                    .average()
                    .orElse(0.0);

            Map<String, Object> analytics = Map.of(
                    "counselorPerformance", counselorPerformance,
                    "teamAverages", Map.of(
                            "averageUtilization", Math.round(avgUtilization * 100.0) / 100.0,
                            "averageCompletionRate", Math.round(avgCompletionRate * 100.0) / 100.0,
                            "totalCounselors", counselorPerformance.size()
                    ),
                    "topPerformer", counselorPerformance.stream()
                            .max(java.util.Comparator.comparingDouble(cp -> (Double) cp.get("completionRate")))
                            .map(cp -> cp.get("counselorName"))
                            .orElse("N/A")
            );

            return ResponseEntity.ok(ApiResponse.success("Counselor performance analytics retrieved successfully", analytics));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving counselor performance analytics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor analytics", "An unexpected error occurred"));
        }
    }

    /**
     * Get time-based analytics (hourly/daily patterns)
     */
    @GetMapping("/time-patterns/{institutionId}")
    public ResponseEntity<?> getTimeBasedAnalytics(@PathVariable String institutionId,
                                                   @RequestParam(defaultValue = "30") int days) {
        try {
            List<LeadResponse> leads = leadService.exportLeadsData(institutionId, null,
                    LocalDateTime.now().minusDays(days), LocalDateTime.now());

            // Group by hour of days
            Map<Integer, Long> hourlyDistribution = leads.stream()
                    .filter(lead -> lead.getCreatedAt() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            lead -> java.time.LocalDateTime.parse(lead.getCreatedAt().toString()).getHour(),
                            java.util.stream.Collectors.counting()
                    ));

            // Group by day of weeks
            Map<String, Long> weeklyDistribution = leads.stream()
                    .filter(lead -> lead.getCreatedAt() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            lead -> java.time.LocalDateTime.parse(lead.getCreatedAt().toString()).getDayOfWeek().toString(),
                            java.util.stream.Collectors.counting()
                    ));

            // Find peak hours and days
            String peakHour = hourlyDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entry -> entry.getKey() + ":00")
                    .orElse("N/A");

            String peakDay = weeklyDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            Map<String, Object> timeAnalytics = Map.of(
                    "hourlyDistribution", hourlyDistribution,
                    "weeklyDistribution", weeklyDistribution,
                    "insights", Map.of(
                            "peakHour", peakHour,
                            "peakDay", peakDay,
                            "totalLeadsAnalyzed", leads.size(),
                            "periodDays", days
                    )
            );

            return ResponseEntity.ok(ApiResponse.success("Time-based analytics retrieved successfully", timeAnalytics));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving time-based analytics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve time analytics", "An unexpected error occurred"));
        }
    }

    /**
     * Get real-time analytics summary
     */
    @GetMapping("/realtime/{institutionId}")
    public ResponseEntity<?> getRealtimeAnalytics(@PathVariable String institutionId) {
        try {
            LeadStatistics stats = leadService.getLeadStatistics(institutionId);
            var queueStatus = leadService.getQueueStatus(institutionId);
            var counselorWorkloads = leadService.getCounselorWorkloads(institutionId);

            Map<String, Object> realtime = Map.of(
                    "currentStats", Map.of(
                            "totalLeads", stats.getTotalLeads(),
                            "newLeads", stats.getNewLeads(),
                            "queuedLeads", stats.getQueuedLeads(),
                            "assignedLeads", stats.getAssignedLeads()
                    ),
                    "queueInfo", Map.of(
                            "currentQueueSize", queueStatus.size(),
                            "nextInQueue", queueStatus.isEmpty() ? "None" : queueStatus.getFirst().getLeadName()
                    ),
                    "counselorStatus", Map.of(
                            "totalCounselors", counselorWorkloads.size(),
                            "availableCounselors", counselorWorkloads.stream()
                                    .filter(w -> "AVAILABLE".equals(w.getStatus()))
                                    .count(),
                            "busyCounselors", counselorWorkloads.stream()
                                    .filter(w -> "BUSY".equals(w.getStatus()))
                                    .count()
                    ),
                    "timestamp", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success("Real-time analytics retrieved successfully", realtime));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving real-time analytics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve real-time analytics", "An unexpected error occurred"));
        }
    }

    /**
     * Clean up old completed leads (maintenance operation)
     */
    @PostMapping("/cleanup/old-leads")
    public ResponseEntity<?> cleanupOldCompletedLeads(@RequestParam(defaultValue = "90") int daysOld,
                                                      Authentication authentication) {
        try {
            if (daysOld < 30) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid days parameter", "Days must be at least 30 for safety"));
            }

            int cleanedCount = leadService.cleanupOldCompletedLeads(daysOld);

            log.info("Cleaned up {} old completed leads (older than {} days) by user: {}",
                    cleanedCount, daysOld, authentication.getName());

            Map<String, Object> result = Map.of(
                    "cleanedLeadsCount", cleanedCount,
                    "daysOld", daysOld,
                    "cleanupDate", LocalDateTime.now().toString()
            );

            return ResponseEntity.ok(ApiResponse.success("Old leads cleanup completed successfully", result));
        } catch (Exception e) {
            log.error("Error during old leads cleanup: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cleanup old leads", "An unexpected error occurred"));
        }
    }

    // Helper methods

    /**
     * Calculate conversion rates between funnel stages
     */
    private Map<String, Object> calculateConversionRates(Map<String, Long> funnel) {
        long totalLeads = funnel.getOrDefault("NEW", 0L) + funnel.getOrDefault("QUEUED", 0L) +
                funnel.getOrDefault("ASSIGNED", 0L) + funnel.getOrDefault("IN_PROGRESS", 0L) +
                funnel.getOrDefault("CONTACTED", 0L) + funnel.getOrDefault("QUALIFIED", 0L) +
                funnel.getOrDefault("CONVERTED", 0L) + funnel.getOrDefault("REJECTED", 0L);

        Map<String, Object> result = new java.util.HashMap<>(funnel);

        if (totalLeads > 0) {
            result.put("conversionRates", Map.of(
                    "newToQueued", calculateRate(funnel.getOrDefault("QUEUED", 0L), funnel.getOrDefault("NEW", 0L)),
                    "queuedToAssigned", calculateRate(funnel.getOrDefault("ASSIGNED", 0L), funnel.getOrDefault("QUEUED", 0L)),
                    "assignedToContacted", calculateRate(funnel.getOrDefault("CONTACTED", 0L), funnel.getOrDefault("ASSIGNED", 0L)),
                    "contactedToConverted", calculateRate(funnel.getOrDefault("CONVERTED", 0L), funnel.getOrDefault("CONTACTED", 0L)),
                    "overallConversion", calculateRate(funnel.getOrDefault("CONVERTED", 0L), totalLeads)
            ));
        }

        return result;
    }

    /**
     * Calculate conversion rate percentage
     */
    private double calculateRate(long numerator, long denominator) {
        if (denominator == 0) return 0.0;
        return Math.round(((double) numerator / denominator) * 10000.0) / 100.0;
    }
}