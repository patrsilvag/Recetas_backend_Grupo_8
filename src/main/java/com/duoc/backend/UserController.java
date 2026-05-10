package com.duoc.backend;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')") // 🔒 Privado: Solo administradores
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Listar todos los usuarios
    @GetMapping
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Cambiar el rol de un usuario
    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable @NonNull Integer id,
            @RequestParam String newRole) {

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        user.setRole(newRole);

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    // 3. Eliminar un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @NonNull Integer id) {

        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
