package com.duoc.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.duoc.backend.Recipe;
import com.duoc.backend.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class RecipeRepositoryTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Test
    void testFindById() {
        // Preparar: Simulamos una receta
        Recipe mockRecipe = new Recipe();
        mockRecipe.setId(1L);
        mockRecipe.setName("Empanadas");

        // Le decimos al simulador qué responder
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(mockRecipe));

        // Ejecutar
        Optional<Recipe> resultado = recipeRepository.findById(1L);

        // Validar
        assertTrue(resultado.isPresent());
        assertEquals("Empanadas", resultado.get().getName());
    }
}
