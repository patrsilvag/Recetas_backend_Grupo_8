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
import org.springframework.test.util.ReflectionTestUtils; // ✅ Necesario para simular @Value
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

    // ✅ Definimos la misma clave que en el application.properties para el test
    private final String testSecret =
            "ZnJhc2VzbGFyZ2FzcGFyYWNvbG9jYXJjb21vY2xhdmVlbnVucHJvamVjdG9kZWVtZXBsb3BhcmFqd3Rjb25zcHJpbmdzZWN1cml0eQ==";

    @BeforeEach
    void setUp() {
        // ✅ SIMULACIÓN DE @Value: Inyectamos el secreto manualmente en el campo privado del filtro
        ReflectionTestUtils.setField(filter, "jwtSecret", testSecret);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_SinToken() throws Exception {
        when(request.getHeader(HEADER_AUTHORIZACION_KEY)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_TokenValido() throws Exception {
        // 1. Generamos el token usando la clave de prueba
        String tokenReal =
                Jwts.builder().subject("admin_test").claim("authorities", List.of("ROLE_ADMIN"))
                        .issuedAt(new Date(System.currentTimeMillis()))
                        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                        .signWith(Constants.getSigningKey(testSecret)) // ✅ Usamos testSecret
                        .compact();

        when(request.getHeader(HEADER_AUTHORIZACION_KEY))
                .thenReturn(TOKEN_BEARER_PREFIX + tokenReal);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin_test",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_TokenMalformado_LanzaExcepcion() throws Exception {
        when(request.getHeader(HEADER_AUTHORIZACION_KEY))
                .thenReturn(TOKEN_BEARER_PREFIX + "token_basura_12345");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }
}
