package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.mk.playAndLearn.activity.AddQuestionActivity.getSchoolType;
import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.Firebase.generalChallengeArabicQuestionsRef;
import static com.mk.playAndLearn.utils.Firebase.generalChallengeLanguageQuestionsRef;

public class AdminQuestionActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    Question question;
    TextView tvQuestion, subjectTv, writerTv, answerTv, gradeTv;
    Button skipQuestionButton;
    String correctAnswer, selection;
    String writerType = "غير معروف";
    CheckBox c1, c2, c3, c4, generalChallengeCheckBox;
    Intent i;
    ProgressBar timerProgressBar;
    WriteBatch batch;
    DocumentReference currentQuestionReference;
    String[] correctAnswers;

    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        i = new Intent(AdminQuestionActivity.this, QuestionResultActivity.class);

        batch = fireStore.batch();


        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        answerTv = findViewById(R.id.answerTv);
        gradeTv = findViewById(R.id.gradeTvInQuestions);
        tvQuestion = findViewById(R.id.questionText);
        c1 = findViewById(R.id.checkBox1InAdminQuestion);
        c2 = findViewById(R.id.checkBox2InAdminQuestion);
        c3 = findViewById(R.id.checkBox3InAdminQuestion);
        c4 = findViewById(R.id.checkBox4InAdminQuestion);
        generalChallengeCheckBox = findViewById(R.id.generalChallengeCheckBox);
        timerProgressBar = findViewById(R.id.timerProgressbar);
        subjectTv = findViewById(R.id.subjectTv);
        writerTv = findViewById(R.id.writerNameTvInQuestion);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            list = intent.getParcelableArrayListExtra("questionsList");
            index = intent.getIntExtra("index", -1);
        }
        if (index < list.size()) {
            question = (Question) list.get(index);
            currentQuestionReference = fireStoreQuestions.document(question.getGrade()).collection(question.getSubject()).document(question.getQuestionId());
            correctAnswer = question.getCorrectAnswer();

            tvQuestion.setText(question.getAlQuestion());
            //store answers in array
            ArrayList<String> answers = new ArrayList<>();
            answers.add(question.getAnswer1());
            answers.add(question.getAnswer2());

            if (question.getAnswer3().length() > 0)
                answers.add(question.getAnswer3());

            Collections.shuffle(answers);

            if (question.getAnswer4().length() > 0)
                answers.add(question.getAnswer4());//because some question there last option is things like "all above"

            setAnswersData(c1, c2, c3, c4, answers);

            correctAnswers = correctAnswer.split(",");


            if (question.getSubject() != null) {
                subjectTv.append(question.getSubject());
                Toast.makeText(this, "المادة : " + question.getSubject(), Toast.LENGTH_SHORT).show();
            } else {
                subjectTv.append("غير مكتوبة");
            }

            if (question.getWriterName() != null) {
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

        gradeTv.append(question.getGrade());

       /* fireStoreUsers.document(question.getWriterUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    writerType = document.getString("userType");
                    if(writerType == null)
                        writerType = "غير معروف";

                    gradeTv.append(writerType);
                }
                else {
                    Toast.makeText(AdminQuestionActivity.this, "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت و أعد المحاولة", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    void setAnswersData(CheckBox v1, CheckBox v2, CheckBox v3, CheckBox v4, ArrayList<String> answers) {
        Log.v("answersLogging", "size is : " + answers.size());

        v1.setText(answers.get(0).trim());
        v2.setText(answers.get(1).trim());
        if (answers.size() > 2) {
            v3.setText(answers.get(2).trim());
        } else {
            v3.setVisibility(View.GONE);
        }
        if (answers.size() > 3) {
            v4.setText(answers.get(3).trim());
        } else {
            v4.setVisibility(View.GONE);
        }
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

        String[] userAnswers = getUserAnswers().split(",");

        if (correctAnswers.length == 1) {
            if (userAnswers[0] != null && userAnswers[0].trim().equals(correctAnswer.trim())) {
                answerTv.setTextColor(Color.GREEN);
            } else {
                answerTv.setTextColor(Color.RED);
            }
        } else if (correctAnswers.length > 1) {

            Arrays.sort(userAnswers);
            Arrays.sort(correctAnswers);

            if (Arrays.equals(userAnswers, correctAnswers)) {
                answerTv.setTextColor(Color.GREEN);
            } else {
                answerTv.setTextColor(Color.RED);
            }
        }


        answerTv.setText("الإجابة : " + correctAnswer);

    }


    private String getUserAnswers() {
        String answers = "";
        if (c1.isChecked()) {
            answers += addAnswer((String) c1.getText(), answers.length());
        }
        if (c2.isChecked()) {
            answers += addAnswer((String) c2.getText(), answers.length());
        }
        if (c3.isChecked()) {
            answers += addAnswer((String) c3.getText(), answers.length());
        }
        if (c4.isChecked()) {
            answers += addAnswer((String) c4.getText(), answers.length());
        }
        return answers;
    }


    public String addAnswer(String answer, int length) {
        if (length == 0) {
            return answer;
        } else {
            return "," + answer;
        }
    }

    public void refuseQuestion(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("تنبيه هام");
        dialog.setMessage("هل أنت متأكد أنك تريد حذف هذا السؤال ؟");
        dialog.setPositiveButton("نعم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final DocumentReference currentUserReference = fireStoreUsers.document(question.getWriterUid());

                fireStore.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(currentUserReference);
                        long newRefusedQuestions = snapshot.getLong("refusedQuestions") + 1;
                        transaction.update(currentUserReference, "refusedQuestions", newRefusedQuestions);
                        transaction.delete(currentQuestionReference);
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Transaction success!");
                        composeEmail("تم رفض سؤالك", "تم رفض سؤالك " + "\"" + question.getAlQuestion() + "\"");
                        Toast.makeText(AdminQuestionActivity.this, "تم رفض السؤال", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Transaction failure.", e);
                    }
                });
            }
        });
        dialog.create();
        dialog.show();
    }

    public void acceptQuestion(View view) {
        Button button = findViewById(R.id.acceptQuestionBtn);
        button.setClickable(false);

        final DocumentReference currentUserReference = fireStoreUsers.document(question.getWriterUid());

        fireStore.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(currentUserReference);

                long newPoints = snapshot.getLong("points") + 5;
                long newAcceptedQuestions = snapshot.getLong("acceptedQuestions") + 1;

                transaction.update(currentUserReference, "points", newPoints);
                transaction.update(currentUserReference, "acceptedQuestions", newAcceptedQuestions);
                transaction.update(currentQuestionReference, "reviewed", true);

                if (generalChallengeCheckBox.isChecked()) {
                    transaction.update(currentQuestionReference, "challengeQuestion", true);

                    String schoolType = getSchoolType(question.getSubject());

                    if (schoolType.equals("arabic")) {
                        transaction.set(generalChallengeArabicQuestionsRef.document(question.getQuestionId()), question);
                    } else if (schoolType.equals("languages")) {
                        transaction.set(generalChallengeLanguageQuestionsRef.document(question.getQuestionId()), question);
                    } else {
                        transaction.set(generalChallengeArabicQuestionsRef.document(question.getQuestionId()), question);
                        transaction.set(generalChallengeLanguageQuestionsRef.document(question.getQuestionId()), question);
                    }
                }

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "Transaction success!");
                Toast.makeText(AdminQuestionActivity.this, "تم قبول السؤال", Toast.LENGTH_SHORT).show();
                if (writerType.equals("طالب")) {
                    composeEmail("تم قبول سؤالك", "تم قبول سؤالك " + "\"" + question.getAlQuestion() + "\"" + " وسيتم زيادة نقطك 5 نقاط");
                } else {
                    composeEmail("تم قبول سؤالك", "تم قبول سؤالك " + "\"" + question.getAlQuestion() + "\"");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Transaction failure.", e);
            }
        });

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

    public void editQuestion(View view) {
        Intent i = new Intent(this, AddQuestionActivity.class);
        i.putExtra("question", question);
        startActivity(i);
    }
}
