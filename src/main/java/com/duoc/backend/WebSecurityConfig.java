package com.duoc.backend;

import static org.springframework.security.config.Customizer.*;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
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
// NUEVOS IMPORTS PARA CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@EnableWebSecurity()
@Configuration
@EnableMethodSecurity
class WebSecurityConfig {

    @Autowired
    JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.cors(withDefaults()).csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
                // 🔓 RUTAS PÚBLICAS
                .requestMatchers(HttpMethod.POST, "/login", "/register",
                        "/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/recipes/**").permitAll()
                .requestMatchers("/error").permitAll()

                // 🔐 RUTAS PRIVADAS (Requieren Token JWT)
                // El detalle y la creación de recetas piden autenticación
                .requestMatchers(HttpMethod.GET, "/recipes/detail/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/recipes/create").authenticated()

                .anyRequest().authenticated())
                .addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 2. Definimos qué orígenes pueden hablar con este Backend (8081)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ACTUALIZADO: Añadimos el puerto 8002 (tu nuevo Front)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8002",
                "http://localhost:8080", "http://127.0.0.1:8002", "http://localhost"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
