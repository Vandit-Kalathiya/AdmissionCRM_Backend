package com.admissioncrm.authenticationservice.Entities;

import jakarta.persistence.*;

@Entity
public class UniversityAdminDetails {
    @Id
    private String id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Users user;

    private boolean superAdmin; // example field

    // You can add more fields if needed later
}
