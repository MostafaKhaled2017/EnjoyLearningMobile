package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChallengeResultActivity extends AppCompatActivity {
    int score;
    TextView challengeResultTv;
    FirebaseDatabase database;
    DatabaseReference ref;
    SharedPreferences sharedPreferences;
    String userName = "", userImage = "", userEmail = "", subject;
    ArrayList questionsList = new ArrayList(), playerAnswersBooleansList = new ArrayList(), playerAnswersList = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        
        challengeResultTv = findViewById(R.id.challengeResultText);

        sharedPreferences = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if(sharedPreferences != null) {
            if (sharedPreferences.contains("userName")){
                userName = sharedPreferences.getString("userName", "");
            }
            if (sharedPreferences.contains("userImage")){
                userImage = sharedPreferences.getString("userImage", "");
            }
            if (sharedPreferences.contains("userEmail")){
                userEmail = sharedPreferences.getString("userEmail", "");
            }
        }

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("challenges");

        Intent intent = getIntent();
        if(intent != null){
            score = intent.getIntExtra("score", -1);
            subject = intent.getStringExtra("subject");
            questionsList = intent.getParcelableArrayListExtra("questionsList");
            playerAnswersBooleansList = intent.getParcelableArrayListExtra("player1AnswersBooleans");
            playerAnswersList = intent.getParcelableArrayListExtra("player1Answers");
        }
        challengeResultTv.append(score +"");
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());//TODO : check that the date changes at 12 p.m exactly
        String date = format.format(today);

        Map<String, Object> map = new HashMap<>();
        map.put("player1Name", userName);
        map.put("player1Email", userEmail);
        map.put("player1Image", userImage);
        map.put("player1score", score);
        map.put("date", date);
        map.put("subject", subject);
        map.put("questionsList", questionsList);
        map.put("player1AnswersBooleans", playerAnswersBooleansList);
        map.put("player1Answers", playerAnswersList);
        map.put("state", "اكتمل"); // TODO : edit this

        ref.push().setValue(map);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
