package com.admission_crm.lead_management.Entity.AnalyticsAndReporting;


import com.admission_crm.lead_management.Entity.CoreEntities.Institution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_funnels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionFunnel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "lead_source_id")
//    private LeadSource leadSource;

    private String institutionId;

    @Column(name = "stage_from", length = 50)
    private String stageFrom;

    @Column(name = "stage_to", length = 50)
    private String stageTo;

    @Column(name = "conversion_count")
    private Long conversionCount;

    @Column(name = "date_range_start")
    private LocalDate dateRangeStart;

    @Column(name = "date_range_end")
    private LocalDate dateRangeEnd;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
