package com.duoc.backend;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
public class LoginController {

    @Autowired
    private JWTAuthenticationConfig jwtAuthenticationConfig;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository; // AGREGADO PARA EL REGISTRO

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("user") String username,
            @RequestParam("password") String password) {

        try {
            // 1. Buscar usuario en Oracle (Carga username, password y ROLE)
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 2. Validar contraseña
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas");
            }

            // 3. GENERAR TOKEN CON ROLES REALES 🚨
            // Convertimos a List de forma segura
            List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());

            String token = jwtAuthenticationConfig.getJWTToken(username, authorities);

            return ResponseEntity.ok(token);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    // NUEVO ENDPOINT DE REGISTRO - SEMANA 4
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {

        // Validar que el usuario no exista previamente
        if (newUser.getUsername() != null
                && userRepository.existsByUsername(newUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El usuario ya existe");
        }

        if (newUser.getEmail() != null && userRepository.existsByEmail(newUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El correo ya está registrado");
        }

        // FORZAR ROL POR DEFECTO: Todos los registros públicos serán 'USER'
        newUser.setRole("USER");

        // 🛡️ REQUISITO: Encriptar la contraseña antes de guardar (OWASP A02)
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // Guardar en la base de datos
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
    }
}
