package com.admissioncrm.authenticationservice.Controllers;

import com.admissioncrm.authenticationservice.DTO.student.StudentRegistrationRequest;
import com.admissioncrm.authenticationservice.Services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("auth")
@RestController
public class AuthController {
    AuthenticationService authenticationService;
    AuthController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;

    }

    @GetMapping("/student/login")
    public String home() {
        return "hello dhup" ;
    }

//    @PostMapping("/student/register")
//    public ResponseEntity<String> registerStudent(@Valid  @RequestBody StudentRegistrationRequest studentRegistrationRequest)
//    {
//
//    }

}
