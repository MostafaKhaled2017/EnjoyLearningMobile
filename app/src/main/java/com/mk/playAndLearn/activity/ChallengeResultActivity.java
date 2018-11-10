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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.DateClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
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
                questionsList =  intent.getParcelableArrayListExtra("questionsList");
            } else if (currentChallenger == 2) {
                challengeId = intent.getStringExtra("challengeId");
            }
        }
        DateClass dateClass = new DateClass();
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        String date = format.format(today);
        dateClass.setDate(today);

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
                map.put("date", dateClass.getDate());
                map.put("subject", subject);
                map.put("questionsId", getQuestionsId());
                map.put("player1AnswersBooleans", playerAnswersBooleansList.trim());
                map.put("player1Answers", playerAnswersList);
                map.put("state", "لم يكتمل"); // TODO : edit this

                fireStoreChallenges.document().set(map);
            } else if (currentChallenger == 2) {
                fireStoreChallenges.document(challengeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            fireStoreChallenges.document(challengeId).update("player2score", score);
                            fireStoreChallenges.document(challengeId).update("player2AnswersBooleans", playerAnswersBooleansList.trim());
                            fireStoreChallenges.document(challengeId).update("player2Answers", playerAnswersList);
                            fireStoreChallenges.document(challengeId).update("state", "اكتمل");

                            addPoints(documentSnapshot);

                        }
                    }
                });
            }
        } else {
            usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int lastGeneralChallengePoints = Integer.parseInt(dataSnapshot.child("lastGeneralChallengeScore").getValue().toString());
                    int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                    int finalChallengePoints = score * generalChallengeScoreMultiply;
                    challengeResultTv.setText("نتيجة التحدى : " + 100 + " / " + finalChallengePoints); //TODO : edit this to (questionsList.size() * generalChallengeScoreMultiply)
                    if (lastGeneralChallengePoints == 0) {
                        usersReference.child(currentUser.getUid()).child("lastGeneralChallengeScore").setValue(finalChallengePoints);
                        usersReference.child(currentUser.getUid()).child("points").setValue(userPoints + finalChallengePoints);
                    } else {
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

    void addPoints(DocumentSnapshot dataSnapshot) {
        final String player1Uid = dataSnapshot.getString("player1Uid");
        final String player2Uid = dataSnapshot.getString("player2Uid");

        long player1Score = (long) dataSnapshot.getLong("player1score");
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

    public String getQuestionsId(){
        String Ids = "";
        ArrayList<Question> list = questionsList;
        for(Question question : list){
            Ids += question.getQuestionId() + " ";//TODO : check this
        }
        return Ids.trim();
    }
}
