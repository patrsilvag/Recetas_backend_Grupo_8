package com.duoc.backend;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @GetMapping
    public Iterable<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Recipe> recipe = recipeRepository.findById(id);
        return recipe.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Iterable<Recipe> searchRecipes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String countryOfOrigin,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) List<String> ingredients
    ) {
        List<Recipe> results = new java.util.ArrayList<>();
        recipeRepository.findAll().forEach(results::add);

        if (name != null && !name.isBlank()) {
            results.retainAll(recipeRepository.findByNameContainingIgnoreCase(name));
        }
        if (cuisineType != null && !cuisineType.isBlank()) {
            results.retainAll(recipeRepository.findByCuisineTypeContainingIgnoreCase(cuisineType));
        }
        if (countryOfOrigin != null && !countryOfOrigin.isBlank()) {
            results.retainAll(recipeRepository.findByCountryOfOriginContainingIgnoreCase(countryOfOrigin));
        }
        if (difficulty != null && !difficulty.isBlank()) {
            results.retainAll(recipeRepository.findByDifficultyContainingIgnoreCase(difficulty));
        }
        if (ingredients != null && !ingredients.isEmpty()) {
            // Match recipes that contain all requested ingredients (case-insensitive).
            List<String> lowerIngredients = ingredients.stream()
                    .filter(i -> i != null && !i.isBlank())
                    .map(String::toLowerCase)
                    .toList();

            results.removeIf(r -> {
                List<String> recipeIngredients = r.getIngredients().stream()
                        .filter(i -> i != null)
                        .map(String::toLowerCase)
                        .toList();
                return !recipeIngredients.containsAll(lowerIngredients);
            });
        }

        return results;
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        if (recipe.getId() != null) {
            // Avoid allowing the client to set the id manually when creating.
            recipe.setId(null);
        }
        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!recipeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        recipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
