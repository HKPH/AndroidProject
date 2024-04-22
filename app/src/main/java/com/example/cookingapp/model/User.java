package com.example.cookingapp.model;

public class User {
    private String name;
    private String email;
    private String phone;
    private String photo;
    private boolean checkAuth;

    private String fcmToken;

    // Constructors
    public User() {
    }

    public User(String email) {
        this.email = email;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isCheckAuth() {
        return checkAuth;
    }

    public void setCheckAuth(boolean checkAuth) {
        this.checkAuth = checkAuth;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
