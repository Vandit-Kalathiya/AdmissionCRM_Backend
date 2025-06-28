package com.admission_crm.lead_management.Service;

import com.admission_crm.lead_management.Entity.AnalyticsAndReporting.AuditLog;
import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import com.admission_crm.lead_management.Entity.CoreEntities.User;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import com.admission_crm.lead_management.Exception.*;
import com.admission_crm.lead_management.Payload.*;
import com.admission_crm.lead_management.Payload.Request.LeadRequest;
import com.admission_crm.lead_management.Payload.Request.LeadUpdateRequest;
import com.admission_crm.lead_management.Payload.Response.LeadResponse;
import com.admission_crm.lead_management.Repository.AuditLogRepository;
import com.admission_crm.lead_management.Repository.InstitutionRepository;
import com.admission_crm.lead_management.Repository.LeadRepository;
import com.admission_crm.lead_management.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final AuditLogRepository auditLogRepository;
    private final InstitutionQueueService queueService;
    private final LeadScoringService scoringService;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public Lead createLead(LeadRequest leadRequest, String userEmail) {
        System.out.println(leadRequest);

        validateLeadRequest(leadRequest);

//        Optional<Lead> existingLead = leadRepository.findByEmail(leadRequest.getEmail());
//        if (existingLead.isPresent()) {
//            throw new DuplicateLeadException("Lead with email " + leadRequest.getEmail() + " already exists");
//        }

        Institution institution = institutionRepository.findById(leadRequest.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        Lead lead = mapRequestToLead(leadRequest);
        lead.setStatus(LeadStatus.NEW);

        Double score = scoringService.calculateLeadScore(lead);
        lead.setLeadScore(score);

        Lead savedLead = leadRepository.save(lead);

        logAudit(userEmail, "CREATED_LEAD", savedLead.getId(), "Lead",
                "Created lead: " + lead.getEmail());

        // Add to institution's queue automatically
        queueService.addToQueue(savedLead);

        // Notify counselors
        notifyCounselors("New lead created and queued: " + lead.getFirstName() + " " + lead.getLastName() +
                " for " + institution.getName());

        // Try auto-assignment if counselors are available
//        tryAutoAssignment(savedLead.getInstitutionId());

        return savedLead;
    }

    // Get a lead by ID
    public Lead getLeadById(String leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException("Lead not found with id: " + leadId));
    }

    // Get All leads with pagination
    public Page<Lead> getAllLeads(Pageable pageable) {
        return leadRepository.findAll(pageable);
    }

    // Get Leads by filter
    public Page<Lead> getLeadsByFilter(String searchTerm, String institutionId,
                                       LeadStatus status, Pageable pageable) {
        return leadRepository.searchLeads(searchTerm, institutionId, status, pageable);
    }

    // Get Leads by institution
    public Page<Lead> getLeadsByInstitution(String institutionId, Pageable pageable) {
        return leadRepository.findByInstitutionId(institutionId, pageable);
    }

    // Get Leads by counselor
    public Page<Lead> getLeadsByCounselor(String counselorId, Pageable pageable) {
        return leadRepository.findByAssignedCounselor(counselorId, pageable);
    }

    // Update lead
    public Lead updateLead(String leadId, LeadUpdateRequest updateRequest, String userEmail) {
        Lead existingLead = getLeadById(leadId);

        LeadStatus oldStatus = existingLead.getStatus();

        // Update fields
        updateLeadFields(existingLead, updateRequest);

        Lead updatedLead = leadRepository.save(existingLead);

        // Handle status changes
        handleStatusChange(updatedLead, oldStatus, userEmail);

        logAudit(userEmail, "UPDATED_LEAD", leadId, "Lead",
                "Updated lead: " + existingLead.getEmail());

        return updatedLead;
    }

    // Delete lead
    public void deleteLead(String leadId, String userEmail) {
        Lead lead = getLeadById(leadId);

        // Remove from queue if queued
        if (lead.getStatus() == LeadStatus.QUEUED) {
            queueService.removeFromQueue(leadId);
        }

        // Remove from an institution's lead list
        Institution institution = institutionRepository.findById(lead.getInstitutionId()).orElse(null);
        if (institution != null) {
            institution.getLeads().remove(leadId);
            institutionRepository.save(institution);
        }

        // Update counselor availability if assigned
        if (lead.getAssignedCounselor() != null) {
            freeCounselorSlot(lead.getAssignedCounselor());
        }

        leadRepository.delete(lead);

        logAudit(userEmail, "DELETED_LEAD", leadId, "Lead",
                "Deleted lead: " + lead.getEmail());
    }

    // ========== Lead Assignment Operations ==========

    // Manual lead assignment to a specific counselor
    public Lead assignLeadToCounselor(String leadId, String counselorId, String userEmail) {
        Lead lead = getLeadById(leadId);
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new CounselorNotFoundException("Counselor not found"));

        // Verify a counselor belongs to the institution
        Institution institution = institutionRepository.findById(lead.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        if (!institution.getCounselors().contains(counselorId)) {
            throw new IllegalArgumentException("Counselor does not belong to this institution");
        }

        // Check counselor capacity
        if (!isCounselorAvailable(counselorId, lead.getInstitutionId())) {
            throw new CounselorUnavailableException("Counselor has reached maximum capacity");
        }

        // Remove from queue if queued
        if (lead.getStatus() == LeadStatus.QUEUED) {
            queueService.removeFromQueue(leadId);
        }

        // Assign lead
        lead.setAssignedCounselor(counselorId);
        lead.setStatus(LeadStatus.ASSIGNED);
        lead.setAssignedAt(LocalDateTime.now());

        Lead assignedLead = leadRepository.save(lead);

        logAudit(userEmail, "ASSIGNED_LEAD", leadId, "Lead",
                "Manually assigned to counselor: " + counselor.getEmail());

        notifyCounselors("Lead assigned to " + counselor.getFirstName() + ": " +
                lead.getFirstName() + " " + lead.getLastName());

        return assignedLead;
    }

    // Get next lead from queue for available counselor
    public Lead getNextLeadForCounselor(String counselorId, String institutionId, String userEmail) {
        // Verify a counselor belongs to an institution
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        if (!institution.getCounselors().contains(counselorId)) {
            throw new IllegalArgumentException("Counselor does not belong to this institution");
        }

        // Check if counselor is available
        if (!isCounselorAvailable(counselorId, institutionId)) {
            throw new CounselorUnavailableException("Counselor is not available for new leads");
        }

        // Get next lead from queue
        Lead nextLead = queueService.getNextLeadFromQueue(institutionId);

        if (nextLead == null) {
            return null; // No leads in queue
        }

        // Assign to counselor
        nextLead.setAssignedCounselor(counselorId);
        nextLead.setStatus(LeadStatus.ASSIGNED);
        nextLead.setAssignedAt(LocalDateTime.now());

        Lead assignedLead = leadRepository.save(nextLead);

        User counselor = userRepository.findById(counselorId).orElse(null);
        String counselorName = counselor != null ? counselor.getFirstName() : "Unknown";

        logAudit(userEmail, "AUTO_ASSIGNED_LEAD", assignedLead.getId(), "Lead",
                "Auto-assigned from queue to counselor: " + counselorName);

        notifyCounselors("Lead auto-assigned to " + counselorName + ": " +
                nextLead.getFirstName() + " " + nextLead.getLastName());

        return assignedLead;
    }

    // Mark lead as completed and free up counselor
    public Lead completeLead(String leadId, LeadStatus finalStatus, String notes, String userEmail) {
        Lead lead = getLeadById(leadId);

        if (lead.getAssignedCounselor() == null) {
            throw new IllegalStateException("Cannot complete unassigned lead");
        }

        // Update lead status
        lead.setStatus(finalStatus);
        lead.setCompletedAt(LocalDateTime.now());

        Lead completedLead = leadRepository.save(lead);

        // Free up counselor slot
        freeCounselorSlot(lead.getAssignedCounselor());

        logAudit(userEmail, "COMPLETED_LEAD", leadId, "Lead",
                "Lead completed with status: " + finalStatus);

        return completedLead;
    }

    // ========== Queue Management Operations ==========

    // Get queue status for institution
    public List<LeadQueueInfo> getQueueStatus(String institutionId) {
        return queueService.getQueueStatus(institutionId);
    }

    // Get queue size for an institution
    public int getQueueSize(String institutionId) {
        return queueService.getQueueSize(institutionId);
    }

    // Move lead to different position in queue
    public void moveLeadInQueue(String leadId, int newPosition, String userEmail) {
        Lead lead = getLeadById(leadId);

        if (lead.getStatus() != LeadStatus.QUEUED) {
            throw new IllegalStateException("Lead is not in queue");
        }

        Institution institution = institutionRepository.findById(lead.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        List<String> queue = new ArrayList<>(institution.getQueuedLeads());

        // Remove lead from the current position
        queue.remove(leadId);

        // Insert at a new position (adjust for 0-based indexing)
        int insertIndex = Math.min(Math.max(0, newPosition - 1), queue.size());
        queue.add(insertIndex, leadId);

        // Update institution's queue
        institution.getQueuedLeads().clear();
        institution.getQueuedLeads().addAll(queue);

        // Update queue positions
        for (int i = 0; i < queue.size(); i++) {
            String queueLeadId = queue.get(i);
            Lead queueLead = leadRepository.findById(queueLeadId).orElse(null);
            if (queueLead != null) {
                queueLead.setQueuePosition(i + 1);
                leadRepository.save(queueLead);
            }
        }

        institutionRepository.save(institution);

        logAudit(userEmail, "MOVED_LEAD_IN_QUEUE", leadId, "Lead",
                "Moved lead to position: " + newPosition);
    }


    // Validate lead request data
    private void validateLeadRequest(LeadRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new InvalidLeadDataException("First name is required");
        }
        if (request.getEmail() == null || !isValidEmail(request.getEmail())) {
            throw new InvalidLeadDataException("Valid email is required");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new InvalidLeadDataException("Phone number is required");
        }
        if (request.getInstitutionId() == null || request.getInstitutionId().trim().isEmpty()) {
            throw new InvalidLeadDataException("Institution ID is required");
        }
        if (request.getCourseInterested() == null || request.getCourseInterested().trim().isEmpty()) {
            throw new InvalidLeadDataException("Course interest is required");
        }
    }

    // Map LeadRequest to Lead entity
    private Lead mapRequestToLead(LeadRequest request) {
        Lead lead = new Lead();
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setAlternatePhone(request.getAlternatePhone());
        lead.setCity(request.getCity());
        lead.setState(request.getState());
        lead.setCountry(request.getCountry());
        lead.setPinCode(request.getPinCode());
        lead.setAddress(request.getAddress());
        lead.setQualification(request.getQualification());
        lead.setBudgetRange(request.getBudgetRange());
        lead.setInstitutionId(request.getInstitutionId());
        lead.setCourseInterestId(request.getCourseInterested());

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().trim().isEmpty()) {
            try {
                lead.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            } catch (Exception e) {
                throw new InvalidLeadDataException("Invalid date of birth format. Use YYYY-MM-DD");
            }
        }

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                lead.setGender(Lead.Gender.valueOf(request.getGender().toUpperCase()));
            } catch (Exception e) {
                throw new InvalidLeadDataException("Invalid gender. Use MALE, FEMALE, or OTHER");
            }
        }

        if (request.getSource() != null && !request.getSource().trim().isEmpty()) {
            try {
                lead.setLeadSource(Lead.LeadSource.valueOf(request.getSource().toUpperCase()));
            } catch (Exception e) {
                throw new InvalidLeadDataException("Invalid source");
            }
        }

        return lead;
    }

    // Update lead fields from update request
    private void updateLeadFields(Lead lead, LeadUpdateRequest request) {
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            lead.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            lead.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            lead.setPhone(request.getPhone().trim());
        }
        if (request.getAlternatePhone() != null) {
            lead.setAlternatePhone(request.getAlternatePhone().trim());
        }
        if (request.getCity() != null) {
            lead.setCity(request.getCity().trim());
        }
        if (request.getState() != null) {
            lead.setState(request.getState().trim());
        }
        if (request.getAddress() != null) {
            lead.setAddress(request.getAddress().trim());
        }
        if (request.getQualification() != null) {
            lead.setQualification(request.getQualification().trim());
        }
        if (request.getBudgetRange() != null) {
            lead.setBudgetRange(request.getBudgetRange().trim());
        }
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            lead.setPriority(request.getPriority());
        }
        if (request.getCourseInterestId() != null) {
            lead.setCourseInterestId(request.getCourseInterestId());
        }
    }

    // Handle status changes and trigger appropriate actions
    private void handleStatusChange(Lead lead, LeadStatus oldStatus, String userEmail) {
        if (oldStatus == lead.getStatus()) {
            return;
        }

        switch (lead.getStatus()) {
            case QUEUED:
                queueService.addToQueue(lead);
                notifyCounselors("Lead re-queued: " + lead.getFirstName() + " " + lead.getLastName());
                break;
            case ASSIGNED:
                if (oldStatus == LeadStatus.QUEUED) {
                    queueService.removeFromQueue(lead.getId());
                }
                break;
//            case CONVERTED:
            case REJECTED:
//            case LOST:
                if (lead.getAssignedCounselor() != null) {
                    freeCounselorSlot(lead.getAssignedCounselor());
                    // Try to assign a next lead to this counselor
                    tryAutoAssignmentForCounselor(lead.getAssignedCounselor(), lead.getInstitutionId(), userEmail);
                }
                lead.setCompletedAt(LocalDateTime.now());
                break;
        }
    }

    // Check if a counselor is available for new leads
    private boolean isCounselorAvailable(String counselorId, String institutionId) {
        // Check if a counselor exists and is active
        User counselor = userRepository.findById(counselorId).orElse(null);
        if (counselor == null || !counselor.getIsActive()) {
            return false;
        }

        // Check counselor's current workload
        List<LeadStatus> activeStatuses = Arrays.asList(
                LeadStatus.ASSIGNED,
                LeadStatus.IN_PROGRESS,
                LeadStatus.CONTACTED,
                LeadStatus.FOLLOW_UP
        );

        Long currentLeadCount = leadRepository.countActiveLeadsByCounselor(counselorId, activeStatuses);

        // Assume a max capacity of 10 leads per counselor (this can be configurable)
        int maxCapacity = 10;

        return currentLeadCount < maxCapacity;
    }

    // Free up counselor slot when a lead is completed
    private void freeCounselorSlot(String counselorId) {
        User counselor = userRepository.findById(counselorId).orElse(null);
        if (counselor != null) {
            logAudit("SYSTEM", "COUNSELOR_FREED", counselorId, "Counselor",
                    "Counselor freed up for new leads: " + counselor.getEmail());
        }
    }

    /**
     * Try to automatically assign leads to available counselors
     */
    private void tryAutoAssignment(String institutionId) {
        Institution institution = institutionRepository.findById(institutionId).orElse(null);
        if (institution == null || institution.getCounselors().isEmpty()) {
            return;
        }

        // Find available counselors
        List<String> availableCounselors = institution.getCounselors().stream()
                .filter(counselorId -> isCounselorAvailable(counselorId, institutionId))
                .toList();

        if (availableCounselors.isEmpty()) {
            return;
        }

        // Try to assign leads from queue
        for (String counselorId : availableCounselors) {
            Lead nextLead = queueService.getNextLeadFromQueue(institutionId);
            if (nextLead != null) {
                nextLead.setAssignedCounselor(counselorId);
                nextLead.setStatus(LeadStatus.ASSIGNED);
                nextLead.setAssignedAt(LocalDateTime.now());
                leadRepository.save(nextLead);

                User counselor = userRepository.findById(counselorId).orElse(null);
                String counselorName = counselor != null ? counselor.getFirstName() : "Unknown";

                notifyCounselors("Lead auto-assigned to " + counselorName + ": " +
                        nextLead.getFirstName() + " " + nextLead.getLastName());

                logAudit("SYSTEM", "AUTO_ASSIGNED_LEAD", nextLead.getId(), "Lead",
                        "Auto-assigned to available counselor: " + counselorName);
            }
        }
    }

    /**
     * Try to assign next lead from queue to specific counselor
     */
    private void tryAutoAssignmentForCounselor(String counselorId, String institutionId, String userEmail) {
        if (!isCounselorAvailable(counselorId, institutionId)) {
            return;
        }

        Lead nextLead = queueService.getNextLeadFromQueue(institutionId);
        if (nextLead != null) {
            nextLead.setAssignedCounselor(counselorId);
            nextLead.setStatus(LeadStatus.ASSIGNED);
            nextLead.setAssignedAt(LocalDateTime.now());
            leadRepository.save(nextLead);

            User counselor = userRepository.findById(counselorId).orElse(null);
            String counselorName = counselor != null ? counselor.getFirstName() : "Unknown";

            notifyCounselors("Next lead auto-assigned to " + counselorName + ": " +
                    nextLead.getFirstName() + " " + nextLead.getLastName());

            logAudit(userEmail, "AUTO_ASSIGNED_NEXT_LEAD", nextLead.getId(), "Lead",
                    "Auto-assigned next lead to counselor: " + counselorName);
        }
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Log audit trail
    private void logAudit(String userEmail, String action, String entityId, String entityType, String details) {
        try {
            User user = userRepository.findByEmail(userEmail).orElse(null);

            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user != null ? user.getId() : "SYSTEM");
            auditLog.setAction(action);
            auditLog.setEntityId(entityId);
            auditLog.setEntityType(entityType);
            auditLog.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }

    // Register a WebSocket session for notifications
    public void registerSession(String userEmail, WebSocketSession session){
        sessions.put(userEmail, session);
    }

    // Remove WebSocket session
    public void removeSession(String userEmail) {
        sessions.remove(userEmail);
    }

    // Notify counselors via WebSocket
    private void notifyCounselors(String message) {
        List<String> disconnectedSessions = new ArrayList<>();

        sessions.forEach((userEmail, session) -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                } else {
                    disconnectedSessions.add(userEmail);
                }
            } catch (IOException e) {
                System.err.println("Failed to send notification to " + userEmail + ": " + e.getMessage());
                disconnectedSessions.add(userEmail);
            }
        });

        // Clean up disconnected sessions
        disconnectedSessions.forEach(sessions::remove);
    }

    // Get lead statistics for an institution
    public LeadStatistics getLeadStatistics(String institutionId) {
        Long totalLeads = leadRepository.countByInstitutionIdAndStatus(institutionId, null);
        Long newLeads = leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.NEW);
        Long queuedLeads = leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.QUEUED);
        Long assignedLeads = leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.ASSIGNED);
        Long convertedLeads = leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.COMPLETED);
        Long rejectedLeads = leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.REJECTED);

        double conversionRate = totalLeads > 0 ? (convertedLeads.doubleValue() / totalLeads.doubleValue()) * 100 : 0.0;

        return LeadStatistics.builder()
                .totalLeads(totalLeads)
                .newLeads(newLeads)
                .queuedLeads(queuedLeads)
                .assignedLeads(assignedLeads)
                .convertedLeads(convertedLeads)
                .rejectedLeads(rejectedLeads)
                .conversionRate(conversionRate)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get counselor workload for institution
     */
    public List<CounselorWorkload> getCounselorWorkloads(String institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        List<CounselorWorkload> workloads = new ArrayList<>();

        for (String counselorId : institution.getCounselors()) {
            User counselor = userRepository.findById(counselorId).orElse(null);
            if (counselor == null) continue;

            List<LeadStatus> activeStatuses = Arrays.asList(
                    LeadStatus.ASSIGNED,
                    LeadStatus.IN_PROGRESS,
                    LeadStatus.CONTACTED,
                    LeadStatus.FOLLOW_UP
            );

            Long currentLeadCount = leadRepository.countActiveLeadsByCounselor(counselorId, activeStatuses);
            int maxCapacity = 10; // This should be configurable
            double utilization = (currentLeadCount.doubleValue() / maxCapacity) * 100;

            String status = currentLeadCount >= maxCapacity ? "BUSY" : "AVAILABLE";

            CounselorWorkload workload = CounselorWorkload.builder()
                    .counselorId(counselorId)
                    .counselorName(counselor.getFirstName() + " " + counselor.getLastName())
                    .counselorEmail(counselor.getEmail())
                    .currentLeadCount(currentLeadCount.intValue())
                    .maxCapacity(maxCapacity)
                    .utilizationPercentage(utilization)
                    .status(status)
                    .build();

            workloads.add(workload);
        }
        return workloads;
    }

    /**
     * Get leads requiring follow-up
     */
//    public List<Lead> getLeadsRequiringFollowUp() {
//        return leadRepository.findByNextFollowUpDateBeforeAndStatus(
//                LocalDateTime.now(),
//                LeadStatus.FOLLOW_UP
//        );
//    }

    /**
     * Schedule follow-up for lead
     */
    public Lead scheduleFollowUp(String leadId, LocalDateTime followUpDate, String notes, String userEmail) {
        Lead lead = getLeadById(leadId);

//        lead.setNextFollowUpDate(followUpDate);
        lead.setStatus(LeadStatus.FOLLOW_UP);
//        if (notes != null && !notes.trim().isEmpty()) {
//            lead.setLastInteractionNotes(notes);
//        }

        Lead updatedLead = leadRepository.save(lead);

        logAudit(userEmail, "SCHEDULED_FOLLOW_UP", leadId, "Lead",
                "Scheduled follow-up for: " + followUpDate);

        return updatedLead;
    }

    /**
     * Bulk assign leads to counselor
     */
    public List<Lead> bulkAssignLeads(List<String> leadIds, String counselorId, String userEmail) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new CounselorNotFoundException("Counselor not found"));

        List<Lead> assignedLeads = new ArrayList<>();

        for (String leadId : leadIds) {
            try {
                Lead lead = getLeadById(leadId);

                // Verify counselor belongs to the institution
                Institution institution = institutionRepository.findById(lead.getInstitutionId()).orElse(null);
                if (institution == null || !institution.getCounselors().contains(counselorId)) {
                    continue; // Skip this lead
                }

                // Check counselor capacity
                if (!isCounselorAvailable(counselorId, lead.getInstitutionId())) {
                    break; // Stop assigning if counselor is at capacity
                }

                // Remove from queue if queued
                if (lead.getStatus() == LeadStatus.QUEUED) {
                    queueService.removeFromQueue(leadId);
                }

                // Assign lead
                lead.setAssignedCounselor(counselorId);
                lead.setStatus(LeadStatus.ASSIGNED);
                lead.setAssignedAt(LocalDateTime.now());

                Lead assignedLead = leadRepository.save(lead);
                assignedLeads.add(assignedLead);

            } catch (Exception e) {
                // Log error but continue with other leads
                System.err.println("Failed to assign lead " + leadId + ": " + e.getMessage());
            }
        }

        if (!assignedLeads.isEmpty()) {
            logAudit(userEmail, "BULK_ASSIGNED_LEADS", counselorId, "Counselor",
                    "Bulk assigned " + assignedLeads.size() + " leads to counselor: " + counselor.getEmail());

            notifyCounselors("Bulk assigned " + assignedLeads.size() + " leads to " + counselor.getFirstName());
        }

        return assignedLeads;
    }

    /**
     * Transfer lead from one counselor to another
     */
    public Lead transferLead(String leadId, String fromCounselorId, String toCounselorId, String reason, String userEmail) {
        Lead lead = getLeadById(leadId);

        if (!lead.getAssignedCounselor().equals(fromCounselorId)) {
            throw new IllegalArgumentException("Lead is not assigned to the specified counselor");
        }

        User fromCounselor = userRepository.findById(fromCounselorId)
                .orElseThrow(() -> new CounselorNotFoundException("Source counselor not found"));

        User toCounselor = userRepository.findById(toCounselorId)
                .orElseThrow(() -> new CounselorNotFoundException("Target counselor not found"));

        // Check if target counselor is available
        if (!isCounselorAvailable(toCounselorId, lead.getInstitutionId())) {
            throw new CounselorUnavailableException("Target counselor is not available");
        }

        // Verify both counselors belong to the institution
        Institution institution = institutionRepository.findById(lead.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        if (!institution.getCounselors().contains(fromCounselorId) ||
                !institution.getCounselors().contains(toCounselorId)) {
            throw new IllegalArgumentException("One or both counselors do not belong to this institution");
        }

        // Transfer the lead
        lead.setAssignedCounselor(toCounselorId);
        lead.setAssignedAt(LocalDateTime.now());

//        if (reason != null && !reason.trim().isEmpty()) {
//            String currentNotes = lead.getLastInteractionNotes() != null ? lead.getLastInteractionNotes() : "";
//            lead.setLastInteractionNotes(currentNotes + "\n[TRANSFER] " + reason);
//        }

        Lead transferredLead = leadRepository.save(lead);

        logAudit(userEmail, "TRANSFERRED_LEAD", leadId, "Lead",
                "Transferred from " + fromCounselor.getEmail() + " to " + toCounselor.getEmail() +
                        (reason != null ? ". Reason: " + reason : ""));

        notifyCounselors("Lead transferred from " + fromCounselor.getFirstName() +
                " to " + toCounselor.getFirstName() + ": " + lead.getFirstName() + " " + lead.getLastName());

        // Try to assign next lead to the counselor who freed up
        tryAutoAssignmentForCounselor(fromCounselorId, lead.getInstitutionId(), userEmail);

        return transferredLead;
    }

    /**
     * Get lead conversion funnel data
     */
    public Map<String, Long> getLeadConversionFunnel(String institutionId) {
        Map<String, Long> funnel = new LinkedHashMap<>();

        funnel.put("NEW", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.NEW));
        funnel.put("QUEUED", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.QUEUED));
        funnel.put("ASSIGNED", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.ASSIGNED));
        funnel.put("IN_PROGRESS", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.IN_PROGRESS));
        funnel.put("CONTACTED", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.CONTACTED));
        funnel.put("QUALIFIED", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.FOLLOW_UP));
        funnel.put("CONVERTED", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.COMPLETED));
        funnel.put("REJECTED", leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.REJECTED));

        return funnel;
    }

    /**
     * Update lead priority and requeue if necessary
     */
    public Lead updateLeadPriority(String leadId, Lead.LeadPriority newPriority, String userEmail) {
        Lead lead = getLeadById(leadId);
        Lead.LeadPriority oldPriority = lead.getPriority();

        lead.setPriority(newPriority);

        // Recalculate lead score
        Double newScore = scoringService.calculateLeadScore(lead);
        lead.setLeadScore(newScore);

        Lead updatedLead = leadRepository.save(lead);

        // If lead is in queue, reorder the queue
        if (lead.getStatus() == LeadStatus.QUEUED) {
            Institution institution = institutionRepository.findById(lead.getInstitutionId()).orElse(null);
            if (institution != null) {
                // Remove and re-add to queue to trigger reordering
                queueService.removeFromQueue(leadId);
                queueService.addToQueue(updatedLead);
            }
        }

        logAudit(userEmail, "UPDATED_LEAD_PRIORITY", leadId, "Lead",
                "Updated priority from " + oldPriority + " to " + newPriority);

        return updatedLead;
    }

    /**
     * Get queue waiting time estimates
     */
    public Map<Integer, String> getQueueWaitingTimeEstimates(String institutionId) {
        List<LeadQueueInfo> queueInfo = queueService.getQueueStatus(institutionId);
        Map<Integer, String> estimates = new HashMap<>();

        // Get number of available counselors
        Institution institution = institutionRepository.findById(institutionId).orElse(null);
        if (institution == null) {
            return estimates;
        }

        long availableCounselors = institution.getCounselors().stream()
                .filter(counselorId -> isCounselorAvailable(counselorId, institutionId))
                .count();

        if (availableCounselors == 0) {
            for (int i = 1; i <= queueInfo.size(); i++) {
                estimates.put(i, "Waiting for counselor availability");
            }
            return estimates;
        }

        // Estimate 30 minutes per lead processing time
        int avgProcessingMinutes = 30;

        for (int i = 1; i <= queueInfo.size(); i++) {
            int estimatedMinutes = (int) ((i * avgProcessingMinutes) / availableCounselors);

            if (estimatedMinutes < 60) {
                estimates.put(i, estimatedMinutes + " minutes");
            } else {
                int hours = estimatedMinutes / 60;
                int minutes = estimatedMinutes % 60;
                estimates.put(i, hours + " hours" + (minutes > 0 ? " " + minutes + " minutes" : ""));
            }
        }

        return estimates;
    }

    /**
     * Export leads data for institution
     */
    public List<LeadResponse> exportLeadsData(String institutionId, LeadStatus status,
                                              LocalDateTime fromDate, LocalDateTime toDate) {
        List<Lead> leads;

        if (status != null) {
            leads = leadRepository.findByInstitutionIdAndStatusOrderByCreatedAtAsc(institutionId, status);
        } else {
            leads = leadRepository.findByInstitutionIdOrderByCreatedAtAsc(institutionId);
        }

        // Filter by date range if provided
        if (fromDate != null || toDate != null) {
            leads = leads.stream()
                    .filter(lead -> {
                        LocalDateTime createdAt = lead.getCreatedAt();
                        boolean afterFrom = fromDate == null || createdAt.isAfter(fromDate);
                        boolean beforeTo = toDate == null || createdAt.isBefore(toDate);
                        return afterFrom && beforeTo;
                    })
                    .toList();
        }

        // Convert to response DTOs
        return leads.stream()
                .map(LeadResponse::fromEntity)
                .toList();
    }

    /**
     * Get daily lead creation statistics
     */
    public Map<LocalDate, Long> getDailyLeadCreationStats(String institutionId, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);

        List<Lead> leads = leadRepository.findByInstitutionIdAndCreatedAtAfter(institutionId, fromDate);

        return leads.stream()
                .collect(Collectors.groupingBy(
                        lead -> lead.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));
    }

    /**
     * Clean up old completed leads (for maintenance)
     */
    @Transactional
    public int cleanupOldCompletedLeads(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

        List<LeadStatus> completedStatuses = Arrays.asList(
                LeadStatus.COMPLETED,
                LeadStatus.REJECTED
        );

        List<Lead> oldLeads = leadRepository.findByStatusInAndCompletedAtBefore(completedStatuses, cutoffDate);

        for (Lead lead : oldLeads) {
            // Remove from institution's leads list
            Institution institution = institutionRepository.findById(lead.getInstitutionId()).orElse(null);
            if (institution != null) {
                institution.getLeads().remove(lead.getId());
                institutionRepository.save(institution);
            }
        }

        leadRepository.deleteAll(oldLeads);

        return oldLeads.size();
    }

    /**
     * Health check for queue system
     */
    public Map<String, Object> getQueueHealthCheck(String institutionId) {
        Map<String, Object> health = new HashMap<>();

        try {
            Institution institution = institutionRepository.findById(institutionId).orElse(null);
            if (institution == null) {
                health.put("status", "ERROR");
                health.put("message", "Institution not found");
                return health;
            }

            int queueSize = institution.getQueuedLeads().size();
            int dbQueueSize = leadRepository.countByInstitutionIdAndStatus(institutionId, LeadStatus.QUEUED).intValue();

            boolean queueSyncStatus = queueSize == dbQueueSize;

            long availableCounselors = institution.getCounselors().stream()
                    .filter(counselorId -> isCounselorAvailable(counselorId, institutionId))
                    .count();

            health.put("status", queueSyncStatus ? "HEALTHY" : "WARNING");
            health.put("queueSize", queueSize);
            health.put("dbQueueSize", dbQueueSize);
            health.put("queueSyncStatus", queueSyncStatus);
            health.put("availableCounselors", availableCounselors);
            health.put("totalCounselors", institution.getCounselors().size());
            health.put("lastChecked", LocalDateTime.now());

            if (!queueSyncStatus) {
                health.put("message", "Queue size mismatch between cache and database");
            }

        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("message", "Health check failed: " + e.getMessage());
        }

        return health;
    }
}