package com.mk.playAndLearn.utils;

import com.google.firebase.auth.FirebaseAuth;

import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.currentUser;

////TODO : add other strings here
public class Strings {
    public static String refusedChallengeText = "تم الرفض";
    public static String waitingChallengeText = "قيد الإنتظار";
    public static String yourTurnChallengeText = "دورك";
    public static String uncompletedChallengeText = "لم يكتمل";
    public static String completedChallengeText = "اكتمل";
    public static String currentUserName = currentUser.getDisplayName();
    public static String currentUserImage = currentUser.getPhotoUrl().toString();
    public static String currentUserEmail = currentUser.getEmail();
    public static String currentUserUid = currentUser.getUid();


}
