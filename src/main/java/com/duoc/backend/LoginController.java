package com.duoc.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LoginController {

    // ✅ Inyección por Constructor: Elimina @Autowired para Rating A en Reliability
    private final JWTAuthenticationConfig jwtAuthenticationConfig;
    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public LoginController(JWTAuthenticationConfig jwtAuthenticationConfig,
            MyUserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // ✅ Uso de DTO y tipo explícito <String> para Rating A en Security y Maintainability
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDto) {
        if (loginDto == null || loginDto.username() == null || loginDto.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        try {
            // 1. Buscar usuario en Oracle
            final UserDetails userDetails =
                    userDetailsService.loadUserByUsername(loginDto.username());

            // 2. Validar contraseña
            if (!passwordEncoder.matches(loginDto.password(), userDetails.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas");
            }

            // 3. Generar token con roles reales
            List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
            String token = jwtAuthenticationConfig.getJWTToken(loginDto.username(), authorities);

            return ResponseEntity.ok(Objects.requireNonNull(token));

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody(required = false) LoginDTO newUserDto) {

        if (newUserDto == null || newUserDto.username() == null || newUserDto.password() == null) {

            return ResponseEntity.badRequest().body("Error: Datos obligatorios ausentes");
        }

        if (userRepository.existsByUsername(newUserDto.username())) {

            return ResponseEntity.badRequest().body("Error: El usuario ya existe");
        }

        if (newUserDto.email() != null && userRepository.existsByEmail(newUserDto.email())) {

            return ResponseEntity.badRequest().body("Error: El correo ya está registrado");
        }

        User user = new User();
        user.setUsername(newUserDto.username());
        user.setEmail(newUserDto.email());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(newUserDto.password()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
    }
}
