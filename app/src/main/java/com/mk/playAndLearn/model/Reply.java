package com.mk.playAndLearn.model;

public class Reply {
    String userName;
    String userEmail;
    String userImage;
    String content;
    String date;
    String replyId;
    String writerUid;
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
    long votes;
    boolean posted;

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    public String getWriterUid() {
        return writerUid;
    }

    public void setWriterUid(String writerUid) {
        this.writerUid = writerUid;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    String postId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
