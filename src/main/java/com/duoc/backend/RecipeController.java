package com.duoc.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private static final String UPLOAD_DIR = "uploads/";

    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;

    public RecipeController(RecipeRepository recipeRepository,
            CommentRepository commentRepository) {
        this.recipeRepository = recipeRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("")
    public Iterable<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return recipeRepository.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
            results.retainAll(recipeRepository
                    .findByNameContainingIgnoreCase(name.replaceAll("[^a-zA-Z0-9 ]", "")));
        }
        if (cuisineType != null && !cuisineType.isBlank()) {
            results.retainAll(recipeRepository.findByCuisineTypeContainingIgnoreCase(
                    cuisineType.replaceAll("[^a-zA-Z0-9 ]", "")));
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
    public ResponseEntity<Recipe> createRecipe(@RequestBody RecipeDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }

        Recipe recipe = new Recipe();
        recipe.setName(dto.name());
        recipe.setCuisineType(dto.cuisineType());
        recipe.setCountryOfOrigin(dto.countryOfOrigin());
        recipe.setDifficulty(dto.difficulty());
        recipe.setInstructions(dto.instructions());
        recipe.setCookTimeMinutes(dto.cookTimeMinutes());
        recipe.setIngredients(dto.ingredients());

        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("id") @NonNull Long id) {
        if (!recipeRepository.existsById(id))
            return ResponseEntity.notFound().build();
        recipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/media")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> addMedia(@PathVariable("id") @NonNull Long id,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String videoUrl) {

        return recipeRepository.findById(id).map(recipe -> {
            if (photoUrl != null && !photoUrl.isBlank()) {
                recipe.getPhotos().add(photoUrl);
            }
            if (videoUrl != null && !videoUrl.isBlank()) {
                recipe.getVideos().add(videoUrl);
            }
            Recipe saved = recipeRepository.save(recipe);
            // ✅ CORRECCIÓN: Objects.requireNonNull garantiza al compilador que 'saved' no es null
            return ResponseEntity.ok(Objects.requireNonNull(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/upload-photo")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> uploadPhoto(@PathVariable("id") @NonNull Long id,
            @RequestParam("file") MultipartFile file) {

        return recipeRepository.findById(id).map(recipe -> {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().<Recipe>build();
            }

            try {
                String originalName = file.getOriginalFilename();
                String sanitizedName = (originalName != null && !originalName.isBlank())
                        ? originalName.replaceAll("[^a-zA-Z0-9.-]", "_")
                        : "file_" + System.currentTimeMillis();

                String fileName = System.currentTimeMillis() + "_" + sanitizedName;
                Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
                Path path = root.resolve(fileName).normalize();

                if (!path.startsWith(root)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).<Recipe>build();
                }

                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());

                recipe.getPhotos().add("http://localhost:8081/" + UPLOAD_DIR + fileName);
                Recipe saved = recipeRepository.save(recipe);
                // ✅ CORRECCIÓN: Garantía de no-nulidad para cumplir con @NonNull
                return ResponseEntity.ok(Objects.requireNonNull(saved));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Recipe>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

        
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Recipe> addComment(@PathVariable("id") @NonNull Long id,
            @RequestBody CommentDTO commentDto) {

        return recipeRepository.findById(id).map(recipe -> {
            Comment comment = new Comment();
            comment.setText(commentDto.text());
            comment.setAuthor(commentDto.author());
            comment.setRecipe(recipe);
            commentRepository.save(comment);
            return ResponseEntity.ok(recipe);
        }).orElse(ResponseEntity.notFound().build());
    }
}
