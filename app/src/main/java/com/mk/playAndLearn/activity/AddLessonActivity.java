package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;

public class AddLessonActivity extends AppCompatActivity {
    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner, termSpinner, gradesSpinner;
    String correctAnswer = "";
    EditText lessonTitleET, lessonContentEt;
    Lesson lesson;
    String currentSubject = "", currentUserName, selectedUnit, selectedLesson, selectedTerm, selectedGrade;
    Map<String, Object> map;
    boolean oldLesson = false;
    String oldLessonId = "";
    WriteBatch batch;
    public SharedPreferences pref; // 0 - for private mode
    int selectedItemgrade = -1;
    int selectedItemtirm = -1;
    int selectedItemunite = -1;
    int selectedItemglesson = -1;
    int selectedItemlecture = -1;







    //TODO : fix the problems of signing in if exists
    //TODO : make push to github then revise the names to be used in the database well and change them when put and when get in all things then clear all the database before starting real use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
      //  ActionBar actionBar = getSupportActionBar();
        //final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
     //   upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
      //  getSupportActionBar().setHomeAsUpIndicator(upArrow);
      //  assert actionBar != null;
       // actionBar.setDisplayHomeAsUpEnabled(true);
     //   actionBar.setDisplayShowTitleEnabled(false);

        batch = fireStore.batch();

        Log.v("sharedPreference", " current userName is : " + currentUserName);

        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        gradesSpinner = findViewById(R.id.gradeSpinner);
        unitOrderSpinner = findViewById(R.id.unitOrderSpinner);
        lessonOrderSpinner = findViewById(R.id.lessonOrderSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        lessonTitleET = findViewById(R.id.lessonTitleEditText);
        lessonContentEt = findViewById(R.id.lessonContentEditText);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(unitOrderSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(800);

            Log.v("spinnerHeight", "try");
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.v("spinnerHeight", "catch , exception is : " + e);
        }


        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(lessonOrderSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(800);

            Log.v("spinnerHeight", "try");
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.v("spinnerHeight", "catch , exception is : " + e);
        }

        //setSpinners
      setUnitOrderSpinner( R.array.units_array);
       setLessonOrderSpinner(R.array.lessons_array);
      setTermSpinner( R.array.term_array);
       setGradeSpinner( R.array.grades_array);
        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setSubject spinner in onCreate");

        final Button addLessonBtn = findViewById(R.id.addLessonButton);
        addLessonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLesson(addLessonBtn);
            }
        });


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

            Log.v("lessonsLog", "term : " + term + " , subject : " + subject);

            oldLesson = true;
            oldLessonId = lesson.getLessonId();
        }

        lessonContentEt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.lessonContentEditText) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    void setSubjectsSpinner(int array, String tag) {

        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddLessonActivity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemlecture) {
                    v.setBackgroundColor(getResources().getColor(R.color.blue_white));

                    TextView tv = (TextView) v.findViewById(R.id.textView);

                    // Set the text color of spinner item
                    tv.setTextColor(Color.WHITE);


                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };
        subjectsSpinner.setAdapter(customAdapter);



        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemlecture  = i;

                unitOrderSpinner.setAlpha(1f);
                lessonOrderSpinner.setAlpha(1f);
                currentSubject = adapterView.getItemAtPosition(i).toString();
                if (!oldLesson) {
                    setLessonOrderSpinner(R.array.lessons_array);
                    switch (currentSubject) {
                        case "لغة انجليزية":
                            unitOrderSpinner.setEnabled(true);
                            unitOrderSpinner.setClickable(true);
                            lessonOrderSpinner.setEnabled(false);
                            lessonOrderSpinner.setClickable(false);
                            lessonOrderSpinner.setAlpha(0.5f);
                            setLessonOrderSpinner(R.array.lessons_array);
                            break;
                        case "لغة عربية: نحو":
                            lessonOrderSpinner.setEnabled(true);
                            lessonOrderSpinner.setClickable(true);
                            unitOrderSpinner.setEnabled(false);
                            unitOrderSpinner.setClickable(false);
                            unitOrderSpinner.setAlpha(0.5f);
                            setUnitOrderSpinner( R.array.units_array);
                            setLessonOrderSpinner(R.array.lessons_array);
                            break;
                        default:
                            unitOrderSpinner.setEnabled(true);
                            unitOrderSpinner.setClickable(true);
                            lessonOrderSpinner.setEnabled(true);
                            lessonOrderSpinner.setClickable(true);
                            setUnitOrderSpinner( R.array.units_array);
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

    void setUnitOrderSpinner(  int  array) {


        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddLessonActivity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemunite) {
                    v.setBackgroundColor(getResources().getColor(R.color.blue_white));

                    TextView tv = (TextView) v.findViewById(R.id.textView);

                    // Set the text color of spinner item
                    tv.setTextColor(Color.WHITE);


                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };

        unitOrderSpinner.setAdapter(customAdapter);

        unitOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemunite = i;
                selectedUnit = adapterView.getItemAtPosition(i).toString();
                if(selectedUnit.equals("الوحدة")){
                    selectedUnit = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setLessonOrderSpinner(int array) {


        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddLessonActivity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemglesson) {
                    v.setBackgroundColor(getResources().getColor(R.color.blue_white));

                    TextView tv = (TextView) v.findViewById(R.id.textView);

                    // Set the text color of spinner item
                    tv.setTextColor(Color.WHITE);


                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };

        lessonOrderSpinner.setAdapter(customAdapter);

        lessonOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemglesson  =  i;
                selectedLesson = adapterView.getItemAtPosition(i).toString();
                if(selectedLesson.equals("الدرس")){
                    selectedLesson = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setTermSpinner(int array) {

        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddLessonActivity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemtirm) {
                    v.setBackgroundColor(getResources().getColor(R.color.blue_white));

                    TextView tv = (TextView) v.findViewById(R.id.textView);

                    // Set the text color of spinner item
                    tv.setTextColor(Color.WHITE);


                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };
        termSpinner.setAdapter(customAdapter);




        termSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemtirm  = i;
              selectedTerm = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setGradeSpinner( int array) {

        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddLessonActivity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemgrade) {
                    v.setBackgroundColor(getResources().getColor(R.color.blue_white));

                    TextView tv = (TextView) v.findViewById(R.id.textView);

                    // Set the text color of spinner item
                    tv.setTextColor(Color.WHITE);


                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };
        gradesSpinner.setAdapter(customAdapter);




        gradesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                selectedItemgrade = i;
                selectedGrade = adapterView.getItemAtPosition(i).toString();
              /*  if (!oldLesson) {
                    if (selectedGrade.contains("الإعدادى")) {
                        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setGradeSpinner1");
                    } else if (selectedGrade.contains("الثانوى")) {
                        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setGradeSpinner2");
                    }
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void addLesson(View view) {
        String lessonTitle = lessonTitleET.getText().toString().trim();
        String lessonContent = lessonContentEt.getText().toString().trim();


        if (selectedGrade.equals("اختر الصف الدراسى")) {
            Toast.makeText(this, "برجاء تحديد الصف الدراسى", Toast.LENGTH_SHORT).show();
        } else if (selectedTerm.equals("اختر الفصل الدراسى")) {
            Toast.makeText(this, "برجاء تحديد الفصل الدراسى", Toast.LENGTH_SHORT).show();
        } else if (currentSubject.equals("اختر المادة")) {
            Toast.makeText(this, "من فضلك اختر المادة التي ينتمى لها هذا الدرس", Toast.LENGTH_SHORT).show();
        } else if (selectedUnit.equals("الوحدة") && !currentSubject.equals("لغة عربية: نحو")) {
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
                map.put("order", selectedUnit + selectedLesson);
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

                batch = fireStore.batch();

                batch.update(currentLessonReference, "title", lessonTitle.trim());
                batch.update(currentLessonReference, "content", lessonContent.trim());
                batch.update(currentLessonReference, "schoolType", schoolType);
                batch.update(currentLessonReference, "subject", currentSubject);
                batch.update(currentLessonReference, "grade", selectedGrade);
                batch.update(currentLessonReference, "unitNumber", selectedUnit);
                batch.update(currentLessonReference, "lessonNumber", selectedLesson);
                batch.update(currentLessonReference, "term", convertTermToLong(selectedTerm));
                batch.update(currentLessonReference,"order", selectedUnit + selectedLesson);

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(AddLessonActivity.this, "تم تحديث الدرس بنجاح", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddLessonActivity.this, "لم يتم تحديث الدرس", Toast.LENGTH_SHORT).show();
                            Log.v("editLessonLog", task.getException().getMessage() + " , " + task.getException().getCause().toString());
                        }
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

    public void finish(View view) {

        finish();
    }
}
