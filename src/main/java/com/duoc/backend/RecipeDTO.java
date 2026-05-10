package com.duoc.backend;

import java.util.List;

/**
 * DTO para la creación de recetas. Se usa para evitar el Mass Assignment y cumplir con los
 * estándares de seguridad de SonarQube.
 */
public record RecipeDTO(String name, String cuisineType, String countryOfOrigin, String difficulty,
        String instructions, // Coincide con tu Recipe.java
        Integer cookTimeMinutes, // Coincide con tu Recipe.java
        List<String> ingredients) {
}
