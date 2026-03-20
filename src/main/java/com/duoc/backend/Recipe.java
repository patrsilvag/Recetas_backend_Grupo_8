package com.duoc.backend;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Represents a cooking recipe with metadata, ingredients and optional photos.
 */
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    /** Tipo de cocina (e.g. italiana, mexicana, japonesa, etc.). */
    private String cuisineType;

    /** País de origen de la receta. */
    private String countryOfOrigin;

    /** Dificultad de la receta (e.g. fácil, media, difícil). */
    private String difficulty;

    /** Instrucciones de preparación. */
    private String instructions;

    /** Tiempo de cocción en minutos. */
    private Integer cookTimeMinutes;

    /** Lista de ingredientes. */
    @ElementCollection
    @CollectionTable(name = "recipe_ingredients")
    private List<String> ingredients = new ArrayList<>();

    /** URLs o identificadores de fotos que facilitan la comprensión de la receta. */
    @ElementCollection
    @CollectionTable(name = "recipe_photos")
    private List<String> photos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}
