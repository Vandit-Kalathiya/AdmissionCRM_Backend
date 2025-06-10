package com.admissioncrm.authenticationservice.Controllers;

import com.admissioncrm.authenticationservice.DTO.Jwt.JwtResponse;
import com.admissioncrm.authenticationservice.DTO.student.StudentLoginRequest;
import com.admissioncrm.authenticationservice.DTO.student.StudentRegistrationRequest;
import com.admissioncrm.authenticationservice.Services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {
    AuthenticationService authenticationService;
    AuthController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;

    }

    @PostMapping("/student/login")
    public ResponseEntity<?> loginStudent(@Valid @RequestBody StudentLoginRequest studentLoginRequest) {

        return authenticationService.loginStudent(studentLoginRequest);
    }

    @PostMapping("/student/register")
    public ResponseEntity<?> registerStudent(@Valid  @RequestBody StudentRegistrationRequest studentRegistrationRequest)
    {

       // String jwtToken=authenticationService.registerStudent(studentRegistrationRequest);

       JwtResponse jwtResponse=authenticationService.registerStudent(studentRegistrationRequest);

            return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/home")
    public String studentDash()
    {
        return "Welcome to the Admission CRM Authentication Service";
    }
}
