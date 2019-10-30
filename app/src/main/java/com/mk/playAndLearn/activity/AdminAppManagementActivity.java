package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.fireStoreComplaintsQuestions;
import static com.mk.playAndLearn.utils.Firebase.fireStoreGeneralChallenge;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;

public class AdminAppManagementActivity extends AppCompatActivity {

    ArrayList questionsList = new ArrayList(), lessonsList = new ArrayList();
    boolean questionsReady = false;
    Button queryButton;
    ArrayList arabicQuestionsList = new ArrayList(), languagesQuestionsList = new ArrayList();
    final String arabicSchoolType = "arabic";
    final String languagesSchoolType = "languages";
    String currentSubjectInSpinner = "";
    int studentsCount = 0, allUsersCount = 0;

    CollectionReference arabicQuestionsReference, languagesQuestionsReference;

    int counter, usersCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_app_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
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

        showSpinnerDialog();
    }

    public void showSpinnerDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);//TODO : check this
        android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog_with_subject_spinner, null);

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("تم", null)
                .create();

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        Spinner spinner = view.findViewById(R.id.subjectsSpinnerInDialog);
        dialogTitle.setText("اختر المادة");
        inputComment.setVisibility(View.GONE);


        final ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.preparatory_subjects_array_for_upload, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(subjectsAdapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubjectInSpinner = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Toast.makeText(AdminAppManagementActivity.this, "جارى تحميل الاسئلة", Toast.LENGTH_SHORT).show();
                        final String[] gradesArray = getResources().getStringArray(R.array.grades_array);
                        for (final String grade : gradesArray) {
                            if (currentSubjectInSpinner.equals("كل المواد")) {
                                final String[] subjectsArray = getResources().getStringArray(R.array.preparatory_subjects_array_for_upload);
                                for (final String subject : subjectsArray) {
                                    fireStoreQuestions.document(grade).collection(subject).whereEqualTo("reviewed", false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot documentSnapshots) {
                                            for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                                addQuestionData(document, null, null);
                                            }

                                            if (subject.equals(subjectsArray[subjectsArray.length - 1]) && grade.equals(gradesArray[gradesArray.length - 1])) {
                                                Log.v("subjectsLogging", "subject is : " + subject + " , grade is : " + grade);
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
                            } else {
                                fireStoreQuestions.document(grade).collection(currentSubjectInSpinner).whereEqualTo("reviewed", false).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot documentSnapshots) {
                                        for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                                            addQuestionData(document, null, null);
                                        }

                                        if (grade.equals(gradesArray[gradesArray.length - 1])) {
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
                        }
                    }
                });

            }
        });

        alertDialogBuilderUserInput.show();

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
                        final String grade = document.getString("grade");
                        final String reportId = document.getId();
                        String subject = document.getString("subject");
                        counter++;
                        if (subject != null && grade != null) {
                            fireStoreQuestions.document(grade).collection(subject).document(questionId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document = task2.getResult();
                                        if (document.exists()) {
                                            addQuestionData(document, complainantEmail, reportId);
                                        }
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
                } else {
                    Log.v("contestLogging", task.getException().toString());
                }
            }
        });
    }


    public void suggestedQuestionsButton(View view) {
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

        Toast.makeText(AdminAppManagementActivity.this, "جارى حصر الدروس", Toast.LENGTH_SHORT).show();
        final String[] gradesArray = getResources().getStringArray(R.array.grades_array);
        for (final String grade : gradesArray) {

            if(grade.equals(gradesArray[0]))
                continue;

            Log.w("lessonsCountLog", "grade is : " + grade);

            final String[] subjectsArray = getResources().getStringArray(R.array.preparatory_subjects_array_for_upload);
            for (final String subject : subjectsArray) {
                if(subject.equals(subjectsArray[0]))
                    continue;
                fireStoreLessons.document(grade)
                        .collection(subject)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        long subjectCounter = 0;

                        int totalNoOfLessons = documentSnapshots.size();

                        Log.w("lessonsCountLog", "Total number of Lessons of subject " + subject + " in grade "
                                + grade + " is " + totalNoOfLessons);

                        for (DocumentSnapshot document : documentSnapshots.getDocuments()) {

                                String unitNumber = document.getString("unitNumber");
                                String lessonNumber = document.getString("lessonNumber");
                                String lessonId = document.getId();

                                Map<String, Object> map = new HashMap<>();
                                map.put("order", unitNumber + lessonNumber);
                                fireStoreLessons.document(grade).collection(subject).document(lessonId).update(map);

                        }

                        Log.w("lessonsCountLog", "\n\n Lessons of subject " + subject + " in grade " + grade + " is \"" + subjectCounter + "\" \n\n");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminAppManagementActivity.this, "Failed to load the data : " + e.getCause().toString() + " , "
                                + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }

      /*  DatabaseReference localUsersReference = FirebaseDatabase.getInstance().getReference("users");

        usersCounter = 0;
        localUsersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshotRoot) {
                for(DataSnapshot dataSnapshot: dataSnapshotRoot.getChildren()) {
                    String userId = dataSnapshot.getKey();
                    long refusedQuestions = (long) dataSnapshot.child("refusedQuestions").getValue();
                    long acceptedQuestions = (long) dataSnapshot.child("acceptedQuestions").getValue();
                    String name = (String) dataSnapshot.child("userName").getValue();
                    String imageUrl = (String) dataSnapshot.child("userImage").getValue();
                    String email = (String) dataSnapshot.child("userEmail").getValue();
                    String type = (String) dataSnapshot.child("userType").getValue();
                    String grade = (String) dataSnapshot.child("grade").getValue();
                    String gender = (String) dataSnapshot.child("gender").getValue();
                    String currentUserUid = (String) dataSnapshot.child("uid").getValue();
                    String schoolType = (String) dataSnapshot.child("userSchoolType").getValue();
                    long points = (long) dataSnapshot.child("points").getValue();

                    Map<String, Object> map = new HashMap<>();
                    map.put("refusedQuestions", refusedQuestions);
                    map.put("acceptedQuestions", acceptedQuestions);
                    map.put("userName", name.trim());
                    map.put("userImage", imageUrl);
                    map.put("userEmail", email.trim());
                    map.put("userType", type);
                    map.put("pointsHistory", "");
                    map.put("grade", grade);
                    map.put("gender", gender);
                    map.put("uid", currentUserUid);
                    map.put("friends", "users: ");
                    map.put("points", points);
                    map.put("userSchoolType", schoolType);

                    fireStoreUsers.document(userId).set(map);

                    usersCounter ++;
                    if(usersCounter == dataSnapshotRoot.getChildrenCount()){
                        Toast.makeText(AdminAppManagementActivity.this, "Query ended", Toast.LENGTH_SHORT).show();
                        Log.v("queryLogging", "query ended , user count is : " + usersCounter);
                    }
                }
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
        long term = document.getLong("term");
        String unitNumber = document.getString("unitNumber");
        String lessonNumber = document.getString("lessonNumber");
        String languageBranch = document.getString("languageBranch");

        question.setTerm(term);
        question.setUnitNumber(unitNumber);
        question.setLessonNumber(lessonNumber);
        question.setLanguageBranch(languageBranch);
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
        question.setReportId(reportId);
        question.setSubject(subject);//extra than normal
        question.setWriterEmail(writerEmail);//extra than normal
        if (complainantEmail != null)
            question.setComplainantEmail(complainantEmail);

        Log.v("dataLogging", "question data is : " + document.getData().toString());


        questionsList.add(0, question);
    }

    public void leaderBoard(View view) {
        startActivity(new Intent(this, TestLeaderboardActivity.class));
    }
}


