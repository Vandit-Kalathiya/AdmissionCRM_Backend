package com.admissioncrm.authenticationservice.Entities;

import jakarta.persistence.*;

@Entity
public class CounsellorDetails {
    @Id
    private String id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Users user;

    private String assignedInstitute;
    private String expertiseArea;
    // more counsellor-specific fields
}
