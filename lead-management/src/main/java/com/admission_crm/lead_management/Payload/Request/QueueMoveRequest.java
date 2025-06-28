package com.admission_crm.lead_management.Payload.Request;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueMoveRequest {

    private String leadId;
    private Integer newPosition;
    private String reason;
}
