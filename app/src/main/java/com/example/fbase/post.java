package com.example.fbase;

public class post {
    private String Title;
    private String Description;
    private String ImageUrl;
    private String userName;


    public post(){}

    public post(String title, String description, String imageUrl,String user) {
        Title = title;
        Description = description;
        ImageUrl = imageUrl;
        userName=user;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
