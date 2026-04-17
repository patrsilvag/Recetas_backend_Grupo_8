package com.duoc.backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    @Test
    void testRecipeGettersAndSetters() {
        // Preparar
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Cazuela de Ave");
        recipe.setInstructions("Hervir el pollo con las verduras por 40 minutos."); // Usamos tu
                                                                                    // campo real

        // Ejecutar y Validar
        assertEquals(1L, recipe.getId());
        assertEquals("Cazuela de Ave", recipe.getName());
        assertEquals("Hervir el pollo con las verduras por 40 minutos.", recipe.getInstructions());
    }
}
