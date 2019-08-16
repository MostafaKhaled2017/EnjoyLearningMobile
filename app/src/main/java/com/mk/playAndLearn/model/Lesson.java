package com.mk.playAndLearn.model;

import java.io.Serializable;

public class Lesson implements Serializable {
    String content;
    String title;
    String date;
    String grade;
    String subject;
    String writerName;
    String writerEmail;
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getWriterEmail() {
        return writerEmail;
    }

    public void setWriterEmail(String writerEmail) {
        this.writerEmail = writerEmail;
    }

    public String getWriterUid() {
        return writerUid;
    }

    public void setWriterUid(String writerUid) {
        this.writerUid = writerUid;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getSchoolType() {
        return schoolType;
    }

    public void setSchoolType(String schoolType) {
        this.schoolType = schoolType;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public String getLessonNo() {
        return lessonNo;
    }

    public void setLessonNo(String lessonNo) {
        this.lessonNo = lessonNo;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    String writerUid;
    String lessonId;
    String schoolType;

    String unitNo, lessonNo;
    long term;

    boolean reviewed;

}
