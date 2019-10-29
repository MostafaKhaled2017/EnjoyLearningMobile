package com.mk.playAndLearn.model;

public class Post {
    private String writerName;
    private boolean posted;
    String subject;
    String upVotedUsers;

    public String getUpVotedUsers() {
        return upVotedUsers;
    }

    public void setUpVotedUsers(String upVotedUsers) {
        this.upVotedUsers = upVotedUsers;
    }

    public String getDownVotedUsers() {
        return downVotedUsers;
    }

    public void setDownVotedUsers(String downVotedUsers) {
        this.downVotedUsers = downVotedUsers;
    }

    String downVotedUsers;

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    long votes;

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWriterUid() {

        return writerUid;
    }

    public void setWriterUid(String writerUid) {
        this.writerUid = writerUid;
    }

    private String writerUid;
    private String content;
    private String date;
    private String imageUrl;
    String id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    public String getWriter() {
        return writerName;
    }

    public void setWriter(String writerName) {
        this.writerName = writerName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String message) {
        this.content = message;
    }


    public String getImage() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
