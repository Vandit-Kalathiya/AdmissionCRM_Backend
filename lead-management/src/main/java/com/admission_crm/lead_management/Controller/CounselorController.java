package com.admission_crm.lead_management.Controller;

import com.admission_crm.lead_management.Payload.Response.ApiResponse;
import com.admission_crm.lead_management.Payload.CounselorWorkload;
import com.admission_crm.lead_management.Payload.Response.LeadResponse;
import com.admission_crm.lead_management.Service.LeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CounselorController {

    private final LeadService leadService;

    /**
     * Get counselor workloads for institution
     */
    @GetMapping("/workload/{institutionId}")
    public ResponseEntity<?> getCounselorWorkloads(@PathVariable String institutionId) {
        try {
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Counselor workloads retrieved successfully", workloads));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving counselor workloads: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor workloads", "An unexpected error occurred"));
        }
    }

    /**
     * Get individual counselor workload
     */
    @GetMapping("/{counselorId}/workload")
    public ResponseEntity<?> getCounselorWorkload(@PathVariable String counselorId,
                                                  @RequestParam String institutionId) {
        try {
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);
            CounselorWorkload counselorWorkload = workloads.stream()
                    .filter(w -> w.getCounselorId().equals(counselorId))
                    .findFirst()
                    .orElse(null);

            if (counselorWorkload == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Counselor not found", "Counselor not found in this institution"));
            }

            return ResponseEntity.ok(ApiResponse.success("Counselor workload retrieved successfully", counselorWorkload));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving counselor workload: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor workload", "An unexpected error occurred"));
        }
    }

    /**
     * Get available counselors for institution
     */
    @GetMapping("/available/{institutionId}")
    public ResponseEntity<?> getAvailableCounselors(@PathVariable String institutionId) {
        try {
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);
            List<CounselorWorkload> availableCounselors = workloads.stream()
                    .filter(w -> "AVAILABLE".equals(w.getStatus()))
                    .toList();

            Map<String, Object> response = Map.of(
                    "availableCounselors", availableCounselors,
                    "totalAvailable", availableCounselors.size(),
                    "totalCounselors", workloads.size()
            );

            return ResponseEntity.ok(ApiResponse.success("Available counselors retrieved successfully", response));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving available counselors: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available counselors", "An unexpected error occurred"));
        }
    }

    /**
     * Get counselor performance summary
     */
    @GetMapping("/{counselorId}/performance")
    public ResponseEntity<?> getCounselorPerformance(@PathVariable String counselorId,
                                                     @RequestParam String institutionId,
                                                     @RequestParam(defaultValue = "30") int days) {
        try {
            // Get counselor's leads
            var counselorLeads = leadService.getLeadsByCounselor(counselorId,
                    org.springframework.data.domain.Pageable.unpaged());

            // Get workload information
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);
            CounselorWorkload counselorWorkload = workloads.stream()
                    .filter(w -> w.getCounselorId().equals(counselorId))
                    .findFirst()
                    .orElse(null);

            if (counselorWorkload == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Counselor not found", "Counselor not found in this institution"));
            }

            // Calculate performance metrics
            long totalLeads = counselorLeads.getTotalElements();
            long completedLeads = counselorLeads.getContent().stream()
                    .filter(lead -> lead.getStatus().name().contains("COMPLETED") ||
                            lead.getStatus().name().contains("REJECTED"))
                    .count();

            double completionRate = totalLeads > 0 ? (double) completedLeads / totalLeads * 100 : 0.0;

            Map<String, Object> performance = Map.of(
                    "counselorInfo", counselorWorkload,
                    "totalLeadsHandled", totalLeads,
                    "completedLeads", completedLeads,
                    "activeLeads", counselorWorkload.getCurrentLeadCount(),
                    "completionRate", Math.round(completionRate * 100.0) / 100.0,
                    "utilizationPercentage", counselorWorkload.getUtilizationPercentage(),
                    "status", counselorWorkload.getStatus(),
                    "capacity", counselorWorkload.getMaxCapacity()
            );

            return ResponseEntity.ok(ApiResponse.success("Counselor performance retrieved successfully", performance));
        } catch (RuntimeException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resource not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving counselor performance: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor performance", "An unexpected error occurred"));
        }
    }

    /**
     * Get counselor dashboard data
     */
    @GetMapping("/{counselorId}/dashboard")
    public ResponseEntity<?> getCounselorDashboard(@PathVariable String counselorId,
                                                   @RequestParam String institutionId) {
        try {
            // Get counselor's current leads
            var counselorLeads = leadService.getLeadsByCounselor(counselorId,
                    org.springframework.data.domain.PageRequest.of(0, 100));

            // Get workload
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);
            CounselorWorkload workload = workloads.stream()
                    .filter(w -> w.getCounselorId().equals(counselorId))
                    .findFirst()
                    .orElse(null);

            if (workload == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Counselor not found", "Counselor not found in this institution"));
            }

            // Count leads by status
            Map<String, Long> leadsByStatus = counselorLeads.getContent().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            lead -> lead.getStatus().name(),
                            java.util.stream.Collectors.counting()
                    ));

            // Get pending follow-ups (this would require additional service method)
            long pendingFollowUps = counselorLeads.getContent().stream()
                    .filter(lead -> lead.getStatus().name().equals("FOLLOW_UP"))
                    .count();

            Map<String, Object> dashboard = Map.of(
                    "counselorInfo", Map.of(
                            "id", workload.getCounselorId(),
                            "name", workload.getCounselorName(),
                            "email", workload.getCounselorEmail()
                    ),
                    "workloadSummary", Map.of(
                            "currentLeads", workload.getCurrentLeadCount(),
                            "maxCapacity", workload.getMaxCapacity(),
                            "utilization", workload.getUtilizationPercentage(),
                            "status", workload.getStatus()
                    ),
                    "leadsByStatus", leadsByStatus,
                    "pendingFollowUps", pendingFollowUps,
                    "recentLeads", counselorLeads.getContent().stream()
                            .limit(5)
                            .map(lead -> Map.of(
                                    "id", lead.getId(),
                                    "name", lead.getFirstName() + " " + lead.getLastName(),
                                    "email", lead.getEmail(),
                                    "status", lead.getStatus().name(),
                                    "assignedAt", lead.getAssignedAt() != null ? lead.getAssignedAt().toString() : "N/A"
                            ))
                            .toList()
            );

            return ResponseEntity.ok(ApiResponse.success("Counselor dashboard data retrieved successfully", dashboard));
        } catch (RuntimeException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Resource not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving counselor dashboard: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor dashboard", "An unexpected error occurred"));
        }
    }

    /**
     * Get counselor capacity status
     */
    @GetMapping("/{counselorId}/capacity")
    public ResponseEntity<?> getCounselorCapacity(@PathVariable String counselorId,
                                                  @RequestParam String institutionId) {
        try {
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);
            CounselorWorkload workload = workloads.stream()
                    .filter(w -> w.getCounselorId().equals(counselorId))
                    .findFirst()
                    .orElse(null);

            if (workload == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Counselor not found", "Counselor not found in this institution"));
            }

            Map<String, Object> capacity = Map.of(
                    "counselorId", counselorId,
                    "currentLoad", workload.getCurrentLeadCount(),
                    "maxCapacity", workload.getMaxCapacity(),
                    "availableSlots", workload.getMaxCapacity() - workload.getCurrentLeadCount(),
                    "utilizationPercentage", workload.getUtilizationPercentage(),
                    "status", workload.getStatus(),
                    "canAcceptNewLeads", "AVAILABLE".equals(workload.getStatus())
            );

            return ResponseEntity.ok(ApiResponse.success("Counselor capacity retrieved successfully", capacity));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving counselor capacity: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor capacity", "An unexpected error occurred"));
        }
    }

    /**
     * Get institution counselor summary
     */
    @GetMapping("/summary/{institutionId}")
    public ResponseEntity<?> getInstitutionCounselorSummary(@PathVariable String institutionId) {
        try {
            List<CounselorWorkload> workloads = leadService.getCounselorWorkloads(institutionId);

            long totalCounselors = workloads.size();
            long availableCounselors = workloads.stream()
                    .filter(w -> "AVAILABLE".equals(w.getStatus()))
                    .count();
            long busyCounselors = workloads.stream()
                    .filter(w -> "BUSY".equals(w.getStatus()))
                    .count();

            double averageUtilization = workloads.stream()
                    .mapToDouble(CounselorWorkload::getUtilizationPercentage)
                    .average()
                    .orElse(0.0);

            int totalCapacity = workloads.stream()
                    .mapToInt(CounselorWorkload::getMaxCapacity)
                    .sum();

            int currentLoad = workloads.stream()
                    .mapToInt(CounselorWorkload::getCurrentLeadCount)
                    .sum();

            Map<String, Object> summary = Map.of(
                    "totalCounselors", totalCounselors,
                    "availableCounselors", availableCounselors,
                    "busyCounselors", busyCounselors,
                    "averageUtilization", Math.round(averageUtilization * 100.0) / 100.0,
                    "totalCapacity", totalCapacity,
                    "currentLoad", currentLoad,
                    "availableCapacity", totalCapacity - currentLoad,
                    "institutionUtilization", totalCapacity > 0 ? Math.round(((double) currentLoad / totalCapacity) * 10000.0) / 100.0 : 0.0
            );

            return ResponseEntity.ok(ApiResponse.success("Institution counselor summary retrieved successfully", summary));
        } catch (RuntimeException e) {
            log.warn("Institution not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Institution not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving institution counselor summary: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor summary", "An unexpected error occurred"));
        }
    }

    /**
     * Request next lead for counselor (pull from queue)
     */
    @PostMapping("/{counselorId}/next-lead")
    public ResponseEntity<?> requestNextLead(@PathVariable String counselorId,
                                             @RequestParam String institutionId,
                                             Authentication authentication) {
        try {
            var nextLead = leadService.getNextLeadForCounselor(counselorId, institutionId, authentication.getName());

            if (nextLead == null) {
                return ResponseEntity.ok(ApiResponse.success("No leads available in queue", null));
            }

            return ResponseEntity.ok(ApiResponse.success("Next lead assigned successfully",
                    LeadResponse.fromEntity(nextLead)));
        } catch (com.admission_crm.lead_management.Exception.CounselorUnavailableException e) {
            log.warn("Counselor unavailable for next lead: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Counselor unavailable", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid counselor for institution: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error requesting next lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get next lead", "An unexpected error occurred"));
        }
    }
}
