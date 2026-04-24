package com.duoc.backend;

import static com.duoc.backend.Constants.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    /**
     * Extrae y valida los Claims del token JWT.
     */
    private Claims setSigningKey(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HEADER_AUTHORIZACION_KEY);
        String jwtToken = authenticationHeader.replace(TOKEN_BEARER_PREFIX, "");

        return Jwts.parser().verifyWith((SecretKey) getSigningKey(JWT_SIGNING_VALUE)).build()
                .parseSignedClaims(jwtToken).getPayload();
    }

    /**
     * Configura la autenticación en el contexto de seguridad de Spring. Mapea los roles del token a
     * Authorities nativos de Spring Security.
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
    private boolean isJWTValid(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HEADER_AUTHORIZACION_KEY);
        return authenticationHeader != null && authenticationHeader.startsWith(TOKEN_BEARER_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 🛡️ LÓGICA NATIVA: El filtro ya no decide rutas.
            // Si hay un token, lo valida y carga al "SecurityContext".
            if (isJWTValid(request)) {
                Claims claims = setSigningKey(request);

                if (claims.get("authorities") != null) {
                    setAuthentication(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                // Si no hay token, simplemente limpiamos el contexto.
                // Spring Security bloqueará el acceso después si la ruta es privada.
                SecurityContextHolder.clearContext();
            }

            // Continuamos la cadena de filtros hacia WebSecurityConfig
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            // Si el token es inválido o expiró, respondemos 403 inmediatamente.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Sesión inválida: " + e.getMessage());
        }
    }
}
