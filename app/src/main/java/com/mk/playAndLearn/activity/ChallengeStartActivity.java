package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class ChallengeStartActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference questionsReference;


    ArrayList list = new ArrayList<>(), chosenQuestionsList = new ArrayList<>(), challengeQuestionList = new ArrayList()
            , playerAnswersBooleansList = new ArrayList(), playerAnswersList = new ArrayList();

    String firstPlayerName, firstPlayerEmail, firstPlayerImage, firstPlayerUid;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    String subject, challengeId;
    int firstPlayerPoints, secondPlayerPoints, currentChallenger = 1;

    ImageView player1Image, player2Image;
    TextView player1Name, player1Points, player2Name, player2Points;
    Button startChallengeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(upArrow);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("ابدأ التحدي");//TODO : change this

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        questionsReference = database.getReference("questions");


        player1Name = findViewById(R.id.firstPlayerName);
        player1Image = findViewById(R.id.firstPlayerImage);
        player1Points = findViewById(R.id.firstPlayerPoints);
        player2Name = findViewById(R.id.secondPlayerName);
        player2Image = findViewById(R.id.secondPlayerImage);
        player2Points = findViewById(R.id.secondPlayerPoints);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            currentChallenger = intent.getIntExtra("currentChallenger", 1);
            if(currentChallenger == 2) {
                challengeId = intent.getStringExtra("challengeId");
                challengeQuestionList = intent.getParcelableArrayListExtra("questionsList");
                secondPlayerName = intent.getStringExtra("secondChallengerName");
                secondPlayerImage = intent.getStringExtra("secondChallengerImage");
                secondPlayerPoints = intent.getIntExtra("secondChallengerPoints", -1);
            }
            else {
                secondPlayerName = intent.getStringExtra("name");
                secondPlayerEmail = intent.getStringExtra("email");
                secondPlayerImage = intent.getStringExtra("image");
                secondPlayerPoints = intent.getIntExtra("points", -1);
                secondPlayerUid = intent.getStringExtra("uid");
                subject = intent.getStringExtra("subject");
            }
        }

        if (!list.isEmpty())
            list.clear();
        questionsReference.orderByChild("subject").equalTo(subject).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (list.isEmpty()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Question question = new Question();
                        String questionText = dataSnapshot1.child("al question").getValue().toString();
                        String answer1 = dataSnapshot1.child("answer 1").getValue().toString();
                        String answer2 = dataSnapshot1.child("answer 2").getValue().toString();
                        String answer3 = dataSnapshot1.child("answer 3").getValue().toString();
                        String answer4 = dataSnapshot1.child("answer 4").getValue().toString();
                        String correctAnswer = dataSnapshot1.child("correctAnswer").getValue().toString();
                        String writerName = dataSnapshot1.child("writerName").getValue().toString();
                        boolean reviewed = ((boolean) dataSnapshot1.child("reviewed").getValue());
                        if (reviewed) {
                            question.setAnswer1(answer1);
                            question.setAnswer2(answer2);
                            question.setAnswer3(answer3);
                            question.setAnswer4(answer4);
                            question.setCorrectAnswer(correctAnswer);
                            question.setWriterName(writerName);
                            question.setAlQuestion(questionText);

                            list.add(question);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final DatabaseReference usersRefrence = database.getReference("users");
        usersRefrence.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstPlayerPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        firstPlayerName = currentUser.getDisplayName();
        firstPlayerEmail = currentUser.getEmail();
        firstPlayerUid = currentUser.getUid();
        firstPlayerImage = currentUser.getPhotoUrl().toString();
        startChallengeButton = findViewById(R.id.lastStartChallengeButton);

        player1Name.setText(firstPlayerName);
        Picasso.with(this).load(firstPlayerImage).into(player1Image);
        player1Points.setText(firstPlayerPoints + "");

        player2Name.setText(secondPlayerName);
        Picasso.with(this).load(secondPlayerImage).into(player2Image);
        player2Points.setText(secondPlayerPoints + "");

        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentChallenger == 1) {
                    if (!chosenQuestionsList.isEmpty())
                        chosenQuestionsList.clear();
                    Collections.shuffle(list);
                    for (int i = 0; i < 5; i++) {
                        Question question = (Question) list.get(i);
                        chosenQuestionsList.add(question);
                    }
                }
                //TODO : after adding the app to play store change challenge activities to fragment to be able to send data one time instead of sending it with intents multiple time
                Intent i = new Intent(ChallengeStartActivity.this, QuestionActivity.class);


                i.putParcelableArrayListExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
                i.putParcelableArrayListExtra("currentPlayerAnswers", playerAnswersList);
                i.putExtra("questionNo", 0);
                i.putExtra("score", 0);
                i.putExtra("subject", subject);
                i.putExtra("currentChallenger", currentChallenger);

                if(currentChallenger == 1) {
                    i.putExtra("player2Name", secondPlayerName);
                    i.putExtra("player2Email", secondPlayerEmail);
                    i.putExtra("player2Image", secondPlayerImage);
                    i.putExtra("player2Points", secondPlayerPoints);
                    i.putExtra("player2Uid", secondPlayerUid);
                    i.putParcelableArrayListExtra("list", chosenQuestionsList);
                }
                else{
                    i.putParcelableArrayListExtra("list", challengeQuestionList);
                    i.putExtra("challengeId", challengeId);
                }

                //TODO : ensuring that the intent doesn't work until the data loaded for example if not show a dialog asking to connect to the internet
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
