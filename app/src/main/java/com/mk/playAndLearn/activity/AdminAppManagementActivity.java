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
import static com.mk.playAndLearn.utils.Firebase.fireStoreGeneralChallenge;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;

public class AdminAppManagementActivity extends AppCompatActivity {

    ArrayList questionList = new ArrayList(), lessonsList = new ArrayList();
    boolean questionsReady = false, lessonsReady = false;
    Button queryButton;
    ArrayList arabicQuestionsList = new ArrayList(), languagesQuestionsList = new ArrayList();
    final String arabicSchoolType = "arabic";
    final String languagesSchoolType = "languages";

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
        String[] subjectsArray = getResources().getStringArray(R.array.subjects_array);
        for (String subject : subjectsArray) {
            fireStoreQuestions.document(subject).collection(subject).whereEqualTo("reviewed", false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
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
                        String questionId = document.getId();

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

                        questionList.add(0, question);
                    }
                }
            });

        }
        questionsReady = true;
    }

    public void getSuggestedLessons() {
        if (!lessonsList.isEmpty())
            lessonsList.clear();

        String[] subjectsArray = getResources().getStringArray(R.array.subjects_array);
        for (String subject : subjectsArray) {
            fireStoreLessons.document(subject).collection(subject).whereEqualTo("reviewed", false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots) {
                    for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                        Lesson lesson = new Lesson();
                        String title = document.getString("title");
                        String content = document.getString("content");
                        String writerName = document.getString("writerName");
                        String writerEmail = document.getString("writerEmail");
                        String writerUid = document.getString("writerUid");
                        String subject = document.getString("subject");
                        String position = document.getString("position");
                        String lessonId = document.getId();
                        lesson.setSubject(subject);
                        lesson.setWriterEmail(writerEmail);
                        lesson.setWriterName(writerName);
                        lesson.setWriterUid(writerUid);
                        lesson.setPosition(position);
                        lesson.setTitle(title);
                        lesson.setContent(content);
                        lesson.setLessonId(lessonId);
                        lessonsList.add(0, lesson);
                    }
                }
            });

        }
        lessonsReady = true;
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

        usersReference.orderByChild("online").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    Log.v("usersPresuneceLog", "datasnapshot is : " + dataSnapshot1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //start editing in database

/*
        fireStorePosts.document("posts").collection("posts").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot dataSnapshot1 : documentSnapshots.getDocuments()) {
                    String postContent = (String) dataSnapshot1.getString("content");
                    String postDate = (String) dataSnapshot1.getString("date");
                    String postWriter = (String) dataSnapshot1.getString("writerName");
                    String postWriterEmail = (String) dataSnapshot1.getString("email");
                    String postImage = (String) dataSnapshot1.getString("image");
                    String subject = (String) dataSnapshot1.getString("subject");
                    String postWriterUid = (String) dataSnapshot1.getString("writerUid");
                    boolean posted = (boolean) dataSnapshot1.getBoolean("posted");

                    Map<String, Object> map = new HashMap<>();
                    map.put("content", postContent);
                    map.put("date", postDate);
                    map.put("writerName", postWriter);
                    map.put("subject", subject);
                    map.put("writerUid", postWriterUid);
                    map.put("image", postImage);
                    map.put("email", postWriterEmail);
                    map.put("upVotedUsers", "users: ");
                    map.put("downVotedUsers", "users: ");
                    map.put("posted", posted);
                    map.put("votes", 0);

                    fireStorePosts.add(map);
                }
            };
            });
*/


/*
        fireStorePosts.document("posts").collection("posts").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("upVotedUsers", "users: ");
                    updates.put("downVotedUsers", "users: ");

                    DocumentReference currentPost = fireStorePosts.document("posts").collection("posts").document(document.getId());
                    currentPost.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(AdminAppManagementActivity.this, "انتهت التحديثات بنجاح", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
*/



        /*usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Log.v("Logging", "datasnapshot1 is : " + dataSnapshot1.toString());
                    long generalChallengePoints = -1000;
                    if(dataSnapshot1.child("lastGeneralChallengeScore").getValue() != null)
                        generalChallengePoints = (long) dataSnapshot1.child("lastGeneralChallengeScore").getValue();
                    if(generalChallengePoints == 100) {
                        usersReference.child(dataSnapshot1.getKey()).child("lastGeneralChallengeScore").setValue(0);
                        Toast.makeText(AdminAppManagementActivity.this, "uid is : " + dataSnapshot1.getKey(), Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(AdminAppManagementActivity.this, "انتهى الاستعلام", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
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

}


