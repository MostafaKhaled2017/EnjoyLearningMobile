package com.mk.playAndLearn.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;
import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.usersReference;

//TODO : note that general sign activity and main activity use other variables
public class Strings {
    public static String refusedChallengeText = "تم الرفض";
    public static String waitingChallengeText = "قيد الإنتظار";
    public static String yourTurnChallengeText = "دورك";
    public static String uncompletedChallengeText = "لم يكتمل";
    public static String completedChallengeText = "اكتمل";
    public static String wonChallengeText = "ربحت";
    public static String loseChallengeText = "خسرت";
    public static String drawChallengeText = "تعادلت";
    public static String adminEmail = "mostafakhaled835@gmail.com";
}
