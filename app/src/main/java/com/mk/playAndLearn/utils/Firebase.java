package com.mk.playAndLearn.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Firebase {
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference usersReference = database.getReference("users");

}
