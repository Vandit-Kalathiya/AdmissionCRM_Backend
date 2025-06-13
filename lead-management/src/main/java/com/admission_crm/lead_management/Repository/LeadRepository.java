package com.admission_crm.lead_management.Repository;

import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LeadRepository extends JpaRepository<Lead, String> {

    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
    Page<Lead> findBySource(String source, Pageable pageable);
    Page<Lead> findByAssignedCounselor_UserId(String counselorId, Pageable pageable);

    Long countByAssignedCounselor_UserId(String userId);
}
