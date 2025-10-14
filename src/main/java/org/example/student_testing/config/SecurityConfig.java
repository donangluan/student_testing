package org.example.student_testing.config;


import org.example.student_testing.student.service.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailService userDetailService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/register", "/register/**").permitAll()
                            .requestMatchers("/verify-otp").permitAll()
                            .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                            .requestMatchers("/student/profile/**").hasAnyRole( "ADMIN","TEACHER","STUDENT")
                            .requestMatchers("/student/**").hasAnyRole( "ADMIN","TEACHER")

                                    .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage("/login")
                            .successHandler((request, response, authentication) -> {
                                boolean isStudent = authentication.getAuthorities().stream()
                                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
                                boolean isTeacher = authentication.getAuthorities().stream()
                                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));
                                boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

                                System.out.println("Authorities: " + authentication.getAuthorities());


                                if (isStudent) {
                                    response.sendRedirect("/student/profile/list");
                                } else if (isTeacher || isAdmin) {
                                    response.sendRedirect("/student/list");
                                } else {
                                    response.sendRedirect("/access-denied");
                                }
                            })
                            .permitAll()
                    ).
                    logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout")
                            .permitAll());
            return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
        return builder.build();
    }
 }
