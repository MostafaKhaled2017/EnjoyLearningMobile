package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
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
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;

public class AddQuestionActivity extends AppCompatActivity {
    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner, termSpinner, languageBranchesSpinner, gradesSpinner;
    String correctAnswer = "";
    EditText editText1, editText2, editText3, editText4, questionEt;
    TextView languageBranchesTv, unitOrderTv;
    Question question;
    String currentSubject = "", currentUserName, selectedUnit, selectedLesson, selectedTerm, selectedLanguageBranch, selectedGrade;
    Map<String, Object> map;
    int currentCheckedRadioButton;
    boolean oldQuestion = false;
    String oldQuestionId = "";
    WriteBatch batch;
    CheckBox c1, c2, c3, c4;
    public SharedPreferences pref; // 0 - for private mode


    //TODO : fix the problems of signing in if exists
    //TODO : make push to github then revise the names to be used in the database well and change them when put and when get in all things then clear all the database before starting real use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
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
        toolbarTitle.setText("إضافة سؤال");

        batch = fireStore.batch();

        Log.v("sharedPreference", " current userName is : " + currentUserName);

        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        gradesSpinner = findViewById(R.id.gradeSpinner);
        unitOrderSpinner = findViewById(R.id.unitOrderSpinner);
        lessonOrderSpinner = findViewById(R.id.lessonOrderSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        languageBranchesSpinner = findViewById(R.id.languageBranchesSpinner);
        languageBranchesTv = findViewById(R.id.languageBranchesTv);
        unitOrderTv = findViewById(R.id.unitOrderTextView);
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
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(subjectsSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        //setSpinners
        setSubjectsSpinner();
        setUnitOrderSpinner();
        setLessonOrderSpinner(R.array.lessons_array);
        setTermSpinner();
        setGradeSpinner();

        ButterKnife.bind(this);

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

            String subject = question.getSubject(); //the value you want the position for

            ArrayAdapter myAdap = (ArrayAdapter) subjectsSpinner.getAdapter(); //cast to an ArrayAdapter

            int spinnerPosition = myAdap.getPosition(subject);

            subjectsSpinner.setSelection(spinnerPosition);

            oldQuestion = true;
            oldQuestionId = question.getQuestionId();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    void setSubjectsSpinner() {
        ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array_for_upload, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSubject = adapterView.getItemAtPosition(i).toString();
                if (currentSubject.equals("لغة عربية")) {
                    showLanguageBranchesSpinner();
                    setLanguageBranchesSpinner(R.array.arabic_branches_array);
                } else if (currentSubject.equals("لغة انجليزية")) {
                    showLanguageBranchesSpinner();
                    setLanguageBranchesSpinner(R.array.english_branches_array);
                } else {
                    hideLanguageBranchesSpinner();
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

    void setLanguageBranchesSpinner(int array) {
        ArrayAdapter<CharSequence> languageBranchesAdapter = ArrayAdapter.createFromResource(this,
                array, android.R.layout.simple_spinner_item);
        languageBranchesAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        languageBranchesSpinner.setAdapter(languageBranchesAdapter);

        languageBranchesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLanguageBranch = adapterView.getItemAtPosition(i).toString();
                //in the store of arabic and english and in the grammar of arabic only
                if (selectedLanguageBranch.equals("قصة")) {
                    setLessonOrderSpinner(R.array.story_chapters_array);
                    hideUnitOrderSpinner();
                } else if (selectedLanguageBranch.equals("نحو")) {
                    setLessonOrderSpinner(R.array.arabic_grammar_array);
                    hideUnitOrderSpinner();
                } else {
                    setLessonOrderSpinner(R.array.lessons_array);
                    showUnitOrderSpinner();
                }
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void showLanguageBranchesSpinner() {
        languageBranchesTv.setVisibility(View.VISIBLE);
        languageBranchesSpinner.setVisibility(View.VISIBLE);
    }

    void hideLanguageBranchesSpinner() {
        languageBranchesTv.setVisibility(View.GONE);
        languageBranchesSpinner.setVisibility(View.GONE);
    }

    void hideUnitOrderSpinner() {
        unitOrderSpinner.setVisibility(View.GONE);
        unitOrderTv.setVisibility(View.GONE);
    }


    void showUnitOrderSpinner() {
        unitOrderSpinner.setVisibility(View.VISIBLE);
        unitOrderTv.setVisibility(View.VISIBLE);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio1:
                if (checked)
                    currentCheckedRadioButton = 1;
                break;
            case R.id.radio2:
                if (checked)
                    currentCheckedRadioButton = 2;
                break;
            case R.id.radio3:
                if (checked)
                    currentCheckedRadioButton = 3;
                break;
            case R.id.radio4:
                if (checked)
                    currentCheckedRadioButton = 4;
                break;
        }
    }

    @OnClick(R.id.addQuestionBtn)
    public void addQuestion(View view) {
        String questionText = questionEt.getText().toString().trim();
        String et1 = editText1.getText().toString();
        String et2 = editText2.getText().toString();
        String et3 = editText3.getText().toString();
        String et4 = editText4.getText().toString();

        //setTheCorrectAnswers
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

        if (TextUtils.isEmpty(questionText)) {
            questionEt.setError("من فضلك ادخل عنوان السؤال");
        } else if (TextUtils.isEmpty(et1)) {
            editText1.setError("هذا الحقل إجبارى");
        } else if (TextUtils.isEmpty(et2)) {
            editText2.setError("هذا الحقل إجبارى");
        } else if (correctAnswer.equals("")) {
            Toast.makeText(this, "من فضلك قم بتحديد الإجابة أو الإجابات الصحيحة", Toast.LENGTH_SHORT).show();
        } else if (currentSubject.equals("اختر المادة")) {
            Toast.makeText(this, "من فضلك اختر المادة التي ينتمى لها هذا السؤال", Toast.LENGTH_SHORT).show();
        } else {
            String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String schoolType = getSchoolType(currentSubject);

            if (!oldQuestion) {
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String todayDate = format.format(today);

                DateClass dateClass = new DateClass();
                dateClass.setDate(today);

                if (selectedUnit.equals("الوحدة") && unitOrderSpinner.getVisibility() == View.VISIBLE) {
                    Toast.makeText(this, "برجاء تحديد الوحدة الحالية", Toast.LENGTH_SHORT).show();
                } else if (lessonOrderSpinner.getVisibility() == View.VISIBLE
                        && (selectedLesson.equals("الدرس") || selectedLesson.equals("الفصل") || selectedLesson.equals("ترتيب الدرس"))) {
                    Toast.makeText(this, "برجاء تحديد ترتيب الدرس", Toast.LENGTH_SHORT).show();
                } else if (selectedTerm.equals("اختر الفصل الدراسى")) {
                    Toast.makeText(this, "برجاء تحديد الفصل الدراسى", Toast.LENGTH_SHORT).show();
                } else if (languageBranchesSpinner.getVisibility() == View.VISIBLE
                        && selectedLanguageBranch.equals("اختر نوع السؤال")) {
                    Toast.makeText(this, "برجاء تحديد فرع اللغة الذي ينتمى له السؤال", Toast.LENGTH_SHORT).show();
                } else if (selectedGrade.equals("اختر الصف الدراسى")) {
                    Toast.makeText(this, "برجاء تحديد الصف الدراسى", Toast.LENGTH_SHORT).show();
                } else {
                    map = new HashMap<>();

                    map.put("grade", selectedGrade);
                    map.put("unitNumber", selectedUnit);//Added
                    map.put("lessonNumber", selectedLesson);//Added
                    map.put("questionType", "choose"); // TODO : edit this when new type of questions added
                    map.put("reviewed", false);
                    map.put("schoolType", schoolType);
                    map.put("subject", currentSubject);
                    map.put("term", convertTerm(selectedTerm));//Added
                    map.put("writerName", currentUserName);
                    map.put("writerEmail", localCurrentUserEmail);
                    map.put("writerUid", localCurrentUserUid);
                    map.put("dayDate", todayDate);
                    map.put("date", dateClass.getDate());
                    map.put("challengeQuestion", false);

                    if (currentSubject.equals("لغة عربية") || currentSubject.equals("لغة انجليزية")) {
                        map.put("languageBranch", selectedLanguageBranch);
                    }

                    //TODO : add a condition to add if the question is choose
                    map.put("alQuestion", questionText);
                    map.put("answer1", et1.trim());
                    map.put("answer2", et2.trim());
                    map.put("answer3", et3.trim());
                    map.put("answer4", et4.trim());
                    map.put("correctAnswer", correctAnswer.trim());

                    //TODO : add icon to the dialog
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("تنبيه هام!!");
                    alertDialog.setMessage("الهدف من هذه الصفحة أن يقوم الطلبة بتأليف أسئلة خاصة بهم أو يقوم المدرسون برفع أسئلة من تأليفهم ممنوع نقل الأسئلة من الكتب الخارجية أو استخدام أسئلة خاصة بأى مدرس إلا بعد أخذ موافقته");
                    alertDialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(AddQuestionActivity.this, "جارى رفع السؤال", Toast.LENGTH_SHORT).show();
                            fireStoreQuestions.document(selectedGrade).collection(currentSubject).add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    Toast.makeText(AddQuestionActivity.this, "تم رفع السؤال بنجاح وسيتم مراجعته قبل ظهوره فى التحديات", Toast.LENGTH_SHORT).show();

                                }
                            });
                            clearViews();
                        }
                    });
                    alertDialog.create();
                    alertDialog.show();

                }
            } else if (oldQuestion) {
                DocumentReference currentQuestionReference = fireStoreQuestions.document(question.getGrade()).collection(question.getSubject()).document(oldQuestionId);

                batch.update(currentQuestionReference, "alQuestion", questionText);
                batch.update(currentQuestionReference, "answer1", et1.trim());
                batch.update(currentQuestionReference, "answer2", et2.trim());
                batch.update(currentQuestionReference, "answer3", et3.trim());
                batch.update(currentQuestionReference, "answer4", et4.trim());
                batch.update(currentQuestionReference, "correctAnswer", correctAnswer.trim());
                batch.update(currentQuestionReference, "subject", currentSubject);
                batch.update(currentQuestionReference, "schoolType", schoolType);

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
        if (correctAnswer.length() == 0) {
            correctAnswer += answer;
        } else if (correctAnswer.length() > 0) {
            correctAnswer += "," + answer;
        }
    }

    long convertTerm(String term) {
        switch (term) {
            case "الفصل الدراسى الأول":
                return 1;
            case "الفصل الدراسى الثانى":
                return 2;
            default:
                return -1;
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

    }

    public static String getSchoolType(String subject) {
        if (subject.equals("فيزياء") || subject.equals("كيمياء")
                || subject.equals("أحياء") || subject.equals("رياضيات")) {
            return "arabic";
        } else if (subject.equals("Physics") || subject.equals("Chemistry")
                || subject.equals("Biology") || subject.equals("Mathematics")) {
            return "languages";
        } else {
            return "both";
        }
    }
}
