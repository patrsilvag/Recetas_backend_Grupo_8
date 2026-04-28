package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class FullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔥 TEST PRINCIPAL (flujo real completo)
    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void testFlujoPrincipal() throws Exception {

        // 1. Crear receta
        Recipe r = new Recipe();
        r.setName("Cazuela");
        r.setDifficulty("Fácil");

        String json = objectMapper.writeValueAsString(r);

        String response = mockMvc
                .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        Recipe creada = objectMapper.readValue(response, Recipe.class);
        Long id = creada.getId();

        // ✨ VERIFICACIÓN ÉTICA DE PERSISTENCIA (Uso del Repositorio)
        // Validamos que el ID generado realmente exista en Oracle Cloud antes de seguir
        assertTrue(recipeRepository.existsById(id),
                "La receta debe persistir correctamente en la BD");

        // 2. Buscar receta
        mockMvc.perform(get("/recipes/search").param("name", "Cazuela")).andExpect(status().isOk());

        // 3. Subir imagen válida
        MockMultipartFile file =
                new MockMultipartFile("file", "foto.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/recipes/" + id + "/upload-photo").file(file))
                .andExpect(status().isOk());

        // 4. Agregar comentario válido
        Comment c = new Comment();
        c.setText("Excelente");
        c.setRating(5);

        mockMvc.perform(post("/recipes/" + id + "/comments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c))).andExpect(status().isOk());

        // 5. Eliminar receta
        mockMvc.perform(delete("/recipes/" + id)).andExpect(status().isNoContent());

        // ✨ VERIFICACIÓN FINAL: Aseguramos que la eliminación fue física en la base de datos
        assertTrue(recipeRepository.findById(id).isEmpty(),
                "La receta debe ser eliminada definitivamente");
    }
    // 🔥 TEST DE RAMAS EN USER
    @Test
    @Transactional
    void testUserAuthoritiesBranches() {
        User u = new User();

        // Rama: role null
        u.setRole(null);
        assertNotNull(u.getAuthorities());

        // Rama: role ADMIN
        u.setRole("ADMIN");
        assertNotNull(u.getAuthorities());

        // Métodos booleanos
        assertTrue(u.isEnabled());
        assertTrue(u.isAccountNonExpired());
        assertTrue(u.isAccountNonLocked());
        assertTrue(u.isCredentialsNonExpired());
    }
}
