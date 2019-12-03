package com.mk.playAndLearn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class sharedPreference {
    private static final String PREFERENCES_FILE = "MyPref";

    public static void setSharedPreference(Context context, String name, String grade, String schoolType, String userType
            , String email, String imageUrl, String lastOnlineDay, long todayChallengesNo, long points, String phoneNo, String governorate, String userId){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("currentUserName", name);
        editor.putString("grade", grade);
        editor.putString("userSchoolType", schoolType);
        editor.putString("userType", userType);
        editor.putString("userEmail", email);
        editor.putString("userImage", imageUrl);
        editor.putString("lastOnlineDay", lastOnlineDay);
        editor.putString("userId", userId);
        editor.putLong("todayChallengesNo", todayChallengesNo);
        editor.putLong("points", points);
        editor.putString("phoneNo", phoneNo);
        editor.putString("governorate", governorate);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String getSavedName(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("currentUserName", "غير معروف");
    }

    public static String getSavedSchoolType(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("userSchoolType", "غير معروف");
    }

    public static String getSavedEmail(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("userEmail", "غير معروف");
    }

    public static String getSavedId(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("userId", "غير معروف");
    }

    public static String getSavedImage(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("userImage", "غير معروف");
    }

    public static long getSavedPoints(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getLong("points", -1);
    }

    public static long getSavedTodayChallengesNo(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getLong("todayChallengesNo", -1);
    }
    public static String getSavedGrade(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("grade", "غير معروف");
    }
    public static String getSavedDate(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        return pref.getString("lastOnlineDay", "غير معروف");
    }

    public static void setSavedDate(Context context, String todayDate){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("lastOnlineDay", todayDate);
        editor.apply();
    }

    public static void setSavedTodayChallengesNo(Context context, long todayChallengesNo){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong("todayChallengesNo", todayChallengesNo);
        editor.apply();
    }

    public static void setSavedPoints(Context context, long points){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong("points", points);
        editor.apply();
    }

}
