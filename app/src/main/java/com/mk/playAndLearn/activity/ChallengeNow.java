package com.mk.playAndLearn.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mk.enjoylearning.R;

public class ChallengeNow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_now);
    }

    public void finishch(View view) {

        finish();
    }
}
