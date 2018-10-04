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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Collections;

public class QuestionActivity extends AppCompatActivity {
    ArrayList list = new ArrayList(), playerAnswersBooleansList = new ArrayList(), playerAnswersList = new ArrayList();
    TextView tvQuestion;
    RadioGroup rg1;
    Button nextButton;
    String selection, correctAnswer;
    RadioButton r1, r2, r3, r4;
    Intent i;
    int questionNo, score;
    CountDownTimer timer;
    String subject;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    ProgressBar timerProgressBar;
    int index = 0;
    //TODO : change the xml tags to support
    //TODO : handle what happens when internet connection problem occurs in a challenge
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        i = new Intent(QuestionActivity.this, QuestionResultActivity.class);

        rg1 = findViewById(R.id.radioGroup);
        nextButton = findViewById(R.id.nextButton);
        tvQuestion = findViewById(R.id.questionText);
        r1 = findViewById(R.id.radio1);
        r2 = findViewById(R.id.radio2);
        r3 = findViewById(R.id.radio3);
        r4 = findViewById(R.id.radio4);
        timerProgressBar = findViewById(R.id.timerProgressbar);


        Intent intent = getIntent();
        if (intent != null) {
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
        Question question = (Question) list.get(questionNo);
        correctAnswer = question.getCorrectAnswer();

        tvQuestion.setText(question.getAlQuestion());
        ArrayList<String> answers = new ArrayList<>();
        answers.add(question.getAnswer1());
        answers.add(question.getAnswer2());
        answers.add(question.getAnswer3());
        answers.add(question.getAnswer4());
        Collections.shuffle(answers);

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
        //TODO : think about making the timer works from the end to the begging
        //
        // TODO : solve timer running in the end problem
        timer = new CountDownTimer(21000, 200) {

            public void onTick(long millisUntilFinished) {
                index++;
                timerProgressBar.setProgress((int)millisUntilFinished/1000);
            }

            public void onFinish() {
                index++;
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
        playerAnswersList.add(selection);
        i.putParcelableArrayListExtra("list", list);
        i.putExtra("questionNo", questionNo);
        i.putExtra("score", score);
        i.putExtra("subject", subject);
        i.putExtra("player2Name", secondPlayerName);
        i.putExtra("player2Email", secondPlayerEmail);
        i.putExtra("player2Image", secondPlayerImage);
        i.putExtra("player2Uid", secondPlayerUid);
        i.putExtra("player2Points", secondPlayerPoints);
        i.putParcelableArrayListExtra("player1AnswersBooleans", playerAnswersBooleansList);
        i.putParcelableArrayListExtra("player1Answers", playerAnswersList);
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
