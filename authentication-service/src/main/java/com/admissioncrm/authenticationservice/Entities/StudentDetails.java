package com.admissioncrm.authenticationservice.Entities;

import com.admissioncrm.authenticationservice.Entities.CoreEntities.User;
import jakarta.persistence.*;

@Entity
public class StudentDetails {
    @Id
    private String id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id") // same ID as Users table
    private User user;

    private String preferredCourse;
    private String educationLevel;
    private String gender;
    // more student-specific fields
}
