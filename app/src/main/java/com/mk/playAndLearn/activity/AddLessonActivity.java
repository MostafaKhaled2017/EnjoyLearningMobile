package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.utils.DateClass;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;

public class AddLessonActivity extends AppCompatActivity {
    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner, termSpinner, gradesSpinner;
    String correctAnswer = "";
    EditText lessonTitleET, lessonContentEt;
    TextView unitOrderTv;
    Lesson lesson;
    String currentSubject = "", currentUserName, selectedUnit, selectedLesson, selectedTerm, selectedGrade;
    Map<String, Object> map;
    boolean oldLesson = false;
    String oldLessonId = "";
    WriteBatch batch;
    RelativeLayout unitOrderLayout;
    public SharedPreferences pref; // 0 - for private mode


    //TODO : fix the problems of signing in if exists
    //TODO : make push to github then revise the names to be used in the database well and change them when put and when get in all things then clear all the database before starting real use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("إضافة درس");

        batch = fireStore.batch();

        Log.v("sharedPreference", " current userName is : " + currentUserName);

        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        gradesSpinner = findViewById(R.id.gradeSpinner);
        unitOrderSpinner = findViewById(R.id.unitOrderSpinner);
        lessonOrderSpinner = findViewById(R.id.lessonOrderSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        unitOrderTv = findViewById(R.id.unitOrderTextView);
        unitOrderLayout = findViewById(R.id.unitOrderLayout);
        lessonTitleET = findViewById(R.id.lessonTitleEditText);
        lessonContentEt = findViewById(R.id.lessonContentEditText);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(unitOrderSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(300);

            Log.v("spinnerHeight", "try");
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.v("spinnerHeight", "catch , exception is : " + e);
        }

        //setSpinners
        setUnitOrderSpinner();
        setLessonOrderSpinner(R.array.lessons_array);
        setTermSpinner();
        setGradeSpinner();
        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setSubject spinner in onCreate");

        ButterKnife.bind(this);


        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(subjectsSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(500);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey("lesson")) {
            lesson = (Lesson) intent.getSerializableExtra("lesson");
            lessonTitleET.setText(lesson.getTitle());
            lessonContentEt.setText(lesson.getContent());


            String grade = lesson.getGrade(); //the value you want the position for
            ArrayAdapter myAdap = (ArrayAdapter) gradesSpinner.getAdapter(); //cast to an ArrayAdapter
            int spinnerPosition = myAdap.getPosition(grade);
            gradesSpinner.setSelection(spinnerPosition);

            String term = convertTermToString(lesson.getTerm()); //the value you want the position for
            myAdap = (ArrayAdapter) termSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(term);
            termSpinner.setSelection(spinnerPosition);

            String subject = lesson.getSubject(); //the value you want the position for
            myAdap = (ArrayAdapter) subjectsSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(subject);
            subjectsSpinner.setSelection(spinnerPosition);

            String unitNumber = lesson.getUnitNo(); //the value you want the position for
            myAdap = (ArrayAdapter) unitOrderSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(unitNumber);
            unitOrderSpinner.setSelection(spinnerPosition);

            String lessonNumber = lesson.getLessonNo(); //the value you want the position for
            myAdap = (ArrayAdapter) lessonOrderSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(lessonNumber);
            lessonOrderSpinner.setSelection(spinnerPosition);

            oldLesson = true;
            oldLessonId = lesson.getLessonId();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    void setSubjectsSpinner(int array, String tag) {
        ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
                if (!oldLesson) {
                    setLessonOrderSpinner(R.array.lessons_array);
                    switch (currentSubject) {
                        case "لغة انجليزية":
                            unitOrderSpinner.setEnabled(true);
                            unitOrderSpinner.setClickable(true);
                            lessonOrderSpinner.setEnabled(false);
                            lessonOrderSpinner.setClickable(false);
                            setLessonOrderSpinner(R.array.lessons_array);
                            break;
                        case "لغة عربية: نحو":
                            lessonOrderSpinner.setEnabled(true);
                            lessonOrderSpinner.setClickable(true);
                            unitOrderSpinner.setEnabled(false);
                            unitOrderSpinner.setClickable(false);
                            setUnitOrderSpinner();
                            setLessonOrderSpinner(R.array.lessons_array);
                            break;
                        default:
                            unitOrderSpinner.setEnabled(true);
                            unitOrderSpinner.setClickable(true);
                            lessonOrderSpinner.setEnabled(true);
                            lessonOrderSpinner.setClickable(true);
                            setUnitOrderSpinner();
                            setLessonOrderSpinner(R.array.lessons_array);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setUnitOrderSpinner() {
        ArrayAdapter<CharSequence> unitOrderAdapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        unitOrderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        unitOrderSpinner.setAdapter(unitOrderAdapter);

        unitOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedUnit = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setLessonOrderSpinner(int array) {
        ArrayAdapter<CharSequence> lessonsOrderAdapter = ArrayAdapter.createFromResource(this,
                array, android.R.layout.simple_spinner_item);
        lessonsOrderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        lessonOrderSpinner.setAdapter(lessonsOrderAdapter);

        lessonOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLesson = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setTermSpinner() {
        ArrayAdapter<CharSequence> termAdapter = ArrayAdapter.createFromResource(this,
                R.array.term_array, android.R.layout.simple_spinner_item);
        termAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        termSpinner.setAdapter(termAdapter);

        termSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTerm = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setGradeSpinner() {
        ArrayAdapter<CharSequence> gradesAdapter = ArrayAdapter.createFromResource(this,
                R.array.grades_array, android.R.layout.simple_spinner_item);
        gradesAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        gradesSpinner.setAdapter(gradesAdapter);

        gradesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGrade = adapterView.getItemAtPosition(i).toString();
                if (!oldLesson) {
                    if (selectedGrade.contains("الإعدادى")) {
                        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setGradeSpinner1");
                    } else if (selectedGrade.contains("الثانوى")) {
                        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setGradeSpinner2");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @OnClick(R.id.addLessonButton)
    public void addLesson(View view) {
        String lessonTitle = lessonTitleET.getText().toString().trim();
        String lessonContent = lessonContentEt.getText().toString().trim();


        if (selectedGrade.equals("اختر الصف الدراسى")) {
            Toast.makeText(this, "برجاء تحديد الصف الدراسى", Toast.LENGTH_SHORT).show();
        } else if (selectedTerm.equals("اختر الفصل الدراسى")) {
            Toast.makeText(this, "برجاء تحديد الفصل الدراسى", Toast.LENGTH_SHORT).show();
        } else if (currentSubject.equals("اختر المادة")) {
            Toast.makeText(this, "من فضلك اختر المادة التي ينتمى لها هذا الدرس", Toast.LENGTH_SHORT).show();
        } else if (selectedUnit.equals("الوحدة") && unitOrderLayout.getVisibility() == View.VISIBLE && !currentSubject.equals("لغة عربية: نحو")) {
            Toast.makeText(this, "برجاء تحديد الوحدة الحالية", Toast.LENGTH_SHORT).show();
        } else if (lessonOrderSpinner.getVisibility() == View.VISIBLE
                && (selectedLesson.equals("الدرس") || selectedLesson.equals("الفصل") || selectedLesson.equals("ترتيب الدرس")) && !currentSubject.equals("لغة انجليزية")) {
            Toast.makeText(this, "برجاء تحديد ترتيب الدرس", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(lessonContent)) {
            lessonTitleET.setError("من فضلك ادخل عنوان الدرس");
        } else if (TextUtils.isEmpty(lessonContent)) {
            lessonContentEt.setError("هذا الحقل إجبارى");
        } else {
            String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String schoolType = getSchoolType(currentSubject);

            if (!oldLesson) {
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String todayDate = format.format(today);

                DateClass dateClass = new DateClass();
                dateClass.setDate(today);

                currentUserName = getSavedName(this);

                map = new HashMap<>();

                map.put("grade", selectedGrade);
                map.put("unitNumber", selectedUnit);//Added
                map.put("lessonNumber", selectedLesson);//Added
                map.put("reviewed", false);
                map.put("schoolType", schoolType);
                map.put("subject", currentSubject);
                map.put("term", convertTermToLong(selectedTerm));//Added
                map.put("writerName", currentUserName);
                map.put("writerEmail", localCurrentUserEmail);
                map.put("writerUid", localCurrentUserUid);
                map.put("dayDate", todayDate);
                map.put("date", dateClass.getDate());
                map.put("title", lessonTitle);
                map.put("content", lessonContent);

                Toast.makeText(AddLessonActivity.this, "جارى رفع الدرس", Toast.LENGTH_SHORT).show();
                fireStoreLessons.document(selectedGrade).collection(currentSubject).add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(AddLessonActivity.this, "تم رفع الدرس بنجاح", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddLessonActivity.this, "فشلت محاولة إضافة الدرس برجاء المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                            Log.v("lessonUpload", task.getException().toString());
                        }

                    }
                });
                clearViews();

            } else if (oldLesson) {
                DocumentReference currentLessonReference = fireStoreLessons.document(lesson.getGrade()).collection(lesson.getSubject()).document(oldLessonId);

                batch.update(currentLessonReference, "title", lessonTitle.trim());
                batch.update(currentLessonReference, "content", lessonContent.trim());
                batch.update(currentLessonReference, "schoolType", schoolType);
                batch.update(currentLessonReference, "subject", currentSubject);
                batch.update(currentLessonReference, "grade", selectedGrade);
                batch.update(currentLessonReference, "unitNumber", selectedUnit);
                batch.update(currentLessonReference, "lessonNumber", selectedLesson);
                batch.update(currentLessonReference, "term", convertTermToLong(selectedTerm));

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AddLessonActivity.this, "تم تحديث الدرس بنجاح", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }
    }

    public void addAnswer(String answer) {
        if (answer != null && answer.length() > 0) {
            if (correctAnswer.length() == 0) {
                correctAnswer += answer;
            } else if (correctAnswer.length() > 0) {
                correctAnswer += "," + answer;
            }
        }
    }

    long convertTermToLong(String term) {
        switch (term) {
            case "الفصل الدراسى الأول":
                return 1;
            case "الفصل الدراسى الثانى":
                return 2;
            default:
                return -1;
        }
    }

    String convertTermToString(long term) {
        if (term == 1) {
            return "الفصل الدراسى الأول";
        } else if (term == 2) {
            return "الفصل الدراسى الثانى";
        } else {
            return "غير معروف";
        }
    }


    void clearViews() {
        lessonTitleET.setText("");
        lessonContentEt.setText("");

     /* gradesSpinner.setSelection(0);
        termSpinner.setSelection(0);
        subjectsSpinner.setSelection(0);
        languageBranchesSpinner.setSelection(0);
        unitOrderSpinner.setSelection(0);
        lessonOrderSpinner.setSelection(0);*/

        correctAnswer = "";

    }

    public static String getSchoolType(String subject) {
        if (subject.equals("فيزياء") || subject.equals("كيمياء")
                || subject.equals("أحياء") || subject.equals("رياضيات")
                || subject.equals("علوم")) {
            return "arabic";
        } else if (subject.equals("Physics") || subject.equals("Chemistry")
                || subject.equals("Biology") || subject.equals("Mathematics")
                || subject.equals("Science")) {
            return "languages";
        } else {
            return "both";
        }
    }
}
