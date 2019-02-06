package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mk.enjoylearning.R;

public class DailyRewardsActivity extends AppCompatActivity {
    long consecutiveDays = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_rewards);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            consecutiveDays = intent.getLongExtra("consecutiveDays", -1);
        }
        Toast.makeText(this, "consecutiveDays : " + consecutiveDays, Toast.LENGTH_SHORT).show();
    }
}
