package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.AdManager;
import com.mk.playAndLearn.utils.DateClass;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.auth;
import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.database;
import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.lastActiveUsersReference;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Integers.drawChallengePoints;
import static com.mk.playAndLearn.utils.Integers.generalChallengeScoreMultiply;
import static com.mk.playAndLearn.utils.Integers.wonChallengePoints;
import static com.mk.playAndLearn.utils.Strings.drawChallengeText;
import static com.mk.playAndLearn.utils.Strings.loseChallengeText;
import static com.mk.playAndLearn.utils.Strings.wonChallengeText;

public class ChallengeResultActivity extends AppCompatActivity {
    //TODO : think about removing challenge result activity but think well before determine what to do in this
    //TODO : handle loosing internet connection before uploading data for example show a dialog when try to go out.

    String subject, challengeId, currentUserName;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    int score, currentChallenger;
    long opponentScore;

    ArrayList questionsList = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "";
    String localCurrentUserUid;
    boolean isGeneralChallenge;

    public SharedPreferences pref; // 0 - for private mode

    DateClass dateClass = new DateClass();
    ImageView player1ImageTv, player2ImageTv;
    TextView player1NameTv, player1ScoreTv, player2NameTv, player2ScoreTv;
    TextView challengeResultTv, challengeStateTv, againstTv;

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

        localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        player1NameTv = findViewById(R.id.firstPlayerName);
        player1ImageTv = findViewById(R.id.firstPlayerImage);
        player1ScoreTv = findViewById(R.id.firstPlayerScore);
        player2NameTv = findViewById(R.id.secondPlayerName);
        player2ImageTv = findViewById(R.id.secondPlayerImage);
        player2ScoreTv = findViewById(R.id.secondPlayerScore);
        challengeResultTv = findViewById(R.id.challengeResultText);
        challengeStateTv = findViewById(R.id.challengeState);
        againstTv = findViewById(R.id.againstTextView);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        currentUserName = pref.getString("currentUserName", "غير معروف");
        Log.v("sharedPreference", " current userName is : " + currentUserName);

        String localCurrentUserImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        AdManager adManager = AdManager.getInstance();
        InterstitialAd ad = adManager.getAd();
        Log.v("contestLogging", "ad is : " + ad + " , ad loaded is : " + ad.isLoaded());
        if (ad != null && ad.isLoaded()) {
            ad.show();
        }

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

        player1NameTv.setText(currentUserName);
        Picasso.with(this).load(localCurrentUserImage).into(player1ImageTv);
        player1ScoreTv.setText(score + "");

        if (currentChallenger == 1) {
            player2ScoreTv.setText(0 + "");
            player2NameTv.setText(secondPlayerName);
            Picasso.with(ChallengeResultActivity.this).load(secondPlayerImage).into(player2ImageTv);
            challengeStateTv.setText("فى إنتظار المنافس");
        } else if(currentChallenger == 2 && !isGeneralChallenge){
            fireStoreChallenges.document(challengeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        opponentScore = documentSnapshot.getLong("player1score");
                        String opponentName = documentSnapshot.getString("player1Name");
                        String opponentImage = documentSnapshot.getString("player1Image");

                        player2ScoreTv.setText(opponentScore + "");
                        player2NameTv.setText(opponentName);
                        Picasso.with(ChallengeResultActivity.this).load(opponentImage).into(player2ImageTv);

                        int opponentScoreInt = (int) opponentScore;

                        if (score == opponentScoreInt) {
                            challengeStateTv.setText(drawChallengeText);
                        } else {
                            if (score > opponentScoreInt) {
                                challengeStateTv.setText(wonChallengeText);
                                challengeStateTv.setBackgroundColor(ChallengeResultActivity.this.getResources().getColor(R.color.green));
                            } else {
                                challengeStateTv.setText(loseChallengeText);
                                challengeStateTv.setBackgroundColor(ChallengeResultActivity.this.getResources().getColor(R.color.red));
                            }
                        }
                    }
                }
            });

        }

        //TODO : check that this date is correct
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String todayDate = format.format(today);

        dateClass.setDate(today);

        if (!isGeneralChallenge) {
            Map<String, Object> map = new HashMap<>();
            if (currentChallenger == 1) {

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
                map.put("score1Added", false);
                map.put("date", dateClass.getDate());
                map.put("dayDate", todayDate);
                map.put("subject", subject);
                map.put("questionsId", getQuestionsId());
                map.put("player1AnswersBooleans", playerAnswersBooleansList.trim());
                map.put("player1Answers", playerAnswersList);
                map.put("state", "لم يكتمل"); // TODO : edit this

                fireStoreChallenges.document().set(map);

                usersReference.child(localCurrentUserUid).child("lastChallengeDate").setValue(dateClass.getDate());
            } else if (currentChallenger == 2) {
                fireStoreChallenges.document(challengeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            fireStoreChallenges.document(challengeId).update("player2score", score);
                            fireStoreChallenges.document(challengeId).update("player2AnswersBooleans", playerAnswersBooleansList.trim());
                            fireStoreChallenges.document(challengeId).update("player2Answers", playerAnswersList);
                            fireStoreChallenges.document(challengeId).update("state", "اكتمل");

                            usersReference.child(documentSnapshot.getString("player2Uid")).child("lastChallengeDate").setValue(dateClass.getDate());

                            addPoints(documentSnapshot);
                        }
                    }
                });
            }
        } else {
            challengeResultTv.setVisibility(View.VISIBLE);
            hideAllViews();

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
                player2Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long points = (long) dataSnapshot.child("points").getValue();
                        usersReference.child(player2Uid).child("points").setValue(points + drawChallengePoints);

                        //usersReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else if(player2Score > player1Score){
                    player2Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long points = (long) dataSnapshot.child("points").getValue();
                            usersReference.child(player2Uid).child("points").setValue(points + wonChallengePoints);
                            }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

    public String getQuestionsId() {
        String Ids = "";
        ArrayList<Question> list = questionsList;
        for (Question question : list) {
            Ids += question.getQuestionId() + " ";//TODO : check this
        }
        return Ids.trim();
    }

    void hideAllViews() {
        player1NameTv.setVisibility(View.GONE);
        player1ImageTv.setVisibility(View.GONE);
        player1ScoreTv.setVisibility(View.GONE);
        player2NameTv.setVisibility(View.GONE);
        player2ImageTv.setVisibility(View.GONE);
        player2ScoreTv.setVisibility(View.GONE);
        challengeStateTv.setVisibility(View.GONE);
        againstTv.setVisibility(View.GONE);
    }
}
