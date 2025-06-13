package com.admission_crm.lead_management.Service;

import com.admission_crm.lead_management.Entity.AnalyticsAndReporting.AuditLog;
import com.admission_crm.lead_management.Entity.CoreEntities.Role;
import com.admission_crm.lead_management.Entity.CoreEntities.User;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import com.admission_crm.lead_management.Repository.AuditLogRepository;
import com.admission_crm.lead_management.Repository.LeadRepository;
import com.admission_crm.lead_management.Repository.UserRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public Lead createLead(Lead lead, String userEmail) {
        lead.setStatus(LeadStatus.NEW);
        Lead savedLead = leadRepository.save(lead);
        logAudit(userEmail, "Created Lead", savedLead.getId(), "Lead", "Created lead: " + lead.getEmail());
        notifyCounselors("New lead created: " + lead.getEmail());
        return savedLead;
    }

    public Lead updateLead(String leadId, Lead updatedLead, String userEmail) {
        Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new RuntimeException("Lead not found"));
        lead.setFirstName(updatedLead.getFirstName());
        lead.setLastName(updatedLead.getLastName());
        lead.setEmail(updatedLead.getEmail());
        lead.setPhone(updatedLead.getPhone());
        lead.setStatus(updatedLead.getStatus());
        Lead savedLead = leadRepository.save(lead);
        logAudit(userEmail, "Updated Lead", leadId, "Lead", "Updated lead status to: " + lead.getStatus());
        notifyCounselors("Lead updated: " + lead.getEmail());
        return savedLead;
    }

    public Lead assignLead(String leadId, String counselorId, String userEmail) {
        Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new RuntimeException("Lead not found"));
        User counselor = userRepository.findById(counselorId).orElseThrow(() -> new RuntimeException("Counselor not found"));
        lead.setAssignedCounselor(counselor);
        Lead savedLead = leadRepository.save(lead);
        logAudit(userEmail, "Assigned Lead", leadId, "Lead", "Assigned to counselor: " + counselor.getEmail());
        notifyCounselors("Lead assigned to: " + counselor.getEmail());
        return savedLead;
    }

    public Lead autoAssignLead(String leadId, String userEmail) {
        Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new RuntimeException("Lead not found"));
        User counselor = findLeastBusyCounselor();
        lead.setAssignedCounselor(counselor);
        Lead savedLead = leadRepository.save(lead);
        logAudit(userEmail, "Auto-assigned Lead", leadId, "Lead", "Auto-assigned to counselor: " + counselor.getEmail());
        notifyCounselors("Lead auto-assigned to: " + counselor.getEmail());
        return savedLead;
    }

    public Page<Lead> getLeads(Pageable pageable, String status, String source, String counselorId) {
        if (status != null) return leadRepository.findByStatus(LeadStatus.valueOf(status), pageable);
        if (source != null) return leadRepository.findBySource(source, pageable);
        if (counselorId != null) return leadRepository.findByAssignedCounselor_UserId(counselorId, pageable);
        return leadRepository.findAll(pageable);
    }

    public double calculateLeadScore(String leadId) {
        Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new RuntimeException("Lead not found"));
        SimpleRegression regression = new SimpleRegression();
        // Example data: response time (hours), engagement count
//        regression.addData(1.0, lead.getSource().equals("Website") ? 0.8 : 0.5);
        regression.addData(2.0, lead.getStatus() == LeadStatus.COMPLETED ? 0.9 : 0.3);
        return regression.predict(1.0); // Simplified scoring
    }

    private User findLeastBusyCounselor() {
        List<User> counselors = userRepository.findByRole(Role.COUNSELOR);
        User leastBusy = counselors.get(0);
        long minLeads = Long.MAX_VALUE;
        for (User counselor : counselors) {
            long leadCount = leadRepository.countByAssignedCounselor_UserId(counselor.getId());
            if (leadCount < minLeads) {
                minLeads = leadCount;
                leastBusy = counselor;
            }
        }
        return leastBusy;
    }

    private void logAudit(String userEmail, String action, String entityId, String entityType, String details) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityId(entityId);
        auditLog.setEntityType(entityType);
//        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public void registerSession(String userEmail, WebSocketSession session) {
        sessions.put(userEmail, session);
    }

    private void notifyCounselors(String message) {
        sessions.values().forEach(session -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
