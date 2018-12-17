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

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;

public class AddQuestionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner subjectsSpinner;
    String correctAnswer = "";
    EditText editText1, editText2, editText3, editText4, questionEt;
    Question question;
    String currentSubject = "", currentUserName;
    Map<String, Object> map;
    int currentCheckedRadioButton;
    boolean oldQuestion = false;
    String oldQuestionId = "";
    WriteBatch batch;
    RadioButton r1, r2, r3, r4;
    RadioGroup radioGroup;
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

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        currentUserName = pref.getString("currentUserName", "غير معروف");
        Log.v("sharedPreference", " current userName is : " + currentUserName);

        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        editText1 = findViewById(R.id.et1);
        editText2 = findViewById(R.id.et2);
        editText3 = findViewById(R.id.et3);
        editText4 = findViewById(R.id.et4);
        questionEt = findViewById(R.id.addQuestionEditText);
        r1 = findViewById(R.id.radio1);
        r2 = findViewById(R.id.radio2);
        r3 = findViewById(R.id.radio3);
        r4 = findViewById(R.id.radio4);
        radioGroup = findViewById(R.id.radioGroup);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(subjectsSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array_for_upload, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(this);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if(intent.getExtras() != null && intent.getExtras().containsKey("question")){
            question = (Question) intent.getSerializableExtra("question");
            questionEt.setText(question.getAlQuestion());
            editText1.setText(question.getAnswer1());
            editText2.setText(question.getAnswer2());
            editText3.setText(question.getAnswer3());
            editText4.setText(question.getAnswer4());

            String correctAnswer = question.getCorrectAnswer();

            if(question.getAnswer1().equals(correctAnswer)){
                r1.setChecked(true);
            }
            if(question.getAnswer2().equals(correctAnswer)){
                r2.setChecked(true);
            }
            if(question.getAnswer3().equals(correctAnswer)){
                r3.setChecked(true);
            }
            if(question.getAnswer4().equals(correctAnswer)){
                r4.setChecked(true);
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

        switch (currentCheckedRadioButton) {
            case 1:
                correctAnswer = editText1.getText().toString();
                break;
            case 2:
                correctAnswer = editText2.getText().toString();
                break;
            case 3:
                correctAnswer = editText3.getText().toString();
                break;
            case 4:
                correctAnswer = editText4.getText().toString();
                break;
        }

        String questionText = questionEt.getText().toString().trim();
        String et1 = editText1.getText().toString();
        String et2 = editText2.getText().toString();
        String et3 = editText3.getText().toString();
        String et4 = editText4.getText().toString();
        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(et1) || TextUtils.isEmpty(et2) || TextUtils.isEmpty(et3) || TextUtils.isEmpty(et4)) {
            Toast.makeText(this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
        } else if (correctAnswer.equals("")) {
            Toast.makeText(this, "من فضلك قم بتحديد الإجابة الصحيحة للسؤال", Toast.LENGTH_SHORT).show();
        } else if (currentSubject.equals("اختر المادة")) {
            Toast.makeText(this, "من فضلك اختر المادة التي ينتمى لها هذا السؤال", Toast.LENGTH_SHORT).show();
        } else {
            String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String schoolType = getSchoolType(currentSubject);

            if(!oldQuestion) {
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String todayDate = format.format(today);

                map = new HashMap<>();
                map.put("writerName", currentUserName);
                map.put("writerEmail", localCurrentUserEmail);
                map.put("writerUid", localCurrentUserUid);
                map.put("subject", currentSubject);
                map.put("alQuestion", questionText);
                map.put("questionType", "choose"); // TODO : edit this when new type of questions added
                map.put("answer1", et1.trim());
                map.put("answer2", et2.trim());
                map.put("answer3", et3.trim());
                map.put("answer4", et4.trim());
                map.put("dayDate", todayDate);
                map.put("reviewed", false);
                map.put("challengeQuestion", false);
                map.put("schoolType", schoolType);
                map.put("correctAnswer", correctAnswer.trim());
                //TODO : add icon to the dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("تنبيه هام!!");
                alertDialog.setMessage("الهدف من هذه الصفحة أن يقوم الطلبة بتأليف أسئلة خاصة بهم أو يقوم المدرسون برفع أسئلة من تأليفهم ممنوع نقل الأسئلة من الكتب الخارجية أو استخدام أسئلة خاصة بأى مدرس إلا بعد أخذ موافقته");
                alertDialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(AddQuestionActivity.this, "جارى رفع السؤال", Toast.LENGTH_SHORT).show();
                        fireStoreQuestions.document(currentSubject).collection(currentSubject).add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
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
            else {
               DocumentReference currentQuestionReference =  fireStoreQuestions.document(question.getSubject()).collection(question.getSubject()).document(oldQuestionId);

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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentSubject = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    void clearViews() {
        questionEt.setText("");

        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");

        radioGroup.clearCheck();
    }

    public static String getSchoolType(String subject){
        if(subject.equals("فيزياء") || subject.equals("كيمياء")
                || subject.equals("أحياء") || subject.equals("رياضيات")){
            return "arabic";
        }
        else if(subject.equals("Physics") || subject.equals("Chemistry")
                || subject.equals("Biology") || subject.equals("Mathematics")){
            return "languages";
        }
        else {
            return "both";
        }
    }
}
