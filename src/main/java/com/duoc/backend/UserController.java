package com.duoc.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')") // 🔒 Privado: Solo administradores
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Listar todos los usuarios
    @GetMapping
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Cambiar el rol de un usuario
    @PutMapping("/{id}/role")
    // 💡 CAMBIO AQUÍ: de Long id a Integer id
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
    // 💡 CAMBIO AQUÍ: de Long id a Integer id
    public ResponseEntity<Void> deleteUser(@PathVariable @NonNull Integer id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
