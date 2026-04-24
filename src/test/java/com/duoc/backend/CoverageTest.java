package com.duoc.backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

class CoverageTest {

    @Test
    void testEntidadesCompletas() {
        // 1. Test de Recipe (usando tu código actual)
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Receta Test");
        recipe.setCuisineType("Chilena");
        recipe.setCountryOfOrigin("Chile");
        recipe.setDifficulty("Media");
        recipe.setInstructions("Paso 1, Paso 2");
        recipe.setCookTimeMinutes(45);
        recipe.setIngredients(new ArrayList<>(List.of("Ingrediente 1")));
        recipe.setPhotos(new ArrayList<>());
        recipe.setVideos(new ArrayList<>());
        recipe.setComments(new ArrayList<>());

        assertEquals("Receta Test", recipe.getName());
        assertEquals(45, recipe.getCookTimeMinutes());
        assertNotNull(recipe.getIngredients());

        // 2. Test de Comment (usando tu código actual)
        Comment comment = new Comment();
        comment.setId(10L);
        comment.setText("Muy buena!");
        comment.setRating(5);
        comment.setAuthor("Juan");
        comment.setRecipe(recipe);

        assertEquals(5, comment.getRating());
        assertEquals(recipe, comment.getRecipe());

        // 3. Test de User (usando tu código actual)
        User user = new User();
        user.setId(1);
        user.setNombreCompleto("Admin Test");
        user.setUsername("admin");
        user.setEmail("admin@duoc.cl");
        user.setPassword("password");
        user.setRole("ADMIN");

        assertEquals("admin", user.getUsername());
        assertEquals("ADMIN", user.getRole());
        assertNotNull(user.getAuthorities());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }
}
