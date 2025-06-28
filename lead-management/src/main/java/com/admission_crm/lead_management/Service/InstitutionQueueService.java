package com.admission_crm.lead_management.Service;

import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import com.admission_crm.lead_management.Payload.LeadQueueInfo;
import com.admission_crm.lead_management.Repository.InstitutionRepository;
import com.admission_crm.lead_management.Repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionQueueService {

    private InstitutionRepository institutionRepository;

    private LeadRepository leadRepository;

    private LeadScoringService scoringService;

    /**
     * Add lead to institution's queue
     */
    public void addToQueue(Lead lead) {
        Institution institution = institutionRepository.findById(lead.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        Double score = scoringService.calculateLeadScore(lead);
        lead.setLeadScore(score);
        lead.setStatus(LeadStatus.QUEUED);

        institution.getQueuedLeads().addLast(lead.getId());

        if (!institution.getLeads().contains(lead.getId())) {
            institution.getLeads().add(lead.getId());
        }

        lead.setQueuePosition(institution.getQueuedLeads().size());

        leadRepository.save(lead);
        institutionRepository.save(institution);

        reorderQueue(institution);
    }

    /**
     * Get next lead from institution's queue
     */
    public Lead getNextLeadFromQueue(String institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        if (institution.getQueuedLeads().isEmpty()) {
            return null;
        }

        String nextLeadId = institution.getQueuedLeads().pollFirst();

        Lead lead = leadRepository.findById(nextLeadId)
                .orElseThrow(() -> new RuntimeException("Lead not found in queue"));

        updateQueuePositions(institution);

        lead.setQueuePosition(null);

        leadRepository.save(lead);
        institutionRepository.save(institution);

        return lead;
    }

    // Remove lead from a queue
    public void removeFromQueue(String leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        Institution institution = institutionRepository.findById(lead.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        // Remove from queue
        institution.getQueuedLeads().remove(leadId);

        // Update lead status
        if (lead.getStatus() == LeadStatus.QUEUED) {
            lead.setStatus(LeadStatus.NEW);
            lead.setQueuePosition(null);
        }

        // Update queue positions
        updateQueuePositions(institution);

        // Save changes
        leadRepository.save(lead);
        institutionRepository.save(institution);
    }

    /**
     * Reorder a queue based on priority and lead score
     */
    private void reorderQueue(Institution institution) {
        List<String> leadIds = new ArrayList<>(institution.getQueuedLeads());

        List<Lead> leads = leadRepository.findAllById(leadIds);

        // Sort by creation time
        leads.sort(Comparator.comparing(Lead::getCreatedAt));

        // Rebuild the queue
        institution.getQueuedLeads().clear();
        for (Lead lead : leads) {
            institution.getQueuedLeads().addLast(lead.getId());
        }

        // Update queue positions
        updateQueuePositions(institution);

        // Save leads with updated positions
        leadRepository.saveAll(leads);
        institutionRepository.save(institution);
    }

    // Update queue positions for all leads in queue
    private void updateQueuePositions(Institution institution) {
        List<String> leadIds = new ArrayList<>(institution.getQueuedLeads());

        for (int i = 0; i < leadIds.size(); i++) {
            String leadId = leadIds.get(i);
            Lead lead = leadRepository.findById(leadId).orElse(null);
            if (lead != null) {
                lead.setQueuePosition(i + 1);
                leadRepository.save(lead);
            }
        }
    }

    // Get queue status for an institution
    public List<LeadQueueInfo> getQueueStatus(String institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        List<LeadQueueInfo> queueInfo = new ArrayList<>();
        List<String> leadIds = new ArrayList<>(institution.getQueuedLeads());

        for (int i = 0; i < leadIds.size(); i++) {
            String leadId = leadIds.get(i);
            Lead lead = leadRepository.findById(leadId).orElse(null);

            if (lead != null) {
                queueInfo.add(new LeadQueueInfo(
                        i + 1,
                        lead.getFirstName() + " " + lead.getLastName(),
                        lead.getEmail(),
                        lead.getLeadScore(),
                        lead.getPriority().name()
                ));
            }
        }

        return queueInfo;
    }

    // Get queue size for an institution
    public int getQueueSize(String institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        return institution.getQueuedLeads().size();
    }
}
