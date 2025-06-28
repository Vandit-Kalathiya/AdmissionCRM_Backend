package com.admissioncrm.authenticationservice.Entities.CoreEntities;

import com.admissioncrm.authenticationservice.DTO.CourseDTO;
import com.admissioncrm.authenticationservice.DTO.LeadDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "institutions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 10, name = "institute_code")
    private String instituteCode;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    private String universityId;

    private List<String> instituteAdminIds;

    @Column(name = "max_counselors")
    private Integer maxCounselors = 5;

    @Column(name = "current_counselors")
    private Integer currentCounselors = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private List<String> counselors = new ArrayList<>();

    private List<String> courses = new ArrayList<>();

    private List<String> leads = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
