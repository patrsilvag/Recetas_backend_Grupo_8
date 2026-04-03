package com.duoc.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("")
    public Iterable<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Recipe> recipe = recipeRepository.findById(id);
        return recipe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Iterable<Recipe> searchRecipes(@RequestParam(required = false) String name,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String countryOfOrigin,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) List<String> ingredients) {
        // Corrección SonarQube: Uso de ArrayList importado en lugar de java.util.ArrayList
        List<Recipe> results = new ArrayList<>();
        recipeRepository.findAll().forEach(results::add);

        // 🛡️ SANITIZACIÓN ACTIVA (A03)
        // Corrección SonarQube (Regla S1226): No reasignar parámetros del método. Se usan variables
        // nuevas.
        if (name != null && !name.isBlank()) {
            String sanitizedName = name.replaceAll("[^a-zA-Z0-9 ]", "");
            results.retainAll(recipeRepository.findByNameContainingIgnoreCase(sanitizedName));
        }
        if (cuisineType != null && !cuisineType.isBlank()) {
            String sanitizedCuisine = cuisineType.replaceAll("[^a-zA-Z0-9 ]", "");
            results.retainAll(
                    recipeRepository.findByCuisineTypeContainingIgnoreCase(sanitizedCuisine));
        }
        if (countryOfOrigin != null && !countryOfOrigin.isBlank()) {
            results.retainAll(
                    recipeRepository.findByCountryOfOriginContainingIgnoreCase(countryOfOrigin));
        }
        if (difficulty != null && !difficulty.isBlank()) {
            results.retainAll(recipeRepository.findByDifficultyContainingIgnoreCase(difficulty));
        }
        if (ingredients != null && !ingredients.isEmpty()) {
            List<String> lowerIngredients = ingredients.stream()
                    .filter(i -> i != null && !i.isBlank()).map(String::toLowerCase).toList();

            results.removeIf(r -> {
                List<String> recipeIngredients = r.getIngredients().stream().filter(i -> i != null)
                        .map(String::toLowerCase).toList();
                return !recipeIngredients.containsAll(lowerIngredients);
            });
        }

        return results;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // <--- AMBOS PUEDEN CREAR
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        if (recipe.getId() != null) {
            recipe.setId(null);
        }
        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')") // <--- SOLO ADMIN PUEDE BORRAR
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
