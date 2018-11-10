package com.mk.playAndLearn.utils;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class DateClass {
    @ServerTimestamp
    private Date date;

   public DateClass() {}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
