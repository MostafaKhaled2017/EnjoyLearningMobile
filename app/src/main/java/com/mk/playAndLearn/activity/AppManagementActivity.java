package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;

public class AppManagementActivity extends AppCompatActivity {

    ArrayList questionList = new ArrayList(), lessonsList = new ArrayList();
    FirebaseDatabase database;
    DatabaseReference questionsReference, lessonsReference;
    boolean questionsReady = false, lessonsReady = false;
    Button suggestedQuestionsButton, suggestedLessonsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_management);
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
        //toolbarTitle.setText("title");

        database = FirebaseDatabase.getInstance();
        questionsReference = database.getReference("questions");
        lessonsReference = database.getReference("lessons");

        getSuggestedQuestions();

        getSuggestedLessons();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void getSuggestedQuestions() {
        if (!questionList.isEmpty())
            questionList.clear();
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

                    questionList.add(question);
                }
                questionsReady = true;
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getSuggestedLessons() {
        if (!lessonsList.isEmpty())
            lessonsList.clear();
        lessonsReference.orderByChild("reviewed").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Lesson lesson = new Lesson();
                    String title = dataSnapshot1.child("title").getValue().toString();
                    String content = dataSnapshot1.child("content").getValue().toString();
                    String writerName = dataSnapshot1.child("writerName").getValue().toString();
                    String writerEmail = dataSnapshot1.child("writerEmail").getValue().toString();
                    String writerUid = dataSnapshot1.child("writerUid").getValue().toString();
                    String subject = dataSnapshot1.child("subject").getValue().toString();
                    String lessonId = dataSnapshot1.getKey();
                    long unitNo = (long) dataSnapshot1.child("unit").getValue();
                    long lessonNo = (long) dataSnapshot1.child("lesson").getValue();
                    lesson.setSubject(subject);
                    lesson.setWriterEmail(writerEmail);
                    lesson.setWriterName(writerName);
                    lesson.setWriterUid(writerUid);
                    lesson.setUnitNo(unitNo);
                    lesson.setLessonNo(lessonNo);
                    lesson.setTitle(title);
                    lesson.setContent(content);
                    lesson.setLessonId(lessonId);
                    lessonsList.add(lesson);
                }
                lessonsReady = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getActivity(), "فشل تحميل البينات من فضلك تأكد من الاتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "database error : " + databaseError);
            }
        });

    }

    public void suggestedLessonsButton(View view) {
        if (lessonsReady && lessonsList.size() != 0) {
            Intent intent = new Intent(this, AdminLessonContentActivity.class);
            intent.putParcelableArrayListExtra("lessonsList", lessonsList);
            intent.putExtra("index", 0);
            startActivity(intent);
            finish();
        } else if (!questionsReady) {
            Toast.makeText(this, "الدروس لم تجهز بعد", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "لا يوجد دروس حاليا", Toast.LENGTH_SHORT).show();
        }

    }


    public void suggestedQuestionsButton(View view) {
        if (questionsReady && questionList.size() != 0) {
            Intent intent = new Intent(this, AdminQuestionActivity.class);
            intent.putParcelableArrayListExtra("questionsList", questionList);
            intent.putExtra("index", 0);
            startActivity(intent);
            finish();
        } else if (!questionsReady) {
            Toast.makeText(this, "الاسئلة لم تجهز بعد", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "لا يوجد أسئلة حاليا", Toast.LENGTH_SHORT).show();
        }
    }


}


