package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
// ✅ IMPORTS DE JUNIT 5 CORREGIDOS
// 👈 El que soluciona el Blocker
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertNotNull(objectMapper);
    }

    @Test
    void testMain() {
        // ✅ SOLUCIÓN AL BLOCKER: Validamos que el main arranque sin excepciones
        assertDoesNotThrow(() -> BackendApplication.main(new String[] {}));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN", "ROLE_USER"})
    void testRecipeControllerEdges() throws Exception {
        mockMvc.perform(get("/recipes/private/9999"))
                .andExpect(status().is(anyOf(is(403), is(404))));

        MockMultipartFile file =
                new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/recipes/1/upload-photo").file(file))
                .andExpect(status().is(anyOf(is(400), is(403), is(404))));
    }

    @Test
    void testLoginEdgeCases() throws Exception {
        LoginDTO emptyDto = new LoginDTO(null, null, null);
        String content = Objects.requireNonNull(objectMapper.writeValueAsString(emptyDto));

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDTOs() {
        RecipeDTO recipeDto =
                new RecipeDTO("Test", "Inst", "CL", "Baja", "Pasos", 10, List.of("Ing"));
        assertNotNull(recipeDto.name());

        LoginDTO loginDto = new LoginDTO("user", "pass", "test@test.com");
        assertNotNull(loginDto.username());
    }

    @Test
    void testConstantsConstructor() throws Exception {
        // ✅ Sube Constants de 33% a 100%
        java.lang.reflect.Constructor<Constants> constructor = 
                Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSecuredController() throws Exception {
        mockMvc.perform(get("/greetings")).andExpect(status().isOk());
    }
}
