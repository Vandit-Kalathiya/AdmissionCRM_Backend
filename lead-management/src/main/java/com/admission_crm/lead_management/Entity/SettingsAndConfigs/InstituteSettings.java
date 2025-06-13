package com.admission_crm.lead_management.Entity.SettingsAndConfigs;

import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "institute_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstituteSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String institutionId;

    @Column(name = "lead_auto_assignment")
    private Boolean leadAutoAssignment = true;

    @Column(name = "max_leads_per_counselor")
    private Integer maxLeadsPerCounselor = 50;

    @Column(name = "working_hours_start")
    private LocalTime workingHoursStart = LocalTime.of(9, 0);

    @Column(name = "working_hours_end")
    private LocalTime workingHoursEnd = LocalTime.of(18, 0);

    @Column(name = "weekend_working")
    private Boolean weekendWorking = false;

    @Column(name = "notification_settings", columnDefinition = "JSON")
    private String notificationSettings;

    @Column(name = "custom_fields", columnDefinition = "JSON")
    private String customFields;

    @Column(name = "email_signature", columnDefinition = "TEXT")
    private String emailSignature;

    @Column(name = "sms_template", columnDefinition = "TEXT")
    private String smsTemplate;

    @Column(name = "auto_follow_up_enabled")
    private Boolean autoFollowUpEnabled = true;

    @Column(name = "follow_up_interval_hours")
    private Integer followUpIntervalHours = 24;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
