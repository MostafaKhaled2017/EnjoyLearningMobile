package com.mk.playAndLearn.model;

import android.support.annotation.NonNull;

import java.util.Date;

public class Challenge implements Comparable<Challenge>{
    String image, secondChallengerImage;
    String opponentName;
    String opponentImage;
    String player1Uid;
    Date timestamp;
    String grade;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlayer1Uid() {
        return player1Uid;
    }

    public void setPlayer1Uid(String player1Uid) {
        this.player1Uid = player1Uid;
    }

    public String getPlayer2Uid() {
        return player2Uid;
    }

    public void setPlayer2Uid(String player2Uid) {
        this.player2Uid = player2Uid;
    }

    String player2Uid;
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

    public String getOpponentImage() {
        return opponentImage;
    }

    public void setOpponentImage(String opponentImage) {
        this.opponentImage = opponentImage;
    }

    public String getOpponentUid() {

        return opponentUid;
    }

    public void setOpponentUid(String secondChallengerUid) {
        this.opponentUid = secondChallengerUid;
    }

    String opponentUid;
    String score;
    long player1Score, player2Score;

    public long getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(long player1Score) {
        this.player1Score = player1Score;
    }

    public long getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(long player2Score) {
        this.player2Score = player2Score;
    }

    public long getSecondChallengerScore() {
        return secondChallengerScore;
    }

    public void setSecondChallengerScore(long secondChallengerScore) {
        this.secondChallengerScore = secondChallengerScore;
    }

    long secondChallengerScore;
    String questionsList;

    public String getQuestionsList() {
        return questionsList;
    }

    public void setQuestionsList(String questionsList) {
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

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
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

    @Override
    public int compareTo(@NonNull Challenge challenge) {
        if (getTimestamp() == null || challenge.getTimestamp() == null)
            return 0;

        return getTimestamp().compareTo(challenge.getTimestamp());
    }


   /* @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null && obj instanceof Challenge) {
            isEqual = this.id.equals(((Challenge) obj).getId());
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }*/
}
