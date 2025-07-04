//package com.admissioncrm.authenticationservice.Config;
//
//import com.admissioncrm.authenticationservice.Entities.CoreEntities.Role;
//import com.admissioncrm.authenticationservice.Entities.CoreEntities.User;
//import com.admissioncrm.authenticationservice.Repositories.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//
//@Configuration
//public class SuperAdminInitializer {
//    @Autowired
//    PasswordEncoder passwordEncoder;
//    @Bean
//    public CommandLineRunner initSuperAdmin(UserRepository userRepository) {
//        return args -> {
//            String superAdminMobile = "9999999999";
//            String superAdminEmail = "admin@university.com";
//
//            // Check if already exists
//            boolean exists = userRepository.existsByPhone(superAdminMobile);
//            if (!exists) {
//                User superAdmin = new User();
//                superAdmin.setFirstName("Super");
//                superAdmin.setLastName("Admin");
//                superAdmin.setUsername("superadmin");
//                superAdmin.setPhone(superAdminMobile);
//                superAdmin.setEmail(superAdminEmail);
//                superAdmin.setPassword(passwordEncoder.encode("abc"));
//                superAdmin.setRole(Role.UNIVERSITY_ADMIN);
//                superAdmin.setCreatedAt(LocalDateTime.now());
//                superAdmin.setUpdatedAt(LocalDateTime.now());
//
//                userRepository.save(superAdmin);
//                System.out.println("✅ Super Admin created successfully!");
//            } else {
//                System.out.println("ℹ️ Super Admin already exists.");
//            }
//        };
//    }
//}
//
