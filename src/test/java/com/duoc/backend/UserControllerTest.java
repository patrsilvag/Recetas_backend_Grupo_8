package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional // Limpia la base de datos después de cada test
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // ✅ 1. Listar Usuarios (Cubre GET /users)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    // ✅ 2. Cambiar Rol Exitoso (Cubre PUT 200 OK)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserRole_Success() throws Exception {
        User user = new User();
        user.setUsername("test_role");
        user.setPassword("pass123");
        user.setRole("USER");
        user = userRepository.save(user);

        mockMvc.perform(put("/users/" + user.getId() + "/role").param("newRole", "ADMIN"))
                .andExpect(status().isOk());
    }

    // ✅ 3. Cambiar Rol No Encontrado (Cubre PUT 404 Not Found)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserRole_NotFound() throws Exception {
        mockMvc.perform(put("/users/99999/role").param("newRole", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    // ✅ 4. Eliminar Usuario Exitoso (Cubre DELETE 204 No Content)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        User user = new User();
        user.setUsername("test_delete");
        user.setPassword("pass123");
        user = userRepository.save(user);

        mockMvc.perform(delete("/users/" + user.getId())).andExpect(status().isNoContent());
    }

    // ✅ 5. Eliminar Usuario No Encontrado (Cubre DELETE 404 Not Found)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_NotFound() throws Exception {
        mockMvc.perform(delete("/users/99999")).andExpect(status().isNotFound());
    }
}
