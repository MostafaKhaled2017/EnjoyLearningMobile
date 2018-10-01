package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Collections;

public class QuestionActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList list = new ArrayList();
    TextView tvQuestion, currentSecondTv;
    RadioGroup rg1;
    Button nextButton;
    String selection, correctAnswer;
    RadioButton r1, r2, r3, r4;
    Intent i;
    int questionNo, score;
    CountDownTimer timer;
    String subject;

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
        currentSecondTv = findViewById(R.id.currentSecondTv);

        Intent intent = getIntent();
        if (intent != null) {
            list = intent.getParcelableArrayListExtra("list");
            questionNo = intent.getIntExtra("questionNo", -1);
            score = intent.getIntExtra("score", -1);
            subject = intent.getStringExtra("subject");
        }
        Question question = (Question) list.get(questionNo);
        correctAnswer = question.getCorrectAnswer();

        tvQuestion.setText(question.getQuestion());
        ArrayList<String> answers = new ArrayList<>();
        answers.add(question.getAns1());
        answers.add(question.getAns2());
        answers.add(question.getAns3());
        answers.add(question.getAns4());
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
        timer = new CountDownTimer(21000, 1000) {

            public void onTick(long millisUntilFinished) {
                long currentSecond = millisUntilFinished / 1000;
                if (currentSecond == 10)
                    currentSecondTv.setTextColor(Color.YELLOW);
                if (currentSecond == 5)
                    currentSecondTv.setTextColor(Color.RED);
                if (currentSecond < 10)
                    currentSecondTv.setText("0" + currentSecond);
                else
                    currentSecondTv.setText(currentSecond + "");
            }

            public void onFinish() {
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
        dialog.setMessage("هل أنت متأكد أنك تريد الخروج وفقدان نقط هذا التحدي");
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
        i.putParcelableArrayListExtra("list", list);
        i.putExtra("questionNo", questionNo);
        i.putExtra("score", score);
        i.putExtra("subject", subject);
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
