package com.admission_crm.lead_management.Entity.CoreEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    private String universityId;

    private String institutionId;

    @ElementCollection
    private List<String> assignedLeads = new ArrayList<>();

    @Column(name = "max_leads_assignment")
    private Integer maxLeadsAssignment = 50;

    @Column(name = "current_leads_count")
    private Integer currentLeadsCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isMainAdmin() {
        return role != null && "UNIVERSITY_ADMIN".equals(role.name());
    }

    public boolean isInstituteAdmin() {
        return role != null && "INSTITUTE_ADMIN".equals(role.name());
    }

    public boolean isCounselor() {
        return role != null && "COUNSELOR".equals(role.name());
    }

    public boolean isStudent() {
        return role != null && "STUDENT".equals(role.name());
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
