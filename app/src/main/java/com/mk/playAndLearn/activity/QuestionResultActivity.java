package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;

import java.util.ArrayList;

public class QuestionResultActivity extends AppCompatActivity {
    TextView resultText;
    boolean correct;
    ArrayList list = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "";
    int questionNo, score;
    String subject, challengeId;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints, currentChallenger, sleepTime = 750;
    boolean isGeneralChallenge;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_question_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        resultText = findViewById(R.id.resultText);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            correct = intent.getBooleanExtra("answer", false);
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
                } else if (currentChallenger == 2) {
                    challengeId = intent.getStringExtra("challengeId");
                }
            }
        }

        if (correct) {
            resultText.setText("إجابة صحيحة");
            resultText.setTextColor(Color.GREEN);
            playerAnswersBooleansList += true + " ";//TODO
            score++;
        } else {
            resultText.setText("إجابة خاطئة");
            resultText.setTextColor(Color.RED);
            playerAnswersBooleansList += false + " ";
        }

        i = new Intent(this, QuestionActivity.class);


        if (questionNo + 1 < list.size()) {
            Thread timer = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        i.putExtra("questionList", list);
                        i.putExtra("questionNo", questionNo + 1);
                        i.putExtra("score", score);
                        i.putExtra("isGeneralChallenge", isGeneralChallenge);

                        if (!isGeneralChallenge) {
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
                        }

                        startActivity(i);
                        finish();
                    }
                }
            };
            timer.start();
        } else {
            Thread timer = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent1 = new Intent(QuestionResultActivity.this, ChallengeResultActivity.class);
                        intent1.putExtra("score", score);
                        intent1.putExtra("isGeneralChallenge", isGeneralChallenge);

                        if(!isGeneralChallenge) {
                            intent1.putExtra("currentChallenger", currentChallenger);
                            intent1.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
                            intent1.putExtra("currentPlayerAnswers", playerAnswersList);
                            intent1.putExtra("subject", subject);

                            if (currentChallenger == 1) {
                                intent1.putExtra("player2Name", secondPlayerName);
                                intent1.putExtra("player2Email", secondPlayerEmail);
                                intent1.putExtra("player2Image", secondPlayerImage);
                                intent1.putExtra("player2Uid", secondPlayerUid);
                                intent1.putExtra("player2Points", secondPlayerPoints);
                                intent1.putParcelableArrayListExtra("questionsList", list);
                            } else if (currentChallenger == 2) {
                                intent1.putExtra("challengeId", challengeId);
                            }
                        }
                        startActivity(intent1);
                        finish();
                    }
                }
            };
            timer.start();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
