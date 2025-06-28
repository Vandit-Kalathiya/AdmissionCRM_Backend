package com.admission_crm.lead_management.Controller;


import com.admission_crm.lead_management.Payload.Response.ApiResponse;
import com.admission_crm.lead_management.Payload.LeadQueueInfo;
import com.admission_crm.lead_management.Service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QueueController {

    private final LeadService leadService;

    /**
     * Get queue status for institution
     */
    @GetMapping("/status/{institutionId}")
    public ResponseEntity<?> getQueueStatus(@PathVariable String institutionId) {
        try {
            List<LeadQueueInfo> queueStatus = leadService.getQueueStatus(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Queue status retrieved successfully", queueStatus));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving queue status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve queue status", "An unexpected error occurred"));
        }
    }

    /**
     * Get queue size for institution
     */
    @GetMapping("/size/{institutionId}")
    public ResponseEntity<?> getQueueSize(@PathVariable String institutionId) {
        try {
            int queueSize = leadService.getQueueSize(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Queue size retrieved successfully",
                    Map.of("queueSize", queueSize)));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving queue size: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve queue size", "An unexpected error occurred"));
        }
    }

    /**
     * Move lead to different position in queue
     */
    @PutMapping("/move/{leadId}")
    public ResponseEntity<?> moveLeadInQueue(@PathVariable String leadId,
                                             @RequestParam int newPosition,
                                             Authentication authentication) {
        try {
            leadService.moveLeadInQueue(leadId, newPosition, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead moved in queue successfully", null));
        } catch (IllegalStateException e) {
            log.warn("Invalid lead state for queue operation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid lead state", e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                log.warn("Lead or institution not found: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Resource not found", e.getMessage()));
            }
            log.warn("Invalid queue operation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid operation", e.getMessage()));
        } catch (Exception e) {
            log.error("Error moving lead in queue: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to move lead in queue", "An unexpected error occurred"));
        }
    }

    /**
     * Get queue waiting time estimates
     */
    @GetMapping("/wait-times/{institutionId}")
    public ResponseEntity<?> getQueueWaitingTimeEstimates(@PathVariable String institutionId) {
        try {
            Map<Integer, String> waitingTimes = leadService.getQueueWaitingTimeEstimates(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Queue waiting times retrieved successfully", waitingTimes));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving queue waiting times: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve waiting times", "An unexpected error occurred"));
        }
    }

    /**
     * Get queue health check
     */
    @GetMapping("/health/{institutionId}")
    public ResponseEntity<?> getQueueHealthCheck(@PathVariable String institutionId) {
        try {
            Map<String, Object> healthCheck = leadService.getQueueHealthCheck(institutionId);

            String status = (String) healthCheck.get("status");
            if ("ERROR".equals(status)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Queue health check failed", status));
            } else if ("WARNING".equals(status)) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(ApiResponse.warning("Queue health check warning", healthCheck));
            }

            return ResponseEntity.ok(ApiResponse.success("Queue health check passed", healthCheck));
        } catch (Exception e) {
            log.error("Error performing queue health check: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Health check failed", "An unexpected error occurred"));
        }
    }

    /**
     * Get queue analytics for institution
     */
    @GetMapping("/analytics/{institutionId}")
    public ResponseEntity<?> getQueueAnalytics(@PathVariable String institutionId) {
        try {
            // Combine multiple queue-related metrics
            List<LeadQueueInfo> queueStatus = leadService.getQueueStatus(institutionId);
            int queueSize = leadService.getQueueSize(institutionId);
            Map<Integer, String> waitingTimes = leadService.getQueueWaitingTimeEstimates(institutionId);

            Map<String, Object> analytics = Map.of(
                    "queueStatus", queueStatus,
                    "queueSize", queueSize,
                    "waitingTimes", waitingTimes,
                    "queueTrend", "stable" // This could be calculated based on historical data
            );

            return ResponseEntity.ok(ApiResponse.success("Queue analytics retrieved successfully", analytics));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving queue analytics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve queue analytics", "An unexpected error occurred"));
        }
    }

    /**
     * Clear completed leads from queue (maintenance operation)
     */
    @PostMapping("/cleanup/{institutionId}")
    public ResponseEntity<?> cleanupQueue(@PathVariable String institutionId,
                                          Authentication authentication) {
        try {
            // This would be a maintenance operation to clean up any inconsistencies
            Map<String, Object> healthCheck = leadService.getQueueHealthCheck(institutionId);

            if (!"HEALTHY".equals(healthCheck.get("status"))) {
                // Perform cleanup operations here
                log.info("Queue cleanup initiated for institution: {} by user: {}",
                        institutionId, authentication.getName());

                return ResponseEntity.ok(ApiResponse.success("Queue cleanup initiated",
                        Map.of("message", "Queue cleanup process started")));
            }

            return ResponseEntity.ok(ApiResponse.success("Queue is healthy, no cleanup needed", null));
        } catch (RuntimeException e) {
            log.warn("Institution not found for cleanup: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error during queue cleanup: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Queue cleanup failed", "An unexpected error occurred"));
        }
    }

    /**
     * Get queue performance metrics
     */
    /**
     * Get queue performance metrics
     */
    @GetMapping("/metrics/{institutionId}")
    public ResponseEntity<?> getQueueMetrics(@PathVariable String institutionId,
                                             @RequestParam(defaultValue = "7") int days) {
        try {
            // Get a current queue size
            int currentQueueSize = leadService.getQueueSize(institutionId);

            // Get daily stats as Map<LocalDate, Long>
            Map<LocalDate, Long> dailyStatsRaw = leadService.getDailyLeadCreationStats(institutionId, days);

            // Convert LocalDate keys to String for JSON serialization
            Map<String, Long> dailyStats = dailyStatsRaw.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().toString(), // Convert LocalDate to String
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new // Maintain order
                    ));

            // Calculate metrics
            double averageDailyLeads = dailyStatsRaw.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            String peakDay = dailyStatsRaw.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entry -> entry.getKey().toString()) // Convert LocalDate to String
                    .orElse("N/A");

            long totalLeadsProcessed = dailyStatsRaw.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();

            // Create metrics map with proper types
            Map<String, Object> metrics = Map.of(
                    "currentQueueSize", currentQueueSize,
                    "dailyLeadCreation", dailyStats, // Now Map<String, Long>
                    "averageDailyLeads", Math.round(averageDailyLeads * 100.0) / 100.0,
                    "peakDay", peakDay, // Now String
                    "totalLeadsProcessed", totalLeadsProcessed
            );

            return ResponseEntity.ok(ApiResponse.success("Queue metrics retrieved successfully", metrics));
        } catch (RuntimeException e) {
            log.warn("Institution not found for metrics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving queue metrics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve queue metrics", "An unexpected error occurred"));
        }
    }
}
