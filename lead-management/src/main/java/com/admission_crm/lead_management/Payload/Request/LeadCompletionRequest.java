package com.admission_crm.lead_management.Payload.Request;

import com.admission_crm.lead_management.Entity.LeadManagement.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCompletionRequest {

    @NotBlank(message = "Lead ID is required")
    private String leadId;

    @NotNull(message = "Final status is required")
    private LeadStatus finalStatus;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private String completionReason;
    private LocalDateTime nextFollowUpDate;
    private Double conversionValue; // if converted
}
