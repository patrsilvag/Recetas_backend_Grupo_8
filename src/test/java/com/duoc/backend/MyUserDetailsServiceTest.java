package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Test
    void loadUserByUsername_Success() {
        // Preparar
        User user = new User();
        user.setUsername("admin");
        user.setPassword("password_encriptada");
        user.setRole("ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(user);

        // Ejecutar
        UserDetails result = myUserDetailsService.loadUserByUsername("admin");

        // Validar
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_RoleNull_AssignsUser() {
        // Preparar: Usuario con rol nulo y PASSWORD NO NULO 🛡️
        User user = new User();
        user.setUsername("user_nuevo");
        user.setPassword("123456"); // <-- Agrega esto para evitar el error
        user.setRole(null);
        when(userRepository.findByUsername("user_nuevo")).thenReturn(user);

        // Ejecutar
        UserDetails result = myUserDetailsService.loadUserByUsername("user_nuevo");

        // Validar: Debe tener ROLE_USER por defecto
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        // Preparar: Simular que el repositorio no encuentra al usuario
        when(userRepository.findByUsername("fantasma")).thenReturn(null);

        // Ejecutar y Validar
        assertThrows(UsernameNotFoundException.class, () -> {
            myUserDetailsService.loadUserByUsername("fantasma");
        });
    }
}
