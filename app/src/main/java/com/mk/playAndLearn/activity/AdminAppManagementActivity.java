package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.model.Question;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Firebase.lessonsReference;
import static com.mk.playAndLearn.utils.Firebase.questionsReference;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;

public class AdminAppManagementActivity extends AppCompatActivity {

    ArrayList questionList = new ArrayList(), lessonsList = new ArrayList();
    boolean questionsReady = false, lessonsReady = false;
    ValueEventListener questionsListener, lessonsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_app_management);
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
        toolbarTitle.setText("إدارة التطبيق");

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
        questionsListener = questionsReference.orderByChild("reviewed").equalTo(false).addValueEventListener(new ValueEventListener() {
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

                    questionList.add(0, question);
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
        lessonsListener = lessonsReference.orderByChild("reviewed").equalTo(false).addValueEventListener(new ValueEventListener() {
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
            Toast.makeText(this, "عدد الدروس : " + lessonsList.size(), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(this, "عدد الأسئلة : " + questionList.size(), Toast.LENGTH_SHORT).show();

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

    public void removeListeners() {
        if (questionsListener != null)
            questionsReference.removeEventListener(questionsListener);

        if (lessonsListener != null)
            lessonsReference.removeEventListener(lessonsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    public void challengesAndUsersMonitoring(View view) {
        startActivity(new Intent(this, AdminChallengesAndUsersMonitoring.class));
    }

    public void generalChallengeManagement(View view) {
        startActivity(new Intent(this, AdminGeneralChallengeManagement.class));
    }

    public void doQuery(View view) {
        //TODO : comment this part

        //start editing in database
/*
        usersReference.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    usersReference.child(dataSnapshot1.getKey()).child("online").setValue(false);
                }

                Toast.makeText(AdminAppManagementActivity.this, "انتهي الاستعلام فى قاعدة البيانات", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

*/
        //end editing in database
    }

    public void adjustGeneralChallengeQuestions(View view) {

        //TODO : comment this
        //start code for setting data to generalChallenge

/*
        list = new ArrayList();
        final String schoolType = "arabic";
        //final String schoolType = "languages";
        if (!list.isEmpty())
            list.clear();
        questionsReference.orderByChild("challengeQuestion").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Question question = new Question();
                    String questionText = dataSnapshot1.child("al question").getValue().toString();
                    String answer1 = dataSnapshot1.child("answer 1").getValue().toString();
                    String answer2 = dataSnapshot1.child("answer 2").getValue().toString();
                    String answer3 = dataSnapshot1.child("answer 3").getValue().toString();
                    String answer4 = dataSnapshot1.child("answer 4").getValue().toString();
                    String correctAnswer = dataSnapshot1.child("correctAnswer").getValue().toString();
                    String writerName = dataSnapshot1.child("writerName").getValue().toString();
                    boolean reviewed = ((boolean) dataSnapshot1.child("reviewed").getValue());
                    String localSchoolType = dataSnapshot1.child("schoolType").getValue().toString();//TODO

                    if (localSchoolType.equals(schoolType) || localSchoolType.equals("both")) {
                        question.setAlQuestion(questionText);
                        question.setAnswer1(answer1);
                        question.setAnswer2(answer2);
                        question.setAnswer3(answer3);
                        question.setAnswer4(answer4);
                        question.setReviewed(reviewed);
                        question.setCorrectAnswer(correctAnswer);

                        list.add(question);

                    }
                }
                DatabaseReference localGeneralChallengesReference = FirebaseDatabase.getInstance().getReference("generalChallenge");

                Log.v("Logging", "list size is : " + list.size());

                Toast.makeText(this, "انتهى إعداد أسئلة التحدى العام", Toast.LENGTH_SHORT).show();

                if(schoolType.equals("arabic")){
                    localGeneralChallengesReference.child("arabicQuestions").setValue(list);
                }
                if(schoolType.equals("languages")){
                    localGeneralChallengesReference.child("languagesQuestions").setValue(list);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/

        //end code for setting data to generalChallenge
    }

    public void deleteOldChallenges(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("هل انت متأكد أنك تريد حذف كل التحديات القديمة");
        dialog.setPositiveButton("نعم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                challengesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -1);
                        Date today = new Date();
                        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                        formatDate.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                        String todayDate = formatDate.format(today);
                        String yesterdayDate = formatDate.format(calendar.getTime());

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            String challengeDate = dataSnapshot1.child("date").getValue().toString();
                            String challengeState = dataSnapshot1.child("state").getValue().toString();

                            if (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText)) {
                                challengesReference.child(dataSnapshot1.getKey()).removeValue();
                            } else {
                                if (!challengeDate.equals(todayDate) && !challengeDate.equals(yesterdayDate)) {
                                    challengesReference.child(dataSnapshot1.getKey()).removeValue();
                                }
                            }

                        }

                        Toast.makeText(AdminAppManagementActivity.this, "انتهت عملية الحذف بنجاح", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        dialog.create();
        dialog.show();
    }

}


