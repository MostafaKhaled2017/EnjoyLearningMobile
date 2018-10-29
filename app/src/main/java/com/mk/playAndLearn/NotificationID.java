package com.mk.playAndLearn;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(1);
    public static int getID() {
        return c.incrementAndGet();
    }
}
