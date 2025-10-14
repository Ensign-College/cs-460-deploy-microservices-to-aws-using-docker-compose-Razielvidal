package com.example.explorecalijpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    var user = User.withUsername("user")
        .password(encoder.encode("password"))
        .roles("USER")
        .build();
    var admin = User.withUsername("admin")
        .password(encoder.encode("admin123"))
        .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // stateless API: disable CSRF for curl/Postman
        .csrf(csrf -> csrf.disable())

        // authorization rules per homework
        .authorizeHttpRequests(auth -> auth
            // public docs/health
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                "/actuator/health", "/actuator/info")
            .permitAll()

            // READS → authenticated (USER or ADMIN)
            .requestMatchers(HttpMethod.GET, "/tours/**", "/packages/**")
            .hasAnyRole("USER", "ADMIN")

            // WRITES → ADMIN only
            .requestMatchers(HttpMethod.POST, "/tours/**", "/packages/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/tours/**", "/packages/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/tours/**", "/packages/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/tours/**", "/packages/**").hasRole("ADMIN")

            // everything else must be authenticated
            .anyRequest().authenticated())

        // HTTP Basic (no login form)
        .httpBasic(Customizer.withDefaults())
        .formLogin(form -> form.disable());

    return http.build();
  }
}
