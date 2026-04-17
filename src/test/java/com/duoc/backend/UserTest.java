package com.duoc.backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserGettersAndSetters() {
        // Preparar
        User user = new User();
        user.setId(1);
        user.setUsername("admin_prueba");
        user.setRole("ADMIN");

        // Validar
        assertEquals(1, user.getId());
        assertEquals("admin_prueba", user.getUsername());
        assertEquals("ADMIN", user.getRole());
        assertTrue(user.isEnabled());
    }
}
