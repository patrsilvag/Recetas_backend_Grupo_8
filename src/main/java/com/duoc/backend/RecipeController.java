package com.duoc.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    // 🔹 Agregamos el repositorio de comentarios
    @Autowired
    private CommentRepository commentRepository;

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

        List<Recipe> results = new ArrayList<>();
        recipeRepository.findAll().forEach(results::add);

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
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        if (recipe.getId() != null) {
            recipe.setId(null);
        }
        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") Long id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        if (!recipeRepository.existsById(id))
            return ResponseEntity.notFound().build();
        recipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/media")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> addMedia(@PathVariable("id") Long id,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String videoUrl) {

        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        if (optionalRecipe.isEmpty())
            return ResponseEntity.notFound().build();

        Recipe recipe = optionalRecipe.get();
        if (photoUrl != null && !photoUrl.isBlank())
            recipe.getPhotos().add(photoUrl);
        if (videoUrl != null && !videoUrl.isBlank())
            recipe.getVideos().add(videoUrl);

        recipeRepository.save(recipe);
        return ResponseEntity.ok(recipe);
    }

    @PostMapping("/{id}/upload-photo")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> uploadPhoto(@PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {

        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        if (optionalRecipe.isEmpty())
            return ResponseEntity.notFound().build();
        if (file.isEmpty())
            return ResponseEntity.badRequest().build();

        try {
            String folder = "uploads/";
            String fileName = System.currentTimeMillis() + "_"
                    + file.getOriginalFilename().replaceAll(" ", "_");
            Path path = Paths.get(folder + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            String localUrl = "http://localhost:8081/uploads/" + fileName;
            Recipe recipe = optionalRecipe.get();
            recipe.getPhotos().add(localUrl);
            recipeRepository.save(recipe);

            return ResponseEntity.ok(recipe);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =================================================================================
    // 💬 NUEVO ENDPOINT: COMENTAR Y VALORAR (Semana 5)
    // =================================================================================
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> addComment(@PathVariable("id") Long id,
            @RequestBody Comment comment) {
        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        if (optionalRecipe.isEmpty())
            return ResponseEntity.notFound().build();

        Recipe recipe = optionalRecipe.get();

        // Establecemos la relación bidireccional
        comment.setRecipe(recipe);
        commentRepository.save(comment); // Guardamos el comentario

        // Retornamos la receta actualizada (que ya incluirá el comentario por el mappedBy)
        return ResponseEntity.ok(recipe);
    }
}
