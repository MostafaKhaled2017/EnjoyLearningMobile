package com.mk.playAndLearn.model;

import java.util.ArrayList;

public class Challenge {
    String image, secondChallengerImage;
    String challengerName, secondChallengerName;
    String date;
    String state;
    String subject;
    String id;

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    int currentPlayer;

    public String getSecondChallengerImage() {
        return secondChallengerImage;
    }

    public void setSecondChallengerImage(String secondChallengerImage) {
        this.secondChallengerImage = secondChallengerImage;
    }

    public String getSecondChallengerName() {
        return secondChallengerName;
    }

    public void setSecondChallengerName(String secondChallengerName) {
        this.secondChallengerName = secondChallengerName;
    }

    public String getSecondChallengerUid() {

        return secondChallengerUid;
    }

    public void setSecondChallengerUid(String secondChallengerUid) {
        this.secondChallengerUid = secondChallengerUid;
    }

    String secondChallengerUid;
    String score;

    public long getSecondChallengerScore() {
        return secondChallengerScore;
    }

    public void setSecondChallengerScore(long secondChallengerScore) {
        this.secondChallengerScore = secondChallengerScore;
    }

    long secondChallengerScore;
    ArrayList questionsList = new ArrayList();

    public ArrayList getQuestionsList() {
        return questionsList;
    }

    public void setQuestionsList(ArrayList questionsList) {
        this.questionsList = questionsList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getChallengerName() {
        return challengerName;
    }

    public void setChallengerName(String challengerName) {
        this.challengerName = challengerName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
