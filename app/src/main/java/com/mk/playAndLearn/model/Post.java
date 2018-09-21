package com.mk.playAndLearn.model;

public class Post {
    private String userName, message, date, imageUrl;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostContent() {
        return message;
    }

    public void setPostContent(String message) {
        this.message = message;
    }

    public String getPostDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
