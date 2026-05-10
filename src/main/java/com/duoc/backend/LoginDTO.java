package com.duoc.backend;

/**
 * DTO para la gestión de acceso y registro. Protege la integridad de la base de datos y cumple con
 * los estándares de seguridad de SonarQube.
 */
public record LoginDTO(String username, String password, String email) {
}
