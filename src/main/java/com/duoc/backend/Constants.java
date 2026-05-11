package com.duoc.backend;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class Constants {

    // ✅ CORRECCIÓN: Constructor privado para ocultar el público implícito (Elimina Issue L8)
    private Constants() {
        throw new UnsupportedOperationException(
                "Esta es una clase de constantes y no debe ser instanciada");
    }

    // Spring Security
    public static final String LOGIN_URL = "/login";
    public static final String HEADER_AUTHORIZACION_KEY = "Authorization";
    public static final String TOKEN_BEARER_PREFIX = "Bearer ";

    // JWT
    public static final String ISSUER_INFO = "https://www.duocuc.cl/";
    // ✅ Inicializar con el mismo valor del properties por seguridad
   
    public static final long TOKEN_EXPIRATION_TIME = 864_000_000; // 10 day

    

    public static Key getSigningKeyB64(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static Key getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
