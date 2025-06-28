package com.admission_crm.lead_management.Controller;

import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import com.admission_crm.lead_management.Exception.*;
import com.admission_crm.lead_management.Payload.*;
import com.admission_crm.lead_management.Payload.Request.LeadUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.ApiResponse;
import com.admission_crm.lead_management.Payload.Request.BulkAssignRequest;
import com.admission_crm.lead_management.Payload.Request.LeadRequest;
import com.admission_crm.lead_management.Payload.Response.LeadResponse;
import com.admission_crm.lead_management.Service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadService leadService;

    // Create a new lead
    @PostMapping
    public ResponseEntity<?> createLead(@RequestBody LeadRequest leadRequest) {
        try {
            Lead createdLead = leadService.createLead(leadRequest, leadRequest.getFirstName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Lead created successfully", LeadResponse.fromEntity(createdLead)));
        } catch (DuplicateLeadException e) {
            log.warn("Duplicate lead creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Lead already exists", e.getMessage()));
        } catch (InvalidLeadDataException e) {
            log.warn("Invalid lead data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid lead data", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create lead", "An unexpected error occurred"));
        }
    }

    // Get a lead by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLeadById(@PathVariable String id) {
        try {
            Lead lead = leadService.getLeadById(id);
            return ResponseEntity.ok(ApiResponse.success("Lead retrieved successfully", LeadResponse.fromEntity(lead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve lead", "An unexpected error occurred"));
        }
    }

    // Get all leads with pagination and filtering
    @GetMapping
    public ResponseEntity<?> getAllLeads(
            Pageable pageable,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String institutionId,
            @RequestParam(required = false) LeadStatus status) {
        try {
            Page<Lead> leads;
            if (searchTerm != null || institutionId != null || status != null) {
                leads = leadService.getLeadsByFilter(searchTerm, institutionId, status, pageable);
            } else {
                leads = leadService.getAllLeads(pageable);
            }

            Page<LeadResponse> leadResponses = leads.map(LeadResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.success("Leads retrieved successfully", leadResponses));
        } catch (Exception e) {
            log.error("Error retrieving leads: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve leads", "An unexpected error occurred"));
        }
    }

    // Get leads by institution
    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<?> getLeadsByInstitution(@PathVariable String institutionId,
                                                   Pageable pageable) {
        try {
            Page<Lead> leads = leadService.getLeadsByInstitution(institutionId, pageable);
            Page<LeadResponse> leadResponses = leads.map(LeadResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.success("Institution leads retrieved successfully", leadResponses));
        } catch (Exception e) {
            log.error("Error retrieving institution leads: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve institution leads", "An unexpected error occurred"));
        }
    }

    // Get leads by counselor
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<?> getLeadsByCounselor(@PathVariable String counselorId,
                                                 Pageable pageable) {
        try {
            Page<Lead> leads = leadService.getLeadsByCounselor(counselorId, pageable);
            Page<LeadResponse> leadResponses = leads.map(LeadResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.success("Counselor leads retrieved successfully", leadResponses));
        } catch (Exception e) {
            log.error("Error retrieving counselor leads: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve counselor leads", "An unexpected error occurred"));
        }
    }

    // Update lead
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable String id,
                                        @Valid @RequestBody LeadUpdateRequest updateRequest,
                                        Authentication authentication) {
        try {
            Lead updatedLead = leadService.updateLead(id, updateRequest, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead updated successfully", LeadResponse.fromEntity(updatedLead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (InvalidLeadDataException e) {
            log.warn("Invalid lead update data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid lead data", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update lead", "An unexpected error occurred"));
        }
    }

    // Delete lead
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable String id,
                                        Authentication authentication) {
        try {
            leadService.deleteLead(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead deleted successfully", null));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete lead", "An unexpected error occurred"));
        }
    }

    // Assign lead to counselor
    @PostMapping("/{leadId}/assign/{counselorId}")
    public ResponseEntity<?> assignLeadToCounselor(@PathVariable String leadId,
                                                   @PathVariable String counselorId,
                                                   Authentication authentication) {
        try {
            Lead assignedLead = leadService.assignLeadToCounselor(leadId, counselorId, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead assigned successfully", LeadResponse.fromEntity(assignedLead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (CounselorNotFoundException e) {
            log.warn("Counselor not found for assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Counselor not found", e.getMessage()));
        } catch (CounselorUnavailableException e) {
            log.warn("Counselor unavailable for assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Counselor unavailable", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid assignment request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid assignment", e.getMessage()));
        } catch (Exception e) {
            log.error("Error assigning lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to assign lead", "An unexpected error occurred"));
        }
    }

    // Get next lead from queue for counselor
    @PostMapping("/queue/next")
    public ResponseEntity<?> getNextLeadForCounselor(@RequestParam String counselorId,
                                                     @RequestParam String institutionId,
                                                     Authentication authentication) {
        try {
            Lead nextLead = leadService.getNextLeadForCounselor(counselorId, institutionId, authentication.getName());
            if (nextLead == null) {
                return ResponseEntity.ok(ApiResponse.success("No leads available in queue", null));
            }
            return ResponseEntity.ok(ApiResponse.success("Next lead assigned", LeadResponse.fromEntity(nextLead)));
        } catch (CounselorUnavailableException e) {
            log.warn("Counselor unavailable for next lead: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Counselor unavailable", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid counselor for institution: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting next lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get next lead", "An unexpected error occurred"));
        }
    }

    // Complete lead
    @PostMapping("/{leadId}/complete")
    public ResponseEntity<?> completeLead(@PathVariable String leadId,
                                          @RequestParam LeadStatus finalStatus,
                                          @RequestParam(required = false) String notes,
                                          Authentication authentication) {
        try {
            Lead completedLead = leadService.completeLead(leadId, finalStatus, notes, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead completed successfully", LeadResponse.fromEntity(completedLead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for completion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Invalid lead state for completion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid lead state", e.getMessage()));
        } catch (Exception e) {
            log.error("Error completing lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to complete lead", "An unexpected error occurred"));
        }
    }

    // Update lead priority
    @PutMapping("/{leadId}/priority")
    public ResponseEntity<?> updateLeadPriority(@PathVariable String leadId,
                                                @RequestParam Lead.LeadPriority priority,
                                                Authentication authentication) {
        try {
            Lead updatedLead = leadService.updateLeadPriority(leadId, priority, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead priority updated successfully", LeadResponse.fromEntity(updatedLead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for priority update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating lead priority: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update lead priority", "An unexpected error occurred"));
        }
    }

    /**
     * Schedule follow-up for lead
     */
    @PostMapping("/{leadId}/follow-up")
    public ResponseEntity<?> scheduleFollowUp(@PathVariable String leadId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime followUpDate,
                                              @RequestParam(required = false) String notes,
                                              Authentication authentication) {
        try {
            Lead updatedLead = leadService.scheduleFollowUp(leadId, followUpDate, notes, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Follow-up scheduled successfully", LeadResponse.fromEntity(updatedLead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for follow-up scheduling: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error scheduling follow-up: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to schedule follow-up", "An unexpected error occurred"));
        }
    }

    /**
     * Bulk assign leads
     */
    @PostMapping("/bulk-assign")
    public ResponseEntity<?> bulkAssignLeads(@RequestBody BulkAssignRequest request,
                                             Authentication authentication) {
        try {
            List<Lead> assignedLeads = leadService.bulkAssignLeads(
                    request.getLeadIds(),
                    request.getCounselorId(),
                    authentication.getName()
            );
            List<LeadResponse> leadResponses = assignedLeads.stream()
                    .map(LeadResponse::fromEntity)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success("Leads assigned successfully", leadResponses));
        } catch (CounselorNotFoundException e) {
            log.warn("Counselor not found for bulk assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Counselor not found", e.getMessage()));
        } catch (Exception e) {
            log.error("Error in bulk assignment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to assign leads", "An unexpected error occurred"));
        }
    }

    // Transfer lead from one counselor to another
    @PostMapping("/{leadId}/transfer")
    public ResponseEntity<?> transferLead(@PathVariable String leadId,
                                          @RequestParam String fromCounselorId,
                                          @RequestParam String toCounselorId,
                                          @RequestParam(required = false) String reason,
                                          Authentication authentication) {
        try {
            Lead transferredLead = leadService.transferLead(leadId, fromCounselorId, toCounselorId, reason, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Lead transferred successfully", LeadResponse.fromEntity(transferredLead)));
        } catch (LeadNotFoundException e) {
            log.warn("Lead not found for transfer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Lead not found", e.getMessage()));
        } catch (CounselorNotFoundException e) {
            log.warn("Counselor not found for transfer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Counselor not found", e.getMessage()));
        } catch (CounselorUnavailableException e) {
            log.warn("Target counselor unavailable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Counselor unavailable", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transfer request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid transfer", e.getMessage()));
        } catch (Exception e) {
            log.error("Error transferring lead: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to transfer lead", "An unexpected error occurred"));
        }
    }

    // Get lead statistics for institution
    @GetMapping("/statistics/{institutionId}")
    public ResponseEntity<?> getLeadStatistics(@PathVariable String institutionId) {
        try {
            LeadStatistics statistics = leadService.getLeadStatistics(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
        } catch (Exception e) {
            log.error("Error retrieving lead statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve statistics", "An unexpected error occurred"));
        }
    }

    // Get lead conversion funnel
    @GetMapping("/funnel/{institutionId}")
    public ResponseEntity<?> getLeadConversionFunnel(@PathVariable String institutionId) {
        try {
            Map<String, Long> funnel = leadService.getLeadConversionFunnel(institutionId);
            return ResponseEntity.ok(ApiResponse.success("Conversion funnel retrieved successfully", funnel));
        } catch (Exception e) {
            log.error("Error retrieving conversion funnel: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve conversion funnel", "An unexpected error occurred"));
        }
    }

    // Export leads data
    @GetMapping("/export/{institutionId}")
    public ResponseEntity<?> exportLeadsData(@PathVariable String institutionId,
                                             @RequestParam(required = false) LeadStatus status,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        try {
            List<LeadResponse> leadsData = leadService.exportLeadsData(institutionId, status, fromDate, toDate);
            return ResponseEntity.ok(ApiResponse.success("Leads data exported successfully", leadsData));
        } catch (Exception e) {
            log.error("Error exporting leads data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to export leads data", "An unexpected error occurred"));
        }
    }

    // Get daily lead creation statistics
    @GetMapping("/daily-stats/{institutionId}")
    public ResponseEntity<?> getDailyLeadCreationStats(@PathVariable String institutionId,
                                                       @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Long> dailyStats = leadService.getDailyLeadCreationStats(institutionId, days)
                    .entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            Map.Entry::getValue
                    ));
            return ResponseEntity.ok(ApiResponse.success("Daily statistics retrieved successfully", dailyStats));
        } catch (Exception e) {
            log.error("Error retrieving daily statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve daily statistics", "An unexpected error occurred"));
        }
    }
}