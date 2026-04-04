package com.duoc.backend;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import static com.duoc.backend.Constants.*;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    /**
     * Extrae y valida los Claims del token JWT.
     */
    private Claims setSigningKey(HttpServletRequest request) {
        String jwtToken =
                request.getHeader(HEADER_AUTHORIZACION_KEY).replace(TOKEN_BEARER_PREFIX, "");

        return Jwts.parser().verifyWith((SecretKey) getSigningKey(SUPER_SECRET_KEY)).build()
                .parseSignedClaims(jwtToken).getPayload();
    }

    /**
     * Configura la autenticación en el contexto de seguridad de Spring.
     */
    private void setAuthentication(Claims claims) {
        Object authoritiesClaim = claims.get("authorities");
        List<String> authorities = new java.util.ArrayList<>();

        if (authoritiesClaim instanceof List<?>) {
            for (Object obj : (List<?>) authoritiesClaim) {
                if (obj instanceof String) {
                    authorities.add((String) obj);
                }
            }
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Verifica si el Header de autorización está presente y tiene el formato correcto.
     */
    private boolean isJWTValid(HttpServletRequest request, HttpServletResponse res) {
        String authenticationHeader = request.getHeader(HEADER_AUTHORIZACION_KEY);
        if (authenticationHeader == null || !authenticationHeader.startsWith(TOKEN_BEARER_PREFIX))
            return false;
        return true;
    }

    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request,
            @SuppressWarnings("null") HttpServletResponse response,
            @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            String method = request.getMethod(); // 👈 IMPORTANTE: Captura el método (GET o POST)

            
            // 🚶 1. EXCEPCIONES: Rutas 100% públicas (Login y Registro)
            if (path.equals("/login") || path.equals("/registro") || path.equals("/register")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 🚶 2. EXCEPCIÓN CONDICIONAL: Ver recetas o buscar es público, pero CREAR (POST) NO.
            if ((path.equals("/recipes") || path.equals("/recipes/")
                    || path.equals("/recipes/search")) && method.equalsIgnoreCase("GET")) {

                filterChain.doFilter(request, response);
                return;
            }


            // 🛑 BLOQUEO: Validación obligatoria para detalle y creación
            if (path.startsWith("/recipes/detail") || path.equals("/recipes/create")) {
                if (!isJWTValid(request, response)) {
                    // Si no es válido, detenemos el flujo aquí
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

            // Si hay un token válido presente (en cualquier ruta), procesamos la identidad
            if (isJWTValid(request, response)) {
                Claims claims = setSigningKey(request); // Corregido: antes validateToken
                if (claims.get("authorities") != null) {
                    setAuthentication(claims); // Corregido: antes setUpSpringAuthentication
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }
    }
}
