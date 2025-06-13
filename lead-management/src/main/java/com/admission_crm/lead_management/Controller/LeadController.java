package com.admission_crm.lead_management.Controller;

import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<Lead> createLead(@RequestBody Lead lead, Authentication authentication) {
        return ResponseEntity.ok(leadService.createLead(lead, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> updateLead(@PathVariable String id, @RequestBody Lead lead, Authentication authentication) {
        return ResponseEntity.ok(leadService.updateLead(id, lead, authentication.getName()));
    }

    @PostMapping("/assign")
    public ResponseEntity<Lead> assignLead(@RequestParam String leadId, @RequestParam String counselorId, Authentication authentication) {
        return ResponseEntity.ok(leadService.assignLead(leadId, counselorId, authentication.getName()));
    }

    @PostMapping("/auto-assign")
    public ResponseEntity<Lead> autoAssignLead(@RequestParam String leadId, Authentication authentication) {
        return ResponseEntity.ok(leadService.autoAssignLead(leadId, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<Lead>> getLeads(Pageable pageable,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String source,
                                               @RequestParam(required = false) String counselorId) {
        return ResponseEntity.ok(leadService.getLeads(pageable, status, source, counselorId));
    }

    @GetMapping("/{id}/score")
    public ResponseEntity<Double> getLeadScore(@PathVariable String id) {
        return ResponseEntity.ok(leadService.calculateLeadScore(id));
    }
}
