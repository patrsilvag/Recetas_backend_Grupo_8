package com.duoc.backend;

import static com.duoc.backend.Constants.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class JWTAuthorizationFilterTest {

    @InjectMocks
    private JWTAuthorizationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        // Limpiar el contexto antes y después de cada test para evitar contaminación cruzada
        SecurityContextHolder.clearContext();
    }

    // ✅ Escenario 1: Petición sin Token (Continúa el filtro normalmente)
    @Test
    void doFilterInternal_SinToken() throws Exception {
        when(request.getHeader(HEADER_AUTHORIZACION_KEY)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    // ✅ Escenario 2: Token 100% Válido y Firmado (Atraviesa toda la lógica)
    @Test
    void doFilterInternal_TokenValido() throws Exception {
        // 1. Generamos un token real usando la misma lógica y llave secreta de tu Constants
        String tokenReal =
                Jwts.builder().subject("admin_test").claim("authorities", List.of("ROLE_ADMIN"))
                        .issuedAt(new Date(System.currentTimeMillis()))
                        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                        .signWith(getSigningKey(JWT_SIGNING_VALUE)).compact();

        // 2. Lo inyectamos en el Header del request simulado
        when(request.getHeader(HEADER_AUTHORIZACION_KEY))
                .thenReturn(TOKEN_BEARER_PREFIX + tokenReal);

        // 3. Ejecutamos el filtro
        filter.doFilterInternal(request, response, filterChain);

        // 4. Validamos que el SecurityContext sí reconoció al usuario (Cobertura al máximo)
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin_test",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    // ✅ Escenario 3: Token Corrupto o Falso (Cae en el Catch -> 403 Forbidden)
    @Test
    void doFilterInternal_TokenMalformado_LanzaExcepcion() throws Exception {
        when(request.getHeader(HEADER_AUTHORIZACION_KEY))
                .thenReturn(TOKEN_BEARER_PREFIX + "token_basura_12345");

        filter.doFilterInternal(request, response, filterChain);

        // Validamos que se ejecute el catch y devuelva un error HTTP 403
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }
}
