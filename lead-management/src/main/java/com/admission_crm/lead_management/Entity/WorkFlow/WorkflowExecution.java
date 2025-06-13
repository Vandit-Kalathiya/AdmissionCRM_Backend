package com.admission_crm.lead_management.Entity.WorkFlow;

import com.admission_crm.lead_management.Entity.Application.Application;
import com.admission_crm.lead_management.Entity.CoreEntities.User;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "workflow_type", nullable = false, length = 50)
    private String workflowType;

    private String leadId;

    private String applicationId;

    private String triggeredBy;

    @Enumerated(EnumType.STRING)
    private WorkflowStatus status;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "workflow_data", columnDefinition = "JSON")
    private String workflowData;

    @Column(name = "result_data", columnDefinition = "JSON")
    private String resultData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum WorkflowStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }
}
