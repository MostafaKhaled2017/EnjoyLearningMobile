package com.mk.playAndLearn.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


//TODO : note that general sign activity and main activity use other variables
public class Firebase {
    public static FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    public static CollectionReference fireStoreLessons = fireStore.collection("lessons");
    public static CollectionReference fireStoreQuestions = fireStore.collection("questions");
    public static CollectionReference fireStoreUsers = fireStore.collection("users");
    public static CollectionReference fireStorePosts = fireStore.collection("posts");
    public static CollectionReference fireStoreComments = fireStore.collection("comments");
    public static CollectionReference fireStoreReplies = fireStore.collection("replies");
    public static CollectionReference fireStoreChallenges = fireStore.collection("challenges");
    public static CollectionReference fireStoreGeneralChallenge = fireStore.collection("generalChallenge");
    public static CollectionReference fireStoreComplaintsQuestions = fireStore.collection("complaintsQuestions");

}
