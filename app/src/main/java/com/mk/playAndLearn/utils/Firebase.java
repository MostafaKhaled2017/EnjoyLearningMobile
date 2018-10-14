package com.mk.playAndLearn.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class Firebase {
    public static FirebaseAuth auth = FirebaseAuth.getInstance();
    public static FirebaseUser currentUser = auth.getCurrentUser();
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference usersReference = database.getReference("users");
    public static DatabaseReference lessonsReference = database.getReference("lessons");
    public static DatabaseReference questionsReference =database.getReference("questions");
    public static DatabaseReference commentsReference =database.getReference("comments");
    public static DatabaseReference  postsReference = database.getReference("posts");
    public static DatabaseReference  challengesReference = database.getReference("challenges");
}
