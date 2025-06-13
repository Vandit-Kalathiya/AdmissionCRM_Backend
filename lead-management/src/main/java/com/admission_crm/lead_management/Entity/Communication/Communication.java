package com.admission_crm.lead_management.Entity.Communication;


import com.admission_crm.lead_management.Entity.CoreEntities.User;
import com.admission_crm.lead_management.Entity.LeadManagement.Lead;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "communications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Communication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String leadId;

    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationType type;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private CommunicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(columnDefinition = "JSON")
    private String attachments;

    private String communicationTemplate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum CommunicationType {
        EMAIL, SMS, WHATSAPP, CALL, MEETING
    }

    public enum CommunicationStatus {
        SENT, DELIVERED, READ, FAILED
    }
}
