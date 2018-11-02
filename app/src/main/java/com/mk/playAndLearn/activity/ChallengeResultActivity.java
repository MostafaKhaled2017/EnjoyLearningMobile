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
import android.util.Log;
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
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Integers.generalChallengeScoreMultiply;

public class ChallengeResultActivity extends AppCompatActivity {
    //TODO : think about removing challenge result activity but think well before determine what to do in this
    //TODO : handle loosing internet connection before uploading data for example show a dialog when try to go out.
    TextView challengeResultTv;

    String subject, challengeId, currentUserName;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    int score, currentChallenger;

    ArrayList questionsList = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "";
    String localCurrentUserUid;
    boolean isGeneralChallenge;

    public SharedPreferences pref; // 0 - for private mode


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

        localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        currentUserName = pref.getString("currentUserName", "غير معروف");
        Log.v("sharedPreference", " current userName is : " + currentUserName);


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            score = intent.getIntExtra("score", -1);
            isGeneralChallenge = intent.getBooleanExtra("isGeneralChallenge", false);
            if (!isGeneralChallenge) {
                currentChallenger = intent.getIntExtra("currentChallenger", currentChallenger);
                subject = intent.getStringExtra("subject");
                playerAnswersBooleansList = intent.getStringExtra("currentPlayerAnswersBooleans");
                playerAnswersList = intent.getStringExtra("currentPlayerAnswers");
            }

            if (currentChallenger == 1) {
                secondPlayerName = intent.getStringExtra("player2Name");
                secondPlayerEmail = intent.getStringExtra("player2Email");
                secondPlayerImage = intent.getStringExtra("player2Image");
                secondPlayerUid = intent.getStringExtra("player2Uid");
                secondPlayerPoints = intent.getIntExtra("player2Points", -1);
                questionsList = intent.getParcelableArrayListExtra("questionsList");
            } else if (currentChallenger == 2) {
                challengeId = intent.getStringExtra("challengeId");
            }
        }
        Date today = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        formatDate.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        formatTime.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        String date = formatDate.format(today);
        String time = formatTime.format(today);

        if (!isGeneralChallenge) {
            challengeResultTv.append(score + "");
            Map<String, Object> map = new HashMap<>();
            if (currentChallenger == 1) {
                String localCurrentUserImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                map.put("player1Name", currentUserName);
                map.put("player1Email", localCurrentUserEmail);
                map.put("player1Image", localCurrentUserImage);
                map.put("player1score", score);
                map.put("player1Uid", localCurrentUserUid);
                map.put("player2Name", secondPlayerName);
                map.put("player2Email", secondPlayerEmail);
                map.put("player2Image", secondPlayerImage);
                map.put("player2Uid", secondPlayerUid);
                map.put("player2score", 0);
                map.put("player1notified", localCurrentUserUid + "false");
                map.put("player2notified", secondPlayerUid + "false");
                map.put("date", date);
                map.put("time", time);//TODO : note that old challenges doesn't have time
                map.put("subject", subject);
                map.put("questionsList", questionsList);
                map.put("player1AnswersBooleans", playerAnswersBooleansList.trim());
                map.put("player1Answers", playerAnswersList);
                map.put("state", "لم يكتمل"); // TODO : edit this

                challengesReference.push().setValue(map);
            } else if (currentChallenger == 2) {
                challengesReference.child(challengeId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            challengesReference.child(challengeId).child("player2score").setValue(score);
                            challengesReference.child(challengeId).child("player2AnswersBooleans").setValue(playerAnswersBooleansList.trim());
                            challengesReference.child(challengeId).child("player2Answers").setValue(playerAnswersList);
                            challengesReference.child(challengeId).child("state").setValue("اكتمل");

                            addPoints(dataSnapshot);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
        else {
            usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int lastGeneralChallengePoints = Integer.parseInt(dataSnapshot.child("lastGeneralChallengeScore").getValue().toString());
                    int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                    int finalChallengePoints =  score * generalChallengeScoreMultiply;
                    challengeResultTv.setText("نتيجة التحدى : " + finalChallengePoints + " نقطة");
                    if(lastGeneralChallengePoints == 0) {
                        usersReference.child(currentUser.getUid()).child("lastGeneralChallengeScore").setValue(finalChallengePoints);
                        usersReference.child(currentUser.getUid()).child("points").setValue(userPoints + finalChallengePoints);
                    }
                    else {
                        Toast.makeText(ChallengeResultActivity.this, "لقد قمت بالمشاركة فى هذا التحدى من قبل ولن يتم احتساب نقاطك الحالية", Toast.LENGTH_SHORT).show();
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

    void addPoints(DataSnapshot dataSnapshot) {
        final String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
        final String player2Uid = dataSnapshot.child("player2Uid").getValue().toString();

        long player1Score = (long) dataSnapshot.child("player1score").getValue();
        long player2Score = (long) score;

        final DatabaseReference player1Reference = usersReference.child(player1Uid);
        DatabaseReference player2Reference = usersReference.child(player2Uid);

        if (getCurrentPlayer(player1Uid) == 2) {

            if (player1Score == player2Score) {
                player1Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v("pointsDebug", "onDataChanged1");
                        long points = (long) dataSnapshot.child("points").getValue();
                        usersReference.child(player1Uid).child("points").setValue(points + 1);

                        // usersReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                player2Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v("pointsDebug", "onDataChanged2");
                        long points = (long) dataSnapshot.child("points").getValue();
                        usersReference.child(player2Uid).child("points").setValue(points + 1);

                        //usersReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                if (player1Score > player2Score) {
                    Log.v("debugPoints3", "listener1 called");
                    player1Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long points = (long) dataSnapshot.child("points").getValue();
                            usersReference.child(player1Uid).child("points").setValue(points + 3);

                            // usersReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Log.v("debugPoints3", "listener1 called");
                    player2Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long points = (long) dataSnapshot.child("points").getValue();
                            usersReference.child(player2Uid).child("points").setValue(points + 3);

                            //usersReference.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
    }

    public int getCurrentPlayer(String player1Uid) {

        if (player1Uid.equals(localCurrentUserUid)) {
            return 1;
        } else {
            return 2;
        }
    }
}
