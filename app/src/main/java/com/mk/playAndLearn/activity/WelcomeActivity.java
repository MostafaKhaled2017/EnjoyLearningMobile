package com.mk.playAndLearn.activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_welcome); TODO
        final Intent i = new Intent(this, GeneralSignActivity.class);
        //TODO : think about using that the data is stored with the app to make it able to works offline

        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
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

