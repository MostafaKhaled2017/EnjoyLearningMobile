package com.mk.playAndLearn.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class sharedPreference {

    public static String getNameFromSharedPreferences(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        String currentUserName = pref.getString("currentUserName", "غير معروف");
        return currentUserName;
    }
    public static String getGradeFromSharedPreferences(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        String grade = pref.getString("grade", "غير معروف");
        return grade;
    }
}
