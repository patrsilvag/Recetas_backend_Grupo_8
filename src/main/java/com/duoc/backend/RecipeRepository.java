package com.duoc.backend;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    List<Recipe> findByNameContainingIgnoreCase(String name);

    List<Recipe> findByCuisineTypeContainingIgnoreCase(String cuisineType);

    List<Recipe> findByCountryOfOriginContainingIgnoreCase(String countryOfOrigin);

    List<Recipe> findByDifficultyContainingIgnoreCase(String difficulty);

    List<Recipe> findByIngredientsContaining(String ingredient);
}
