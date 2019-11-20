package com.mk.playAndLearn.activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_welcome); TODO
        final Intent i = new Intent(this, GeneralSignActivity.class);
        //TODO : think about using that the data is stored with the app to make it able to works offline

        //TODO : save the selected language in shared preference
     /*   Configuration mainConfig = new Configuration(getResources().getConfiguration());
        String languageToLoad = "ar_EG";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        mainConfig.setLocale(locale);
        getResources().updateConfiguration(mainConfig, null);*/

        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(0); //TODO :Edit this
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(i);
                    finish();
                }
            }
        };

        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
    }
}

