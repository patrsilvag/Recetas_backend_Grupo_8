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

    // 🛡️ Constante para evitar "String Literal Duplication" y facilitar mantenimiento
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private RecipeRepository recipeRepository;

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
        if (recipe == null)
            return ResponseEntity.badRequest().build();
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

        // 1. Validar existencia de receta
        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        if (optionalRecipe.isEmpty())
            return ResponseEntity.notFound().build();

        // 2. Validar que el archivo no sea nulo ni vacío (Bug Fix Sonar)
        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest().build();

        try {
            // 3. Obtener y sanitizar nombre de archivo de forma segura
            String originalName = file.getOriginalFilename();
            String sanitizedName = (originalName != null && !originalName.isBlank())
                    ? originalName.replaceAll("[^a-zA-Z0-9.-]", "_")
                    : "file_" + System.currentTimeMillis();

            String fileName = System.currentTimeMillis() + "_" + sanitizedName;

            // 4. Resolver ruta de forma segura contra Path Traversal
            Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Path path = root.resolve(fileName).normalize();

            // Seguridad: Verificar que el archivo resultante sigue dentro de la carpeta uploads
            if (!path.startsWith(root)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // 5. Guardar URL local en la receta
            String localUrl = "http://localhost:8081/" + UPLOAD_DIR + fileName;
            Recipe recipe = optionalRecipe.get();
            recipe.getPhotos().add(localUrl);
            recipeRepository.save(recipe);

            return ResponseEntity.ok(recipe);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> addComment(@PathVariable("id") Long id,
            @RequestBody Comment comment) {

        if (comment == null)
            return ResponseEntity.badRequest().build();

        Optional<Recipe> optionalRecipe = recipeRepository.findById(id);
        if (optionalRecipe.isEmpty())
            return ResponseEntity.notFound().build();

        Recipe recipe = optionalRecipe.get();

        // Relación bidireccional y persistencia
        comment.setRecipe(recipe);
        commentRepository.save(comment);

        return ResponseEntity.ok(recipe);
    }
}
