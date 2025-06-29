package com.admission_crm.lead_management.Payload.Request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignRequest {

    @NotEmpty(message = "Lead IDs list cannot be empty")
    private List<String> leadIds;

    @NotNull(message = "Counselor ID is required")
    private String counselorId;
}