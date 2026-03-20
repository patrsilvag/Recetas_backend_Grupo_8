package com.duoc.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Imports necesarios para el PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity()
@Configuration
class WebSecurityConfig {

        @Autowired
        JWTAuthorizationFilter jwtAuthorizationFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain configure(HttpSecurity http) throws Exception {

                http
                                .csrf((csrf) -> csrf
                                                .disable())
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/recipes/**").permitAll()
                                                .anyRequest().authenticated())
                                .addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
} // <--- ESTA ES LA LLAVE QUE FALTABA