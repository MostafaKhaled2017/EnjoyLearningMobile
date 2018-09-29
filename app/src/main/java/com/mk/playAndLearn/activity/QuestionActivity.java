package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList list = new ArrayList();
    TextView tvQuestion;
    RadioGroup rg1;
    Button nextButton;
    String selection, correctAnswer;
    RadioButton r1, r2, r3, r4;
    Intent i;
    int position, score;
    //TODO : change the xml tags to support
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
        i = new Intent(QuestionActivity.this,QuestionResultActivity.class);

        rg1 = findViewById(R.id.radioGroup);
        nextButton = findViewById(R.id.nextButton);
        tvQuestion = findViewById(R.id.questionText);
        r1 = findViewById(R.id.radio1);
        r2 = findViewById(R.id.radio2);
        r3 = findViewById(R.id.radio3);
        r4 = findViewById(R.id.radio4);

        Intent intent = getIntent();
        if(intent != null){
           list = intent.getParcelableArrayListExtra("list");
           position = intent.getIntExtra("position",-1);
           score = intent.getIntExtra("score", -1);
        }
        Question question = (Question) list.get(position);
        correctAnswer = question.getCorrectAnswer();

        tvQuestion.setText(question.getQuestion());
        r1.setText(question.getAns1());
        r2.setText(question.getAns2());
        r3.setText(question.getAns3());
        r4.setText(question.getAns4());

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rg1.getCheckedRadioButtonId()!=-1){
                    int id= rg1.getCheckedRadioButtonId();
                    View radioButton = rg1.findViewById(id);
                    int radioId = rg1.indexOfChild(radioButton);
                    RadioButton btn = (RadioButton) rg1.getChildAt(radioId);
                    selection = (String) btn.getText();
                }
                if(selection.equals(correctAnswer)){
                    i.putExtra("answer", true);
                }
                else {
                   i.putExtra("answer", false);
                }
                i.putParcelableArrayListExtra("list", list);
                i.putExtra("position", position);
                i.putExtra("score", score);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("هل أنت متأكد أنك تريد الخروج وفقدان نقط هذا التحدي");
        dialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {//TODO : edit this
                startActivity(new Intent(QuestionActivity.this, MainActivity.class));
            }
        });
        dialog.create();
        dialog.show();

        return  true;
    }

}
