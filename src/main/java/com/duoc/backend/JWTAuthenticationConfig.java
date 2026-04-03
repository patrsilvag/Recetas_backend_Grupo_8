package com.duoc.backend;

    import static com.duoc.backend.Constants.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import io.jsonwebtoken.Jwts;

    @Configuration
    public class JWTAuthenticationConfig {

    // 🚨 CAMBIO: Ahora recibe las autoridades (roles) como parámetro
    public String getJWTToken(String username, List<GrantedAuthority> grantedAuthorities) {

        Map<String, Object> claims = new HashMap<>();
        // Ahora guardamos los roles REALES que le pasemos
        claims.put("authorities", grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 1440))
                .and()
                .signWith(getSigningKey(SUPER_SECRET_KEY))
                .compact();

        return "Bearer " + token;
    }
}

