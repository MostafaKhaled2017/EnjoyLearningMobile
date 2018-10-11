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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserImage;
import static com.mk.playAndLearn.utils.Strings.currentUserName;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class ChallengeResultActivity extends AppCompatActivity {
    //TODO : think about removing challenge result activity but think well before determine what to do in this
    //TODO : handle loosing internet connection before uploading data for example show a dialog when try to go out.
    TextView challengeResultTv;

    String subject, challengeId;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    int score, currentChallenger;

    ArrayList questionsList = new ArrayList(), playerAnswersBooleansList = new ArrayList(), playerAnswersList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        
        challengeResultTv = findViewById(R.id.challengeResultText);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            currentChallenger = intent.getIntExtra("currentChallenger", currentChallenger);
            score = intent.getIntExtra("score", -1);
            subject = intent.getStringExtra("subject");
            playerAnswersBooleansList = intent.getParcelableArrayListExtra("currentPlayerAnswersBooleans");
            playerAnswersList = intent.getParcelableArrayListExtra("currentPlayerAnswers");


            if(currentChallenger == 1){
                secondPlayerName = intent.getStringExtra("player2Name");
                secondPlayerEmail= intent.getStringExtra("player2Email");
                secondPlayerImage = intent.getStringExtra("player2Image");
                secondPlayerUid = intent.getStringExtra("player2Uid");
                secondPlayerPoints = intent.getIntExtra("player2Points", -1);
                questionsList = intent.getParcelableArrayListExtra("questionsList");
            }
            else if(currentChallenger == 2){
                challengeId = intent.getStringExtra("challengeId");
            }
        }
        challengeResultTv.append(score +"");
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());//TODO : check that the date changes at 12 p.m exactly
        String date = format.format(today);

        Map<String, Object> map = new HashMap<>();
        if(currentChallenger == 1) {
            map.put("player1Name", currentUserName);
            map.put("player1Email", currentUserEmail);
            map.put("player1Image", currentUserImage);
            map.put("player1score", score);
            map.put("player1Uid", currentUserUid);
            map.put("player2Name", secondPlayerName);
            map.put("player2Email", secondPlayerEmail);
            map.put("player2Image", secondPlayerImage);
            map.put("player2Uid", secondPlayerUid);
            map.put("player2score", 0);
            map.put("date", date);
            map.put("subject", subject);
            map.put("questionsList", questionsList);

            map.put("player1AnswersBooleans", playerAnswersBooleansList);
            map.put("player1Answers", playerAnswersList);
            map.put("state", "لم يكتمل"); // TODO : edit this

            challengesReference.push().setValue(map);
        }
        else if(currentChallenger == 2){
            challengesReference.child(challengeId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        challengesReference.child(challengeId).child("player2score").setValue(score);
                        challengesReference.child(challengeId).child("player2AnswersBooleans").setValue(playerAnswersBooleansList);
                        challengesReference.child(challengeId).child("player2Answers").setValue(playerAnswersList);
                        challengesReference.child(challengeId).child("state").setValue("اكتمل");
                    }
                    else {
                        challengesReference.child(challengeId).removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

}
