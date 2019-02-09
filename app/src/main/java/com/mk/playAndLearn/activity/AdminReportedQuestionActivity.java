package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Collections;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreComplaintsQuestions;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;

public class AdminReportedQuestionActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    Question question;
    TextView tvQuestion, subjectTv, writerTv, answerTv, writerTypeTv;
    RadioGroup rg1;
    Button skipQuestionButton;
    String correctAnswer, selection;
    String writerType = "غير معروف";
    RadioButton r1, r2, r3, r4;
    Intent i;
    ProgressBar timerProgressBar;
    WriteBatch batch;
    DocumentReference currentQuestionReference;

    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reported_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        i = new Intent(AdminReportedQuestionActivity.this, QuestionResultActivity.class);

        batch = fireStore.batch();


        rg1 = findViewById(R.id.radioGroup);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        answerTv = findViewById(R.id.answerTv);
        writerTypeTv = findViewById(R.id.gradeTvInQuestions);
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
            currentQuestionReference =  fireStoreQuestions.document(question.getSubject()).collection(question.getSubject()).document(question.getQuestionId());
            correctAnswer = question.getCorrectAnswer();

            tvQuestion.setText(question.getAlQuestion());
            ArrayList<String> answers = new ArrayList<>();
            answers.add(question.getAnswer1());
            answers.add(question.getAnswer2());
            answers.add(question.getAnswer3());
            Collections.shuffle(answers);
            answers.add(question.getAnswer4());

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
        Intent intent1 = new Intent(this, AdminReportedQuestionActivity.class);
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

    public void finishReport(View view) {
        fireStoreComplaintsQuestions.document(question.getReportId()).update("complaintResolved", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(AdminReportedQuestionActivity.this, "تم إنهاء الشكوى", Toast.LENGTH_SHORT).show();
                nextQuestion();
            }
        });
    }

    public void complainant(View view) {
        composeEmail(question.getComplainantEmail(),"تم مراجعة السؤال الذي قدمت به شكوى", "تم مراجعة السؤال الذي قدمت به شكوى " + "\"" + question.getAlQuestion() + "\"");
    }

    public void questionUploader(View view) {
        composeEmail(question.getWriterEmail(),"تم تقديم شكوي في سؤالك", "تم تقديم شكوي في سؤالك " + "\"" + question.getAlQuestion() + "\"");
    }

    public void composeEmail(String email, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AdminReportedQuestionActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void editQuestion(View view) {
        Intent i = new Intent(this, AddQuestionActivity.class);
        i.putExtra("question", question);
        startActivity(i);
    }
}
