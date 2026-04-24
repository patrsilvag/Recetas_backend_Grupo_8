package com.duoc.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            logger.error("Usuario no encontrado en Oracle Cloud: {}", username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        // 🛡️ Lógica Nativa: Aseguramos que el rol tenga el prefijo ROLE_ para Spring Security
        String roleName = user.getRole();
        if (roleName == null)
            roleName = "USER";

        String finalRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        logger.info("Usuario {} autenticado con rol: {}", username, finalRole);

        // Retornamos un objeto User de Spring Security basado en tu entidad
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername()).password(user.getPassword()) // Debe estar encriptado
                                                                           // con BCrypt
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(finalRole)))
                .build();
    }
}
