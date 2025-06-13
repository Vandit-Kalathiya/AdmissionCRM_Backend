package com.admission_crm.lead_management.Repository;

import com.admission_crm.lead_management.Entity.AnalyticsAndReporting.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
