package com.mk.playAndLearn.model;

import java.io.Serializable;

public class Lesson implements Serializable {
    String content;
    String title;
    String position;
    String arabicPosition;
    String subject;
    String writerName;
    String writerEmail;
    String writerUid;
    String lessonId;

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    long unitNo, lessonNo;

    public long getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(long unitNo) {
        this.unitNo = unitNo;
    }

    public long getLessonNo() {
        return lessonNo;
    }

    public void setLessonNo(long lessonNo) {
        this.lessonNo = lessonNo;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getArabicPosition() {
        return arabicPosition;
    }

    public void setArabicPosition(String arabicPosition) {
        this.arabicPosition = arabicPosition;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
