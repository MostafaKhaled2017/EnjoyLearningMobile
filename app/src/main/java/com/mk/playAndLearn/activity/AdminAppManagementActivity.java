package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.model.Question;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.fireStoreComplaintsQuestions;
import static com.mk.playAndLearn.utils.Firebase.fireStoreGeneralChallenge;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.Firebase.fireStorePosts;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;

public class AdminAppManagementActivity extends AppCompatActivity {

    ArrayList questionsList = new ArrayList(), lessonsList = new ArrayList();
    boolean questionsReady = false;
    Button queryButton;
    ArrayList arabicQuestionsList = new ArrayList(), languagesQuestionsList = new ArrayList();
    final String arabicSchoolType = "arabic";
    final String languagesSchoolType = "languages";
    int studentsCount = 0, allUsersCount = 0;

    CollectionReference arabicQuestionsReference, languagesQuestionsReference;

    int counter;


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

        queryButton = findViewById(R.id.doQuery);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("إدارة التطبيق");

        DocumentReference document = fireStoreGeneralChallenge.document("generalChallengeDocument");
        arabicQuestionsReference = document.collection("arabicQuestions");
        languagesQuestionsReference = document.collection("languagesQuestions");
        Log.d("Loggingg", "arabicList size : " + arabicQuestionsList + " , languagesListSize : " + languagesQuestionsList);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void getSuggestedQuestions() {
        if (!questionsList.isEmpty())
            questionsList.clear();
        final String[] subjectsArray = getResources().getStringArray(R.array.subjects_array);
        for (final String subject : subjectsArray) {
            fireStoreQuestions.document(subject).collection(subject).whereEqualTo("reviewed", false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                        addQuestionData(document, null, null);
                    }
                    if (subject.equals(subjectsArray[subjectsArray.length - 1])) {
                        if (questionsList.size() > 0) {
                            Toast.makeText(AdminAppManagementActivity.this, "عدد الأسئلة : " + questionsList.size(), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AdminAppManagementActivity.this, AdminQuestionActivity.class);
                            intent.putParcelableArrayListExtra("questionsList", questionsList);
                            intent.putExtra("index", 0);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AdminAppManagementActivity.this, "لا يوجد أسئلة حاليا", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
        questionsReady = true;
    }

    public void complaintsQuestionsButton(View view) {
        Toast.makeText(this, "جارى إعداد أسئلة الشكاوى", Toast.LENGTH_SHORT).show();
        if (!questionsList.isEmpty())
            questionsList.clear();
        fireStoreComplaintsQuestions.whereEqualTo("complaintResolved", false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    counter = 0;
                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        final String complainantEmail = document.getString("ComplainantEmail");
                        final String questionId = document.getString("questionId");
                        final String reportId = document.getId();
                        String subject = document.getString("subject");
                        counter ++;
                        if (subject != null) {
                            fireStoreQuestions.document(subject).collection(subject).document(questionId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document = task2.getResult();
                                        addQuestionData(document, complainantEmail, reportId);
                                        Log.v("contestLogging", "list size : " + questionsList.size()
                                        + " , counter is : " + counter
                                        + " , result size : " + task.getResult().size());
                                        if (counter == task.getResult().size()) {
                                            Toast.makeText(AdminAppManagementActivity.this, "عدد الأسئلة : " + questionsList.size(), Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(AdminAppManagementActivity.this, AdminReportedQuestionActivity.class);
                                            intent.putParcelableArrayListExtra("questionsList", questionsList);
                                            intent.putExtra("index", 0);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
                else {
                    Log.v("contestLogging", task.getException().toString());
                }
            }
        });
    }


    public void suggestedQuestionsButton(View view) {
        Toast.makeText(this, "جارى تحميل الاسئلة", Toast.LENGTH_SHORT).show();
        getSuggestedQuestions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void challengesAndUsersMonitoring(View view) {
        startActivity(new Intent(this, AdminChallengesAndUsersMonitoring.class));
    }

    public void generalChallengeManagement(View view) {
        startActivity(new Intent(this, AdminGeneralChallengeManagement.class));
    }

    public void doQuery(View view) {
       /* final String[] subjectsArray = getResources().getStringArray(R.array.subjects_array);
        for (final String subject : subjectsArray) {
            fireStoreQuestions.document(subject).collection(subject).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                        String questionText = document.getString("alQuestion");
                        String answer1 = document.getString("answer1");
                        String answer2 = document.getString("answer2");
                        String answer3 = document.getString("answer3");
                        String answer4 = document.getString("answer4");
                        String subject = document.getString("subject");//extra than normal
                        String writerEmail = document.getString("writerEmail");//extra than normal
                        String correctAnswer = document.getString("correctAnswer");
                        String schoolType = document.getString("schoolType");
                        String writerName = document.getString("writerName");
                        String dayDate = document.getString("dayDate");
                        Boolean reviewed = document.getBoolean("reviewed");
                        Boolean challengeQuestion = document.getBoolean("challengeQuestion");
                        String writerUid = document.getString("writerUid");
                        String quesionId = document.getId();
                        String grade = "الصف الأول الثانوى";

                        Map<String, Object> map = new HashMap();
                        map.put("grade", grade);
                        map.put("unitNumber", null);//Added
                        map.put("lessonNumber", null);//Added
                        map.put("questionType", "choose"); // TODO : edit this when new type of questions added
                        map.put("reviewed", reviewed);
                        map.put("schoolType", schoolType);
                        map.put("subject", subject);
                        map.put("term", 1);//Added
                        map.put("writerName", writerName);
                        map.put("writerEmail", writerEmail);
                        map.put("writerUid", writerUid);
                        map.put("dayDate", dayDate);
                        map.put("challengeQuestion", challengeQuestion);

                        map.put("languageBranch", null);

                        map.put("alQuestion", questionText);
                        map.put("answer1",answer1);
                        map.put("answer2", answer2);
                        map.put("answer3", answer3);
                        map.put("answer4", answer4);
                        map.put("correctAnswer", correctAnswer);

                       // Log.v("transferingQuestions", "documentSnapShot is : " + document.toString());

                        if(subject != null) {
                            fireStoreQuestions.document(grade).collection(subject).document(quesionId).set(map);
                        } else {
                            Log.v("transferingQuestions", "document is : " + document.getData().toString());
                        }

                    }
                }
            });

        }*/
    }

    public void adjustGeneralChallengeQuestions(View view) {
        //TODO : comment this
        //start code for setting data to generalChallenge
        if (!arabicQuestionsList.isEmpty())
            arabicQuestionsList.clear();
        if (!languagesQuestionsList.isEmpty())
            languagesQuestionsList.clear();

        getChallengeQuestion("رياضيات", arabicSchoolType);

        getChallengeQuestion("Mathematics", languagesSchoolType);

        //end code for setting data to generalChallenge
    }

    public void deleteOldChallenges(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("هل انت متأكد أنك تريد حذف كل التحديات القديمة");
        dialog.setPositiveButton("نعم", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                fireStoreChallenges.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -1);
                        Date today = new Date();
                        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                        formatDate.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                        String todayDate = formatDate.format(today);
                        String yesterdayDate = formatDate.format(calendar.getTime());

                        for (DocumentSnapshot documentSnapshot : documentSnapshots.getDocuments()) {
                            String challengeDate = formatDate.format(documentSnapshot.get("date"));
                            ;
                            String challengeState = documentSnapshot.getString("state");

                            if (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText)) {
                                fireStoreChallenges.document(documentSnapshot.getId()).delete();
                            } else {
                                if (!challengeDate.equals(todayDate) && !challengeDate.equals(yesterdayDate)) {
                                    fireStoreChallenges.document(documentSnapshot.getId()).delete();
                                }
                            }

                        }

                        Toast.makeText(AdminAppManagementActivity.this, "انتهت عملية الحذف بنجاح", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.create();
        dialog.show();
    }

    public void getChallengeQuestion(final String subject, final String schoolType) {
        fireStoreQuestions.document(subject).collection(subject).whereEqualTo("challengeQuestion", true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                    Log.d("Loggingg", document.getData().toString());
                    Question question = new Question();
                    String questionText = document.getString("alQuestion");
                    String answer1 = document.getString("answer1");
                    String answer2 = document.getString("answer2");
                    String answer3 = document.getString("answer3");
                    String answer4 = document.getString("answer4");
                    String subject = document.getString("subject");
                    String correctAnswer = document.getString("correctAnswer");
                    String writerName = document.getString("writerName");
                    boolean reviewed = document.getBoolean("reviewed");
                    String localSchoolType = document.getString("schoolType");//TODO

                    Map<String, Object> map = new HashMap<>();
                    map.put("alQuestion", questionText);
                    map.put("answer1", answer1);
                    map.put("answer2", answer2);
                    map.put("answer3", answer3);
                    map.put("answer4", answer4);
                    map.put("subject", subject);
                    map.put("reviewed", reviewed);
                    map.put("correctAnswer", correctAnswer);

                    Log.d("Logginggg", "local school type is : " + schoolType + " , map is : " + map.toString());

                    if (localSchoolType.equals(arabicSchoolType)) {
                        arabicQuestionsReference.add(map);
                    } else if (localSchoolType.equals(languagesSchoolType)) {
                        languagesQuestionsReference.add(map);
                    } else if (localSchoolType.equals("both")) {
                        arabicQuestionsReference.add(map);
                        languagesQuestionsReference.add(map);
                    }
                }
            }
        });
    }

    public void countUsers(View view) {
        studentsCount = 0;
        allUsersCount = 0;

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String userType = "";
                    if (dataSnapshot1.child("userType").getValue() != null) {
                        userType = dataSnapshot1.child("userType").getValue().toString();
                    }

                    allUsersCount++;

                    if (userType.equals("طالب")) {
                        studentsCount++;
                    }
                }
                Toast.makeText(AdminAppManagementActivity.this, "عدد الطلاب : " + allUsersCount + " (" + studentsCount + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void addQuestionData(DocumentSnapshot document, String complainantEmail, String reportId) {
        Question question = new Question();
        String questionText = document.getString("alQuestion");
        String answer1 = document.getString("answer1");
        String answer2 = document.getString("answer2");
        String answer3 = document.getString("answer3");
        String answer4 = document.getString("answer4");
        String subject = document.getString("subject");//extra than normal
        String writerEmail = document.getString("writerEmail");//extra than normal
        String correctAnswer = document.getString("correctAnswer");
        String writerName = document.getString("writerName");
        String writerUid = document.getString("writerUid");
        String grade = document.getString("grade");
        String questionId = document.getId();

        question.setGrade(grade);
        question.setAnswer1(answer1);
        question.setAnswer2(answer2);
        question.setAnswer3(answer3);
        question.setAnswer4(answer4);
        question.setCorrectAnswer(correctAnswer);
        question.setWriterName(writerName);
        question.setQuestionId(questionId);
        question.setWriterUid(writerUid);
        question.setAlQuestion(questionText);
        question.setQuestionId(document.getId());
        question.setSubject(subject);//extra than normal
        question.setWriterEmail(writerEmail);//extra than normal
        if (complainantEmail != null)
            question.setComplainantEmail(complainantEmail);
        if(reportId != null)
            question.setReportId(reportId);

        questionsList.add(0, question);
    }
}


