package com.example.cookingapp.model;

import java.io.Serializable;

public class Rating implements Serializable {
    private String id;
    private String userId;
    private String review;
    private String recipeId;


    public Rating() {
        // Constructor mặc định
    }

    // Constructor và các phương thức getter và setter khác

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String productId) {
        this.recipeId = productId;
    }

}
