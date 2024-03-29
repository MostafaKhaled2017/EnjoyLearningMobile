package com.mk.playAndLearn.model;

public class User {
    String name;
    String email;
    String imageUrl;
    String Uid;

    int position = -1;
    int points = -1;

    public int getAcceptedQuestions() {
        return acceptedQuestions;
    }

    public void setAcceptedQuestions(int acceptedQuestions) {
        this.acceptedQuestions = acceptedQuestions;
    }

    int acceptedQuestions = -1;
    boolean admin = false;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    boolean online = false;

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }
}
