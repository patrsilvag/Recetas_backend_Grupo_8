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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@SuppressWarnings("null")
class RecipeControllerBranchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔴 1. CREATE con null → 400
    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecipe_null() throws Exception {
        mockMvc.perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
    }

    // 🔴 2. DELETE inexistente → 404
    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/recipes/99999")).andExpect(status().isNotFound());
    }

    // 🔴 3. UPLOAD archivo vacío → 400
    @Test
    @WithMockUser(roles = "ADMIN")
    void upload_emptyFile() throws Exception {
        Recipe r = new Recipe();
        r.setName("Receta Test");
        r = recipeRepository.save(r);

        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/recipes/" + r.getId() + "/upload-photo").file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    // 🔴 4. UPLOAD receta inexistente → 404
    @Test
    @WithMockUser(roles = "ADMIN")
    void upload_notFound() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "foto.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/recipes/99999/upload-photo").file(file))
                .andExpect(status().isNotFound());
    }

    // 🔴 5. COMMENT null → 400
    @Test
    @WithMockUser(roles = "ADMIN")
    void addComment_null() throws Exception {
        Recipe r = new Recipe();
        r.setName("Receta Comentario");
        r = recipeRepository.save(r);

        mockMvc.perform(post("/recipes/" + r.getId() + "/comments")
                .contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
    }

    // 🟢 6. CREATE OK → 201 (extra para subir coverage)
    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecipe_ok() throws Exception {
        Recipe r = new Recipe();
        r.setName("Cazuela");

        mockMvc.perform(post("/recipes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r))).andExpect(status().isCreated());
    }


    // 🔥 SEARCH con todos los parámetros
    @Test
    @WithMockUser(roles = "ADMIN")
    void search_allParams() throws Exception {
        mockMvc.perform(get("/recipes/search").param("name", "Test").param("difficulty", "Fácil")
                .param("cuisineType", "Chilena")).andExpect(status().isOk());
    }

    // 🔥 SEARCH sin parámetros
    @Test
    @WithMockUser(roles = "ADMIN")
    void search_emptyParams() throws Exception {
        mockMvc.perform(get("/recipes/search")).andExpect(status().isOk());
    }

    // 🔥 addMedia solo foto
    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void addMedia_onlyPhoto() throws Exception {
        Recipe r = recipeRepository.save(new Recipe());

        mockMvc.perform(post("/recipes/" + r.getId() + "/media").param("photoUrl", "foto.jpg"))
                .andExpect(status().isOk());
    }

    // 🔥 addMedia solo video
    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void addMedia_onlyVideo() throws Exception {
        Recipe r = recipeRepository.save(new Recipe());

        mockMvc.perform(post("/recipes/" + r.getId() + "/media").param("videoUrl", "video.mp4"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void search_withIngredients() throws Exception {
        Recipe r = new Recipe();
        r.setName("Completa");
        r.setIngredients(List.of("pan", "tomate"));
        recipeRepository.save(r);

        mockMvc.perform(get("/recipes/search").param("ingredients", "pan"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void addMedia_noParams() throws Exception {
        Recipe r = recipeRepository.save(new Recipe());

        mockMvc.perform(post("/recipes/" + r.getId() + "/media")).andExpect(status().isOk());
    }   

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_sanitizedName() throws Exception {
        mockMvc.perform(get("/recipes/search").param("name", "Cazuela!!!@@@"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadPhoto_invalidPath() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "../hack.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/recipes/99999/upload-photo").file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void getRecipe_ok() throws Exception {
        Recipe r = recipeRepository.save(new Recipe());

        mockMvc.perform(get("/recipes/private/" + r.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addComment_notFound() throws Exception {
        Comment c = new Comment();
        c.setText("Test");

        mockMvc.perform(post("/recipes/99999/comments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c))).andExpect(status().isNotFound());
    }

}
