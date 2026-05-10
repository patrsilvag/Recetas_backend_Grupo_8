package com.duoc.backend;

import static com.duoc.backend.Constants.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class JWTAuthenticationConfig {

    @Value("${jwt.secret}")
    private String jwtSecret; // ✅ Inyección directa
    
    // 🚨 CAMBIO: Ahora recibe las autoridades (roles) como parámetro
    public String getJWTToken(String username, List<GrantedAuthority> grantedAuthorities) {

        Map<String, Object> claims = new HashMap<>();

        // ✅ CORRECCIÓN 1: Se reemplazó .collect(Collectors.toList()) por .toList() (Java 16+)
        // Ahora guardamos los roles REALES que le pasemos
        claims.put("authorities",
                grantedAuthorities.stream().map(GrantedAuthority::getAuthority).toList());

        // ✅ CORRECCIÓN 2: Retorno inmediato (Elimina la variable temporal "token")
        // ✅ CORRECCIÓN 3: Uso de la constante TOKEN_EXPIRATION_TIME para evitar números mágicos
        return Jwts.builder().claims().add(claims).subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME)).and()
                .signWith(getSigningKey(jwtSecret)).compact();
    }
}
