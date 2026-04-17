package com.duoc.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeController recipeController;

    @Test
    void testGetAllRecipes() {
        // Preparar: Simulamos una lista vacía de recetas
        List<Recipe> listaVacia = new ArrayList<>();
        when(recipeRepository.findAll()).thenReturn(listaVacia);

        // Ejecutar
        Iterable<Recipe> resultado = recipeController.getAllRecipes();

        // Validar
        assertNotNull(resultado);
    }
}
