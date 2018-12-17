package com.mk.playAndLearn.activity;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.AdManager;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;

public class ChallengeStartActivity extends AppCompatActivity {
    //TODO : make this page loads until all data finished loading by hiding the 4 main views until the data loads
    //TODO : make the app loads only five questions from the database
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseDatabase database;

    SharedPreferences pref;

    String challengeQuestionsIds;
    ArrayList list = new ArrayList<>();
    String playerAnswersBooleansList = "", playerAnswersList = "";

    Intent i;
    Context context;

    String firstPlayerName, firstPlayerEmail, firstPlayerImage, firstPlayerUid;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    String subject, challengeId;
    int firstPlayerPoints = -1, currentChallenger = 1;
    long secondPlayerPoints = -1;

    ImageView player1Image, player2Image;
    TextView player1Name, player1Points, player2Name, player2Points;
    Button startChallengeButton;
    ProgressBar horizontalProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_start);

        i = new Intent(ChallengeStartActivity.this, QuestionActivity.class);
        context = this;

        AdManager adManager = AdManager.getInstance();
        adManager.createAd(this);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
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

        horizontalProgressBar = findViewById(R.id.horizontalProgressbarInChallengeStartActivity);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            currentChallenger = intent.getIntExtra("currentChallenger", 1);
            subject = intent.getStringExtra("subject");
            secondPlayerUid = intent.getStringExtra("uid");

            if (currentChallenger == 2) {
                challengeId = intent.getStringExtra("challengeId");
                challengeQuestionsIds = intent.getStringExtra("questionsList");
            } else {
                secondPlayerName = intent.getStringExtra("name");
                secondPlayerEmail = intent.getStringExtra("email");
                secondPlayerImage = intent.getStringExtra("image");
                secondPlayerPoints = intent.getIntExtra("points", -1);
            }
        }
        DatabaseReference usersReference = database.getReference("users");
        if (currentChallenger == 2) {
            usersReference.child(secondPlayerUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    secondPlayerName = (String) dataSnapshot.child("userName").getValue();
                    secondPlayerImage = (String) dataSnapshot.child("userImage").getValue();
                    if (dataSnapshot.child("points").getValue() != null)
                        secondPlayerPoints = (long) dataSnapshot.child("points").getValue();

                    player2Name.setText(secondPlayerName);
                    Picasso.with(ChallengeStartActivity.this).load(secondPlayerImage).into(player2Image);
                    player2Points.setText(secondPlayerPoints + " XP");

                    Log.v("ChallengeStartLog", "dataSnapshot is : " + dataSnapshot.toString());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (currentChallenger == 1) {
            player2Name.setText(secondPlayerName);
            Picasso.with(ChallengeStartActivity.this).load(secondPlayerImage).placeholder(R.drawable.picasso_placeholder).into(player2Image);
            player2Points.setText(secondPlayerPoints + "");
        }

        usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstPlayerPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                player1Points.setText(firstPlayerPoints + " XP");
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
                /*if (list.size() < 5 && currentChallenger == 1) {
                    showDialog();
                } else if (currentChallenger == 1 && list.size() >= 5) {
                    if (!chosenQuestionsList.isEmpty())
                        chosenQuestionsList.clear();
                    Collections.shuffle(list);
                    for (int i = 0; i < 5; i++) {
                        Question question = (Question) list.get(i);
                        chosenQuestionsList.add(question);
                    }
                }*/

               // Toast.makeText(ChallengeStartActivity.this, "جارى إعداد الأسئلة", Toast.LENGTH_SHORT).show();

                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                i.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
                i.putExtra("currentPlayerAnswers", playerAnswersList);
                i.putExtra("questionNo", 0);
                i.putExtra("score", 0);
                i.putExtra("subject", subject);
                i.putExtra("currentChallenger", currentChallenger);
                i.putExtra("isGeneralChallenge", false);
                i.putParcelableArrayListExtra("questionList", list);


                if (currentChallenger == 1) {
                    i.putExtra("player2Name", secondPlayerName);
                    i.putExtra("player2Email", secondPlayerEmail);
                    i.putExtra("player2Image", secondPlayerImage);
                    i.putExtra("player2Points", secondPlayerPoints);
                    i.putExtra("player2Uid", secondPlayerUid);
                } else {
                    i.putExtra("challengeId", challengeId);
                }

                if (currentChallenger == 1) {
                    horizontalProgressBar.setVisibility(View.VISIBLE);

                    if (!list.isEmpty())
                        list.clear();


                    loadQuestionsForChallenger1();

                } else if (currentChallenger == 2) {
                    horizontalProgressBar.setVisibility(View.VISIBLE);

                    if (!list.isEmpty())
                        list.clear();

                    //setQuestionsList
                    String[] questionsIds = challengeQuestionsIds.split(" ");
                    final int listSize = questionsIds.length;
                    for (String questionId : questionsIds) {
                        fireStoreQuestions.document(subject).collection(subject).document(questionId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                addQuestionData(documentSnapshot);
                                if (list.size() == listSize) {
                                    context.startActivity(i);
                                    finish();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void addQuestionData(DocumentSnapshot document) {
        Question question = new Question();
        String questionText = document.getString("alQuestion");
        String answer1 = document.getString("answer1");
        String answer2 = document.getString("answer2");
        String answer3 = document.getString("answer3");
        String answer4 = document.getString("answer4");
        String correctAnswer = document.getString("correctAnswer");
        String writerName = document.getString("writerName");
        boolean reviewed = document.getBoolean("reviewed");

        question.setAnswer1(answer1);
        question.setAnswer2(answer2);
        question.setAnswer3(answer3);
        question.setAnswer4(answer4);
        question.setCorrectAnswer(correctAnswer);
        question.setWriterName(writerName);
        question.setAlQuestion(questionText);
        question.setReviewed(reviewed);
        question.setQuestionId(document.getId());

        if (list.size() < 5 && !existsInList(questionText))
            list.add(question);
    }

    void loadQuestionsForChallenger1() {
        String randomId = fireStoreQuestions.document().getId();

        fireStoreQuestions.document(subject).collection(subject)
                .whereEqualTo("reviewed", true)
                .whereGreaterThan(FieldPath.documentId(), randomId)
                .limit(1)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                    addQuestionData(document);
                    if (list.size() < 5) {
                        loadQuestionsForChallenger1();
                    } else {
                        context.startActivity(i);
                        finish();
                    }
                }
            }
        });
    }

    boolean existsInList(String questionText) {
        ArrayList<Question> localList = list;
        for (Question q : localList) {
            if (q.getAlQuestion().equals(questionText)) {
                return true;
            }
        }
        return false;
    }
}
