package com.mk.playAndLearn.utils;

import android.content.Context;
import androidx.multidex.MultiDex;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp  extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //FirebaseDatabase.getInstance().getReference("currentTerm").keepSynced(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
