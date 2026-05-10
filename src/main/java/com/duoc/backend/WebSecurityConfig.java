package com.duoc.backend;

import static org.springframework.security.config.Customizer.*;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity()
@Configuration
@EnableMethodSecurity
class WebSecurityConfig {

   

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http,
            JWTAuthorizationFilter jwtAuthorizationFilter) throws Exception {

        http.cors(withDefaults()).csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.POST, "/login", "/register", "/registro").permitAll()

                .requestMatchers(HttpMethod.GET, "/recipes", "/recipes/search/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                .requestMatchers("/error").permitAll()

                .requestMatchers(HttpMethod.GET, "/recipes/detail/**").authenticated()

                .requestMatchers(HttpMethod.GET, "/recipes/private/**").authenticated()

                .requestMatchers(HttpMethod.POST, "/recipes/create", "/recipes/*/comments")
                .authenticated()

                .requestMatchers("/users/**").hasRole("ADMIN")

                .anyRequest().authenticated())

                .addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (Asegúrate de incluir todos los puertos de tus entornos)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8002",
                "http://localhost:8080", "http://127.0.0.1:8002", "http://localhost"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Importante: "Authorization" debe estar permitido para que el JWT pase desde el Front
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
