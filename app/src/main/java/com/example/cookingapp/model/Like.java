package com.example.cookingapp.model;

public class Like {
    private String userId;
    private String recipeId;

    public Like() {
    }

    public Like(String userId, String recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}
