package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.Inflater;

public class ChallengeStartActivity extends AppCompatActivity {
    //TODO : make this page loads until all data finished loading by hiding the 4 main views until the data loads
    //TODO : make the app loads only five questions from the database
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference questionsReference;

    SharedPreferences pref;

    ArrayList list = new ArrayList<>(), chosenQuestionsList = new ArrayList<>(), challengeQuestionList = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "";

    String firstPlayerName, firstPlayerEmail, firstPlayerImage, firstPlayerUid;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    String subject, challengeId;
    int firstPlayerPoints = -1, currentChallenger = 1;
    long secondPlayerPoints;
    boolean finished = false;

    ImageView player1Image, player2Image;
    TextView player1Name, player1Points, player2Name, player2Points;
    Button startChallengeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_start);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        questionsReference = database.getReference("questions");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(upArrow);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("ابدأ التحدي");

        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode //TODO : check this

        player1Name = findViewById(R.id.firstPlayerName);
        player1Image = findViewById(R.id.firstPlayerImage);
        player1Points = findViewById(R.id.firstPlayerPoints);
        player2Name = findViewById(R.id.secondPlayerName);
        player2Image = findViewById(R.id.secondPlayerImage);
        player2Points = findViewById(R.id.secondPlayerPoints);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            currentChallenger = intent.getIntExtra("currentChallenger", 1);

            secondPlayerUid = intent.getStringExtra("uid");

            if (currentChallenger == 2) {
                challengeId = intent.getStringExtra("challengeId");
                challengeQuestionList = intent.getParcelableArrayListExtra("questionsList");
            } else {
                secondPlayerName = intent.getStringExtra("name");
                secondPlayerEmail = intent.getStringExtra("email");
                secondPlayerImage = intent.getStringExtra("image");
                secondPlayerPoints = intent.getIntExtra("points", -1);
                subject = intent.getStringExtra("subject");
            }
        }
        DatabaseReference usersReference = database.getReference("users");
        if (currentChallenger == 2) {
            usersReference.child(secondPlayerUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    secondPlayerName = dataSnapshot.child("userName").getValue().toString();
                    secondPlayerImage = dataSnapshot.child("userImage").getValue().toString();
                    secondPlayerPoints = (long) dataSnapshot.child("points").getValue();

                    player2Name.setText(secondPlayerName);
                    Picasso.with(ChallengeStartActivity.this).load(secondPlayerImage).into(player2Image);
                    player2Points.setText(secondPlayerPoints + "");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (currentChallenger == 1) {
            player2Name.setText(secondPlayerName);
            Picasso.with(ChallengeStartActivity.this).load(secondPlayerImage).placeholder(R.drawable.picasso_placeholder).into(player2Image);
            player2Points.setText(secondPlayerPoints + "");

            if (!list.isEmpty())
                list.clear();
            questionsReference.orderByChild("subject").equalTo(subject).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Question question = new Question();
                    String questionText = dataSnapshot.child("al question").getValue().toString();
                    String answer1 = dataSnapshot.child("answer 1").getValue().toString();
                    String answer2 = dataSnapshot.child("answer 2").getValue().toString();
                    String answer3 = dataSnapshot.child("answer 3").getValue().toString();
                    String answer4 = dataSnapshot.child("answer 4").getValue().toString();
                    String correctAnswer = dataSnapshot.child("correctAnswer").getValue().toString();
                    String writerName = dataSnapshot.child("writerName").getValue().toString();
                    boolean reviewed = ((boolean) dataSnapshot.child("reviewed").getValue());
                    if (reviewed) {
                        question.setAnswer1(answer1);
                        question.setAnswer2(answer2);
                        question.setAnswer3(answer3);
                        question.setAnswer4(answer4);
                        question.setCorrectAnswer(correctAnswer);
                        question.setWriterName(writerName);
                        question.setAlQuestion(questionText);
                        question.setReviewed(reviewed);

                        list.add(question);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstPlayerPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                player1Points.setText(firstPlayerPoints + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        firstPlayerName = pref.getString("currentUserName", "غير معروف");
        firstPlayerEmail = currentUser.getEmail();
        firstPlayerUid = currentUser.getUid();
        firstPlayerImage = currentUser.getPhotoUrl().toString();
        startChallengeButton = findViewById(R.id.lastStartChallengeButton);

        player1Name.setText(firstPlayerName);
        Picasso.with(this).load(firstPlayerImage).placeholder(R.drawable.picasso_placeholder).into(player1Image);

        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO : after adding the app to play store change challenge activities to fragment to be able to send data one time instead of sending it with intents multiple time
                if(list.size() < 5 && currentChallenger == 1){
                    showDialog();
                }
                else if (currentChallenger == 1 && list.size() >= 5) {
                        if (!chosenQuestionsList.isEmpty())
                            chosenQuestionsList.clear();
                        Collections.shuffle(list);
                        for (int i = 0; i < 5; i++) {
                            Question question = (Question) list.get(i);
                            chosenQuestionsList.add(question);
                        }
                    }
                    Intent i = new Intent(ChallengeStartActivity.this, QuestionActivity.class);
                    i.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
                    i.putExtra("currentPlayerAnswers", playerAnswersList);
                    i.putExtra("questionNo", 0);
                    i.putExtra("score", 0);
                    i.putExtra("subject", subject);
                    i.putExtra("currentChallenger", currentChallenger);

                    if (currentChallenger == 1) {
                        i.putExtra("player2Name", secondPlayerName);
                        i.putExtra("player2Email", secondPlayerEmail);
                        i.putExtra("player2Image", secondPlayerImage);
                        i.putExtra("player2Points", secondPlayerPoints);
                        i.putExtra("player2Uid", secondPlayerUid);
                        i.putParcelableArrayListExtra("questionList", chosenQuestionsList);
                    } else {
                        i.putParcelableArrayListExtra("questionList", challengeQuestionList);
                        i.putExtra("challengeId", challengeId);
                    }

                if(chosenQuestionsList.size() >= 5 || challengeQuestionList.size() >= 5) {
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
    public void showDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChallengeStartActivity.this);
        dialog.setTitle("أنت غير جاهز للبدء");//TODO : think about changing these two texts
        dialog.setMessage("سرعة الانترنت لديك بطيئة حاول بدء التحدي مرة أخري");
        dialog.setPositiveButton("موافق", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }
}
