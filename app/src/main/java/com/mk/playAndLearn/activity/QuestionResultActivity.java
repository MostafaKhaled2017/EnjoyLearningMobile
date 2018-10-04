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
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;

import java.util.ArrayList;

public class QuestionResultActivity extends AppCompatActivity {
    TextView resultText;
    boolean correct;
    ArrayList list = new ArrayList(), playerAnswersBooleansList = new ArrayList(), playerAnswersList = new ArrayList();
    int questionNo, score;
    String subject;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        resultText = findViewById(R.id.resultText);

        Intent intent = getIntent();
        if (intent != null) {
            correct = intent.getBooleanExtra("answer", false);
            list = intent.getParcelableArrayListExtra("list");
            questionNo = intent.getIntExtra("questionNo", -1);
            score = intent.getIntExtra("score", -1);
            subject = intent.getStringExtra("subject");
            playerAnswersBooleansList = intent.getParcelableArrayListExtra("player1AnswersBooleans");
            playerAnswersList = intent.getParcelableArrayListExtra("player1Answers");
            secondPlayerName = intent.getStringExtra("player2Name");
            secondPlayerEmail= intent.getStringExtra("player2Email");
            secondPlayerImage = intent.getStringExtra("player2Image");
            secondPlayerUid = intent.getStringExtra("player2Uid");
            secondPlayerPoints = intent.getIntExtra("player2Points", -1);

        }
        i = new Intent(this, QuestionActivity.class);
        if (correct) {
            resultText.setText("إجابة صحيحة");
            resultText.setTextColor(Color.GREEN);
            playerAnswersBooleansList.add(true);
            score ++;
        } else {
            resultText.setText("إجابة خاطئة");
            resultText.setTextColor(Color.RED);
            playerAnswersBooleansList.add(false);
        }
        if (questionNo + 1 < 5) {
        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    i.putExtra("list", list);
                    i.putExtra("questionNo", questionNo + 1);
                    i.putExtra("score", score);
                    i.putExtra("subject", subject);
                    i.putExtra("player2Name", secondPlayerName);
                    i.putExtra("player2Email", secondPlayerEmail);
                    i.putExtra("player2Image", secondPlayerImage);
                    i.putExtra("player2Uid", secondPlayerUid);
                    i.putExtra("player2Points", secondPlayerPoints);
                    i.putParcelableArrayListExtra("player1AnswersBooleans", playerAnswersBooleansList);
                    i.putParcelableArrayListExtra("player1Answers", playerAnswersList);
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
    }
        else {
            Thread timer = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent1 = new Intent(QuestionResultActivity.this, ChallengeResultActivity.class);
                        intent1.putExtra("score", score);
                        intent1.putExtra("subject", subject);
                        intent1.putExtra("player2Name", secondPlayerName);
                        intent1.putExtra("player2Email", secondPlayerEmail);
                        intent1.putExtra("player2Image", secondPlayerImage);
                        intent1.putExtra("player2Uid", secondPlayerUid);
                        intent1.putExtra("player2Points", secondPlayerPoints);
                        intent1.putParcelableArrayListExtra("questionsList", list);
                        intent1.putParcelableArrayListExtra("player1AnswersBooleans", playerAnswersBooleansList);
                        intent1.putParcelableArrayListExtra("player1Answers", playerAnswersList);
                        startActivity(intent1);
                        finish();
                    }
                }
            };
            timer.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showDialog();
        return  true;
    }

    @Override
    public void onBackPressed() {
        showDialog();
    }

    public void showDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("هل أنت متأكد أنك تريد الخروج وإلغاء هذا التحدى");
        dialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {//TODO : edit this
                finish();
            }
        });
        dialog.create();
        dialog.show();
    }
}
