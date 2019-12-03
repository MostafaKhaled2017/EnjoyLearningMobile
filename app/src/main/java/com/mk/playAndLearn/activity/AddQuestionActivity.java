package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.spinnercustom.CustomAdapter;
import com.mk.playAndLearn.utils.DateClass;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;

public class AddQuestionActivity extends AppCompatActivity  {
    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner, termSpinner, gradesSpinner;
    String correctAnswer = "";
    EditText editText1, editText2, editText3, editText4, questionEt;
    Question question;
    String currentSubject = "", currentUserName, selectedUnit, selectedLesson, selectedTerm, selectedGrade;
    Map<String, Object> map;
    int currentCheckedRadioButton;
    boolean oldQuestion = false;
    String oldQuestionId = "";
    WriteBatch batch;
    CheckBox c1, c2, c3, c4;
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
        setContentView(R.layout.activity_add_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
       // final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
     //   upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
     //   getSupportActionBar().setHomeAsUpIndicator(upArrow);
    /*    assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);*/

        batch = fireStore.batch();

        Log.v("sharedPreference", " current userName is : " + currentUserName);

        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        gradesSpinner = findViewById(R.id.gradeSpinner);
        unitOrderSpinner = findViewById(R.id.unitOrderSpinner);
        lessonOrderSpinner = findViewById(R.id.lessonOrderSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        editText1 = findViewById(R.id.et1);
        editText2 = findViewById(R.id.et2);
        editText3 = findViewById(R.id.et3);
        editText4 = findViewById(R.id.et4);
        questionEt = findViewById(R.id.addQuestionEditText);
        c1 = findViewById(R.id.checkbox1);
        c2 = findViewById(R.id.checkbox2);
        c3 = findViewById(R.id.checkbox3);
        c4 = findViewById(R.id.checkbox4);

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
        setUnitOrderSpinner(R.array.units_array);
        setLessonOrderSpinner(R.array.lessons_array);
        setTermSpinner(R.array.term_array);
        setGradeSpinner();
        setSubjectsSpinner(R.array.preparatory_subjects_array_for_upload, "setSubject spinner in onCreate");

        final Button addQuestionBtn = findViewById(R.id.addQuestionBtn);
        addQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuestion(addQuestionBtn);
            }
        });

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(lessonOrderSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(300);

            Log.v("spinnerHeight", "try");
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.v("spinnerHeight", "catch , exception is : " + e);
        }

   /*     try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(subjectsSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(500);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }*/

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey("question")) {
            question = (Question) intent.getSerializableExtra("question");
            questionEt.setText(question.getAlQuestion());
            editText1.setText(question.getAnswer1());
            editText2.setText(question.getAnswer2());
            editText3.setText(question.getAnswer3());
            editText4.setText(question.getAnswer4());

            String correctAnswer = question.getCorrectAnswer();
            String[] correctAnswersArray = correctAnswer.split(",");
            if (Arrays.asList(correctAnswersArray).contains(question.getAnswer1())) {
                c1.setChecked(true);
            }
            if (Arrays.asList(correctAnswersArray).contains(question.getAnswer2())) {
                c2.setChecked(true);
            }
            if (Arrays.asList(correctAnswersArray).contains(question.getAnswer3())) {
                c3.setChecked(true);
            }
            if (Arrays.asList(correctAnswersArray).contains(question.getAnswer4())) {
                c4.setChecked(true);
            }


            String grade = question.getGrade(); //the value you want the position for
            ArrayAdapter myAdap = (ArrayAdapter) gradesSpinner.getAdapter(); //cast to an ArrayAdapter
            int spinnerPosition = myAdap.getPosition(grade);
            gradesSpinner.setSelection(spinnerPosition);

            String term = convertTermToString(question.getTerm()); //the value you want the position for
            myAdap = (ArrayAdapter) termSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(term);
            termSpinner.setSelection(spinnerPosition);

            String subject = question.getSubject(); //the value you want the position for
            myAdap = (ArrayAdapter) subjectsSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(subject);
            subjectsSpinner.setSelection(spinnerPosition);

            String unitNumber = question.getUnitNumber(); //the value you want the position for
            myAdap = (ArrayAdapter) unitOrderSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(unitNumber);
            unitOrderSpinner.setSelection(spinnerPosition);

            String lessonNumber = question.getLessonNumber(); //the value you want the position for
            myAdap = (ArrayAdapter) lessonOrderSpinner.getAdapter(); //cast to an ArrayAdapter
            spinnerPosition = myAdap.getPosition(lessonNumber);
            lessonOrderSpinner.setSelection(spinnerPosition);

            oldQuestion = true;
            oldQuestionId = question.getQuestionId();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    void setSubjectsSpinner(int array, String tag) {

        //Change the xml array to java array
        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddQuestionActivity.this,R.layout.testactiv,subjects){


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






   /* ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                array, R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);*/

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                selectedItemlecture  = i;
               currentSubject = adapterView.getItemAtPosition(i).toString();
                if (!oldQuestion) {
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
                            setUnitOrderSpinner(R.array.units_array);
                            setLessonOrderSpinner(R.array.lessons_array);
                            break;
                        default:
                            unitOrderSpinner.setEnabled(true);
                            unitOrderSpinner.setClickable(true);
                            lessonOrderSpinner.setEnabled(true);
                            lessonOrderSpinner.setClickable(true);
                            setUnitOrderSpinner(R.array.units_array);
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

    void setUnitOrderSpinner(int  array) {

        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddQuestionActivity.this,R.layout.testactiv,subjects){


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

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddQuestionActivity.this,R.layout.testactiv,subjects){


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
                selectedItemglesson = i;
                selectedLesson = adapterView.getItemAtPosition(i).toString();
                if(selectedLesson.equals("الوحدة")){
                    selectedLesson = "";
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setTermSpinner(int  array) {






        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddQuestionActivity.this,R.layout.testactiv,subjects){


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

    void setGradeSpinner() {




        String[] countryNames = this.getResources().getStringArray(R.array.grades_array);

            ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(AddQuestionActivity.this,R.layout.testactiv,countryNames){


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

                selectedItemgrade  =  i;
          selectedGrade = adapterView.getItemAtPosition(i).toString();
              if (!oldQuestion) {
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

    public void addQuestion(View view) {
        String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        final String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String questionText = questionEt.getText().toString().trim();
        String et1 = editText1.getText().toString().trim();
        String et2 = editText2.getText().toString().trim();
        String et3 = editText3.getText().toString().trim();
        String et4 = editText4.getText().toString().trim();

        //setTheCorrectAnswers
        correctAnswer = ""; // make the correct answer string empty
        if (c1.isChecked()) {
            addAnswer(et1.trim());
        }
        if (c2.isChecked()) {
            addAnswer(et2.trim());
        }
        if (c3.isChecked()) {
            addAnswer(et3.trim());
        }
        if (c4.isChecked()) {
            addAnswer(et4.trim());
        }


        if (selectedGrade.equals("اختر الصف الدراسى")) {
            Toast.makeText(this, "برجاء تحديد الصف الدراسى", Toast.LENGTH_SHORT).show();
        } else if (selectedTerm.equals("اختر الفصل الدراسى")) {
            Toast.makeText(this, "برجاء تحديد الفصل الدراسى", Toast.LENGTH_SHORT).show();
        } else if (currentSubject.equals("اختر المادة")) {
            Toast.makeText(this, "من فضلك اختر المادة التي ينتمى لها هذا السؤال", Toast.LENGTH_SHORT).show();
        } else if (selectedUnit.equals("الوحدة") && unitOrderSpinner.getVisibility() == View.VISIBLE && !currentSubject.equals("لغة عربية: نحو")) {
            Toast.makeText(this, "برجاء تحديد الوحدة الحالية", Toast.LENGTH_SHORT).show();
        } else if (lessonOrderSpinner.getVisibility() == View.VISIBLE
                && (selectedLesson.equals("الدرس") || selectedLesson.equals("الفصل") || selectedLesson.equals("ترتيب الدرس"))
                && !currentSubject.equals("لغة انجليزية")) {
            Toast.makeText(this, "برجاء تحديد ترتيب الدرس", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(questionText)) {
            questionEt.setError("من فضلك ادخل عنوان السؤال");
        } else if (TextUtils.isEmpty(et1)) {
            editText1.setError("هذا الحقل إجبارى");
        } else if (TextUtils.isEmpty(et2)) {
            editText2.setError("هذا الحقل إجبارى");
        } else if (correctAnswer.equals("")) {
            Toast.makeText(this, "من فضلك قم بتحديد الإجابة أو الإجابات الصحيحة", Toast.LENGTH_SHORT).show();
        } else {

            String schoolType = getSchoolType(currentSubject);

            if (!oldQuestion) {
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
                map.put("questionType", "choose"); // TODO : edit this when new type of questions added
                map.put("reviewed", false);
                map.put("schoolType", schoolType);
                map.put("subject", currentSubject);
                map.put("term", convertTermToLong(selectedTerm));//Added
                map.put("writerName", currentUserName);
                map.put("writerEmail", localCurrentUserEmail);
                map.put("writerUid", localCurrentUserUid);
                map.put("dayDate", todayDate);
                map.put("order", selectedUnit + selectedLesson);
                map.put("date", dateClass.getDate());
                map.put("challengeQuestion", false);

                //TODO : add a condition to add if the question is choose
                map.put("alQuestion", questionText);
                map.put("answer1", et1.trim());
                map.put("answer2", et2.trim());
                map.put("answer3", et3.trim());
                map.put("answer4", et4.trim());
                map.put("correctAnswer", correctAnswer.trim());

                //TODO : add icon to the dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("تنبيه هام");
                alertDialog.setMessage("برجاء مراجعة السؤال جيدا قبل رفعه والتأكد من عدم وجود أي أخطاء إملائية به");
                alertDialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(AddQuestionActivity.this, "جارى رفع السؤال", Toast.LENGTH_SHORT).show();

                        final DocumentReference currentUserReference = fireStoreUsers.document(localCurrentUserUid);

                        fireStore.runTransaction(new Transaction.Function<Void>() {
                            @Nullable
                            @Override
                            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(currentUserReference);

                                long newPoints = snapshot.getLong("points") + 5;
                                long newAcceptedQuestions = snapshot.getLong("acceptedQuestions") + 1;

                                transaction.update(currentUserReference, "points", newPoints);
                                transaction.update(currentUserReference, "acceptedQuestions", newAcceptedQuestions);
                                transaction.set(fireStoreQuestions.document(selectedGrade).collection(currentSubject).document(), map);

                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "Transaction success!");
                                Toast.makeText(AddQuestionActivity.this, "تم رفع السؤال بنجاح", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Transaction failure.", e);
                                Toast.makeText(AddQuestionActivity.this, "لم يتم رفع السؤال برجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
                            }
                        });


                    /*   fireStoreQuestions.document(selectedGrade).collection(currentSubject).add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                Toast.makeText(AddQuestionActivity.this, "تم رفع السؤال بنجاح وسيتم مراجعته قبل ظهوره فى التحديات", Toast.LENGTH_SHORT).show();
                            }
                        });*/
                        clearViews();
                    }
                });
                alertDialog.create();
                alertDialog.show();


            } else if (oldQuestion) {
                DocumentReference currentQuestionReference = fireStoreQuestions.document(question.getGrade()).collection(question.getSubject()).document(oldQuestionId);

                batch.update(currentQuestionReference, "alQuestion", questionText);
                batch.update(currentQuestionReference, "answer1", et1.trim());
                batch.update(currentQuestionReference, "answer2", et2.trim());
                batch.update(currentQuestionReference, "answer3", et3.trim());
                batch.update(currentQuestionReference, "answer4", et4.trim());
                batch.update(currentQuestionReference, "correctAnswer", correctAnswer.trim());
                batch.update(currentQuestionReference, "schoolType", schoolType);
                batch.update(currentQuestionReference, "subject", currentSubject);
                batch.update(currentQuestionReference, "grade", selectedGrade);
                batch.update(currentQuestionReference, "unitNumber", selectedUnit);
                batch.update(currentQuestionReference, "lessonNumber", selectedLesson);
                batch.update(currentQuestionReference, "term", convertTermToLong(selectedTerm));

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AddQuestionActivity.this, "تم تحديث السؤال بنجاح", Toast.LENGTH_SHORT).show();
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
        questionEt.setText("");

        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");

        c1.setChecked(false);
        c2.setChecked(false);
        c3.setChecked(false);
        c4.setChecked(false);

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

    public void finishq(View view) {
        finish();
    }
}
