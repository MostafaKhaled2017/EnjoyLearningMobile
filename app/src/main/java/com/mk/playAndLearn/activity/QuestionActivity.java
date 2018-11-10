package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Collections;

import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Integers.generalChallengeScoreMultiply;

public class QuestionActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "";
    TextView tvQuestion;
    RadioGroup rg1;
    Button nextButton;
    String selection, correctAnswer;
    RadioButton r1, r2, r3, r4;
    Intent i;
    int questionNo, score, currentChallenger;
    boolean isGeneralChallenge = true;
    CountDownTimer timer;
    String subject, challengeId;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    ProgressBar timerProgressBar;

    //TODO : change the xml tags to support
    //TODO : handle what happens when internet connection problem occurs in a challenge
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        i = new Intent(QuestionActivity.this, QuestionResultActivity.class);

        rg1 = findViewById(R.id.radioGroup);
        nextButton = findViewById(R.id.skipQuestionButton);
        tvQuestion = findViewById(R.id.questionText);
        r1 = findViewById(R.id.radio1);
        r2 = findViewById(R.id.radio2);
        r3 = findViewById(R.id.radio3);
        r4 = findViewById(R.id.radio4);
        timerProgressBar = findViewById(R.id.timerProgressbar);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            list = intent.getParcelableArrayListExtra("questionList");
            questionNo = intent.getIntExtra("questionNo", -1);
            score = intent.getIntExtra("score", -1);
            isGeneralChallenge = intent.getBooleanExtra("isGeneralChallenge", false);

            if (!isGeneralChallenge) {
                currentChallenger = intent.getIntExtra("currentChallenger", -1);
                subject = intent.getStringExtra("subject");
                playerAnswersBooleansList = intent.getStringExtra("currentPlayerAnswersBooleans");
                playerAnswersList = intent.getStringExtra("currentPlayerAnswers");

                if (currentChallenger == 1) {
                    secondPlayerName = intent.getStringExtra("player2Name");
                    secondPlayerEmail = intent.getStringExtra("player2Email");
                    secondPlayerImage = intent.getStringExtra("player2Image");
                    secondPlayerUid = intent.getStringExtra("player2Uid");
                    secondPlayerPoints = intent.getIntExtra("player2Points", -1);
                } else {
                    challengeId = intent.getStringExtra("challengeId");
                }
            }
        }
        Question question = (Question) list.get(questionNo);
        correctAnswer = question.getCorrectAnswer();

        tvQuestion.setText(question.getAlQuestion());
        ArrayList<String> answers = new ArrayList<>();
        answers.add(question.getAnswer1());
        answers.add(question.getAnswer2());
        answers.add(question.getAnswer3());
        Collections.shuffle(answers);
        answers.add(question.getAnswer4());//because some question there last option is things like "all above"

        r1.setText(answers.get(0));
        r2.setText(answers.get(1));
        r3.setText(answers.get(2));
        r4.setText(answers.get(3));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate();
            }
        });

        final int millisInFuture, countDownInterval, totalSeconds;

        if (isGeneralChallenge) {
            millisInFuture = 300000;//5 minutes
            countDownInterval = 3000;//3 seconds
            totalSeconds = millisInFuture / 1000;
        } else {
            millisInFuture = 31000;
            countDownInterval = 300;
            totalSeconds = millisInFuture / 1000;
        }

        // TODO : solve timer running in the end problem
        timer = new CountDownTimer(millisInFuture, countDownInterval) {

            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) millisUntilFinished / 1000;
                int percentage = (int) (((double) secondsLeft / (double) totalSeconds) * 100.0);
                //Toast.makeText(QuestionActivity.this, "percentage is : " + percentage, Toast.LENGTH_SHORT).show();
                timerProgressBar.setProgress(percentage);
            }

            public void onFinish() {
                timerProgressBar.setProgress(0);
                navigate();
            }

        };
        timer.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showDialog();
        return true;
    }

    @Override
    public void onBackPressed() {
        showDialog();
    }

    public void showDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (currentChallenger == 2) {
            dialog.setMessage("هل أنت متأكد أنك تريد الخروج و خسارة نقط الاسئلة الباقية");
            dialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {//TODO : edit this
                    fireStoreChallenges.document(challengeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!isGeneralChallenge) {
                                fireStoreChallenges.document(challengeId).update("player2score", score);
                                fireStoreChallenges.document(challengeId).update("player2AnswersBooleans", playerAnswersBooleansList);
                                fireStoreChallenges.document(challengeId).update("player2Answers", playerAnswersList);
                                fireStoreChallenges.document(challengeId).update("state", "اكتمل");//TODO : think about changing this
                            }
                            else {
                                usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int lastGeneralChallengePoints = Integer.parseInt(dataSnapshot.child("lastGeneralChallengeScore").getValue().toString());
                                        int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                                        int finalChallengePoints = score * generalChallengeScoreMultiply;
                                        if (lastGeneralChallengePoints == 0) {
                                            usersReference.child(currentUser.getUid()).child("lastGeneralChallengeScore").setValue(finalChallengePoints);
                                            usersReference.child(currentUser.getUid()).child("points").setValue(userPoints + finalChallengePoints);
                                        } else {
                                            Toast.makeText(QuestionActivity.this, "لقد قمت بالمشاركة فى هذا التحدى من قبل ولن يتم احتساب نقاطك الحالية", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
                    finish();


                }
            });
        } else {
            dialog.setMessage("هل أنت متأكد أنك تريد الخروج و إلغاء هذا التحدى");
            dialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
        dialog.create();
        dialog.show();
    }

    public void navigate() {
        if (rg1.getCheckedRadioButtonId() != -1) {
            int id = rg1.getCheckedRadioButtonId();
            View radioButton = rg1.findViewById(id);
            int radioId = rg1.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg1.getChildAt(radioId);
            selection = (String) btn.getText();
        }
        if (selection != null && selection.equals(correctAnswer)) {
            i.putExtra("answer", true);
        } else {
            i.putExtra("answer", false);
        }
        if (questionNo < list.size()) {//TODO : check this condition
            playerAnswersList += selection + " / ";
        } else {
            playerAnswersList += selection;
        }
        i.putExtra("questionNo", questionNo);
        i.putExtra("score", score);
        i.putParcelableArrayListExtra("questionList", list);
        i.putExtra("isGeneralChallenge", isGeneralChallenge);


        if (!isGeneralChallenge) {
            i.putExtra("isGeneralChallenge", false);
            i.putExtra("subject", subject);
            i.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
            i.putExtra("currentPlayerAnswers", playerAnswersList);
            i.putExtra("currentChallenger", currentChallenger);
            if (currentChallenger == 1) {
                i.putExtra("player2Name", secondPlayerName);
                i.putExtra("player2Email", secondPlayerEmail);
                i.putExtra("player2Image", secondPlayerImage);
                i.putExtra("player2Uid", secondPlayerUid);
                i.putExtra("player2Points", secondPlayerPoints);
            } else if (currentChallenger == 2) {
                i.putExtra("challengeId", challengeId);
            }
        } else {
            i.putExtra("isGeneralChallenge", true);
        }
        timer.cancel();
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isGeneralChallenge) {
            timer.cancel();
        }
    }
}
