package com.admissioncrm.authenticationservice.Entities;

import com.admissioncrm.authenticationservice.Enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid mobile number")
    private String mobileNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    public String getRole() {
        return role.toString();
    }
}
