package com.mk.playAndLearn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class sharedPreference {
    public static void setSharedPreference(Context context, String name, String grade, String schoolType, String userType
            , String email, String imageUrl, String lastOnlineDay, long todayChallengesNo){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("currentUserName", name);
        editor.putString("grade", grade);
        editor.putString("userSchoolType", schoolType);
        editor.putString("userType", userType);
        editor.putString("userEmail", email);
        editor.putString("userImage", imageUrl);
        editor.putString("lastOnlineDay", lastOnlineDay);
        editor.putLong("todayChallengesNo", todayChallengesNo);
        editor.apply();
    }

    public static String getSavedName(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        return pref.getString("currentUserName", "غير معروف");
    }

    public static long getSavedTodayChallengesNo(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        return pref.getLong("todayChallengesNo", -1);
    }
    public static String getSavedGrade(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        return pref.getString("grade", "غير معروف");
    }
    public static long getSavedPoints(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        return pref.getLong("points", -1);
    }
    public static String getSavedDate(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        return pref.getString("lastOnlineDay", "غير معروف");
    }

    public static void setSavedDate(Context context, String todayDate){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("lastOnlineDay", todayDate);
        editor.apply();
    }

    public static void setSavedTodayChallengesNo(Context context, long todayChallengesNo){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong("todayChallengesNo", todayChallengesNo);
        editor.apply();
    }

}
