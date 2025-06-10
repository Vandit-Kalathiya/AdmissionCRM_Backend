package com.admissioncrm.authenticationservice.Services;

import com.admissioncrm.authenticationservice.Entities.User;
import com.admissioncrm.authenticationservice.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {


        Optional<User> user = userRepository.findByMobileNumber(mobileNumber);
        if (user.isPresent()) {
            return  org.springframework.security.core.userdetails.User
                    .withUsername(user.get().getMobileNumber())
                    .password(user.get().getPassword())
                    .authorities("ROLE_"+user.get().getRole())
                    .build();
        }
        return null;
    }
}
