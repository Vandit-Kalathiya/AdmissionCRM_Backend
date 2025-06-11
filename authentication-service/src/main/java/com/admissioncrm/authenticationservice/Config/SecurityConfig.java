package com.admissioncrm.authenticationservice.Config;

import com.admissioncrm.authenticationservice.Services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    JwtFilter jwtFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        //enable CORS
        http.cors(cors-> cors.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(authorize -> {
                // Public endpoints - no authentication
                authorize.requestMatchers(
                        "/auth/student/login",
                        "/auth/student/register",
                        "/auth/sadmin/login",
                        "/auth/iadmin/login",
                        "/actuator/**"
                ).permitAll();



                // Role-based endpoints
                authorize.requestMatchers("/auth/admin/**").hasAnyRole("UNIVERSITY_ADMIN", "INSTITUTE_ADMIN");
                authorize.requestMatchers("/api/university-admin/**").hasRole("UNIVERSITY_ADMIN");
                authorize.requestMatchers("/api/institute-admin/**").hasAnyRole("UNIVERSITY_ADMIN", "INSTITUTE_ADMIN");
                authorize.requestMatchers("/api/counsellor/**").hasAnyRole("UNIVERSITY_ADMIN", "INSTITUTE_ADMIN", "COUNSELLOR");
                authorize.requestMatchers("/api/student/**").hasRole("STUDENT");

            //authentication needed for this
            authorize.anyRequest().authenticated();

            });
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); //add Frontend URL's here aa
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();

    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
