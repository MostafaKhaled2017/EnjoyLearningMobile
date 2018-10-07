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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;

public class AppManagementActivity extends AppCompatActivity {

    ArrayList list = new ArrayList();
    FirebaseDatabase database;
    DatabaseReference questionsReference;
    boolean questionsReady = false, lessonsReady = false;
    Button suggestedQuestionsButton, suggestedLessonsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_management);
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
        //toolbarTitle.setText("title");

        database = FirebaseDatabase.getInstance();
        questionsReference = database.getReference("questions");

        getSuggestedQuestions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void getSuggestedQuestions() {
        if (!list.isEmpty())
            list.clear();
        questionsReference.orderByChild("reviewed").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Question question = new Question();
                    String questionText = dataSnapshot1.child("al question").getValue().toString();
                    String answer1 = dataSnapshot1.child("answer 1").getValue().toString();
                    String answer2 = dataSnapshot1.child("answer 2").getValue().toString();
                    String answer3 = dataSnapshot1.child("answer 3").getValue().toString();
                    String answer4 = dataSnapshot1.child("answer 4").getValue().toString();
                    String subject = dataSnapshot1.child("subject").getValue().toString();//extra than normal
                    String writerEmail = dataSnapshot1.child("writerEmail").getValue().toString();//extra than normal
                    String correctAnswer = dataSnapshot1.child("correctAnswer").getValue().toString();
                    String writerName = dataSnapshot1.child("writerName").getValue().toString();
                    String writerUid = dataSnapshot1.child("writerUid").getValue().toString();
                    String questionId = dataSnapshot1.getKey();
                    question.setAnswer1(answer1);
                    question.setAnswer2(answer2);
                    question.setAnswer3(answer3);
                    question.setAnswer4(answer4);
                    question.setCorrectAnswer(correctAnswer);
                    question.setWriterName(writerName);
                    question.setQuestionId(questionId);
                    question.setWriterUid(writerUid);
                    question.setAlQuestion(questionText);
                    question.setSubject(subject);//extra than normal
                    question.setWriterEmail(writerEmail);//extra than normal

                    list.add(question);
                }
                questionsReady = true;
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void suggestedLessonsButton(View view) {
    }

    public void suggestedQuestionsButton(View view) {
        if(questionsReady && list.size() != 0){
            Intent intent = new Intent(this, AdminQuestionActivity.class);
            intent.putParcelableArrayListExtra("questionsList", list);
            intent.putExtra("index", 0);
            startActivity(intent);
        }
        else if(!questionsReady){
            Toast.makeText(this, "الاسئلة لم تجهز بعد", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "لا يوجد أسئلة حاليا", Toast.LENGTH_SHORT).show();
        }
    }
}


