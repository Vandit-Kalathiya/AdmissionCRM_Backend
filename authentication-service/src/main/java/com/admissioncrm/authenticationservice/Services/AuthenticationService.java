package com.admissioncrm.authenticationservice.Services;

import com.admissioncrm.authenticationservice.DTO.Jwt.JwtResponse;
import com.admissioncrm.authenticationservice.DTO.student.StudentLoginRequest;
import com.admissioncrm.authenticationservice.DTO.student.StudentRegistrationRequest;
import com.admissioncrm.authenticationservice.DTO.loginRequestViaEmail;
import com.admissioncrm.authenticationservice.Entities.User;
import com.admissioncrm.authenticationservice.Enums.Role;
import com.admissioncrm.authenticationservice.ExceptionHandling.ApiException;
import com.admissioncrm.authenticationservice.Repositories.UserRepository;
import com.admissioncrm.authenticationservice.Utilities.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils   jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;
@Transactional
    public JwtResponse registerStudent(StudentRegistrationRequest request) {

            if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
                throw new ApiException("Mobile number already registered");
            }
            User user = new User();
            user.setMobileNumber(request.getMobileNumber());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.STUDENT);

            userRepository.save(user);

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getMobileNumber(), request.getPassword())
            );

            String jwtToken = jwtUtils.generateToken(user.getMobileNumber(), Role.STUDENT);
            return new JwtResponse(jwtToken, Role.STUDENT.toString());


    }

    public ResponseEntity<?> loginStudent(StudentLoginRequest request) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getMobileNumber(), request.getPassword())
            );
            User user=userRepository.findByMobileNumber(request.getMobileNumber())
                    .orElseThrow(() -> new ApiException("User not found"));
            if (user.getRole() != Role.STUDENT) {
                throw new ApiException("Access denied: Not a student");
            }
            String jwtToken=jwtUtils.generateToken(user.getMobileNumber(), Role.STUDENT);

            JwtResponse jwtResponse=new JwtResponse(jwtToken,user.getRole().toString());
            return ResponseEntity.ok(jwtResponse);
        }catch(Exception e){
            throw new ApiException("Invalid username or password");
        }
    }
    //Admin Login handeling

    public ResponseEntity<?> loginSuperAdmin(loginRequestViaEmail request)
    {
        return loginAdminWithRole(Role.UNIVERSITY_ADMIN,request);
    }
    public ResponseEntity<?> loginInstitueAdmin(loginRequestViaEmail request)
    {
        return loginAdminWithRole(Role.INSTITUTE_ADMIN,request);
    }

    public ResponseEntity<?> loginAdminWithRole(Role expectedRole, loginRequestViaEmail request){

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ApiException("User not found"));

            if (user.getRole() != expectedRole) {
                throw new ApiException("Access denied for this role");
            }

            String jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole());

            return ResponseEntity.ok(new JwtResponse(jwtToken, user.getRole().toString()));
        } catch (Exception e) {
            throw new ApiException("Invalid credentials" );
        }

    }
}
