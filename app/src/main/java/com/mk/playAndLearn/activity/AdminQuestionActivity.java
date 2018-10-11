package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Collections;

import static com.mk.playAndLearn.utils.Firebase.questionsReference;
import static com.mk.playAndLearn.utils.Firebase.usersReference;

public class AdminQuestionActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    Question question;
    TextView tvQuestion, subjectTv, writerTv, answerTv, writerTypeTv;
    RadioGroup rg1;
    Button skipQuestionButton;
    String correctAnswer, selection;
    String writerType;
    RadioButton r1, r2, r3, r4;
    Intent i;
    ProgressBar timerProgressBar;

    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        i = new Intent(AdminQuestionActivity.this, QuestionResultActivity.class);

        rg1 = findViewById(R.id.radioGroup);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        answerTv = findViewById(R.id.answerTv);
        writerTypeTv = findViewById(R.id.writerTypeTv);
        tvQuestion = findViewById(R.id.questionText);
        r1 = findViewById(R.id.radio1);
        r2 = findViewById(R.id.radio2);
        r3 = findViewById(R.id.radio3);
        r4 = findViewById(R.id.radio4);
        timerProgressBar = findViewById(R.id.timerProgressbar);
        subjectTv = findViewById(R.id.subjectTv);
        writerTv = findViewById(R.id.writerNameTv);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            list = intent.getParcelableArrayListExtra("questionsList");
            index = intent.getIntExtra("index", -1);
        }
        if (index < list.size()) {
            question = (Question) list.get(index);
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
            if (question.getSubject() != null) {
                subjectTv.append(question.getSubject());
                Toast.makeText(this, "المادة : " + question.getSubject(), Toast.LENGTH_SHORT).show();
            } else {
                subjectTv.append("غير مكتوبة");
            }
            if (question.getSubject() != null) {
                writerTv.append(question.getWriterName());
            } else {
                writerTv.append("غير مكتوبة");
            }
        } else {
            Toast.makeText(this, "لا يوجد أسئلة أخري", Toast.LENGTH_SHORT).show();
        }

        skipQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipQuestion();
            }
        });
        usersReference.child(question.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                writerType = dataSnapshot.child("userType").getValue().toString();
                writerTypeTv.append(writerType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void skipQuestion() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("تنبيه هام");
        dialog.setMessage("هل تريد تخطى هذا السؤال؟");
        dialog.setPositiveButton("موافق", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                nextQuestion();
            }
        });

        dialog.create();
        dialog.show();
    }

    public void nextQuestion() {
        index++;
        Intent intent1 = new Intent(this, AdminQuestionActivity.class);
        intent1.putParcelableArrayListExtra("questionsList", list);
        intent1.putExtra("index", index);
        if (index < list.size()) {
            startActivity(intent1);
            finish();
        } else {
            Toast.makeText(this, "انتهت الاسئلة", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void showAnswer(View view) {
        if (rg1.getCheckedRadioButtonId() != -1) {
            int id = rg1.getCheckedRadioButtonId();
            View radioButton = rg1.findViewById(id);
            int radioId = rg1.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rg1.getChildAt(radioId);
            selection = (String) btn.getText();
        }
        if (selection != null && selection.equals(correctAnswer)) {
            answerTv.setTextColor(Color.GREEN);
        } else {
            answerTv.setTextColor(Color.RED);
        }
        answerTv.append(correctAnswer);

    }

    public void refuseQuestion(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("تنبيه هام");
        dialog.setMessage("هل أنت متأكد أنك تريد حذف هذا السؤال ؟");
        dialog.setPositiveButton("نعم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                usersReference.child(question.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int userRefusedQuestions = Integer.parseInt(dataSnapshot.child("refusedQuestions").getValue().toString());
                        usersReference.child(question.getWriterUid()).child("refusedQuestions").setValue(userRefusedQuestions + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                questionsReference.child(question.getQuestionId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        questionsReference.child(question.getQuestionId()).removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                composeEmail("تم رفض سؤالك", "تم رفض سؤالك " + "\"" + question.getAlQuestion() + "\"");
                Toast.makeText(AdminQuestionActivity.this, "تم رفض السؤال", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.create();
        dialog.show();
    }

    public void acceptQuestion(View view) {

        usersReference.child(question.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                usersReference.child(question.getWriterUid()).child("points").setValue(userPoints + 5);
                int userAcceptedQuestions = Integer.parseInt(dataSnapshot.child("acceptedQuestions").getValue().toString());
                usersReference.child(question.getWriterUid()).child("acceptedQuestions").setValue(userAcceptedQuestions + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        questionsReference.child(question.getQuestionId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                questionsReference.child(question.getQuestionId()).child("reviewed").setValue(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, "تم قبول السؤال", Toast.LENGTH_SHORT).show();
        if (writerType.equals("طالب")) {
            composeEmail("تم قبول سؤالك", "تم قبول سؤالك " + "\"" + question.getAlQuestion() + "\"" + " وسيتم زيادة نقطك 5 نقاط");
        } else {
            composeEmail("تم قبول سؤالك", "تم قبول سؤالك " + "\"" + question.getAlQuestion() + "\"");
        }
    }

    public void composeEmail(String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{question.getWriterEmail()});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivityForResult(Intent.createChooser(i, "Send mail..."), 0);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AdminQuestionActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            nextQuestion();
        }
    }

}
