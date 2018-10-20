package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mk.enjoylearning.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;
import static com.mk.playAndLearn.utils.Firebase.questionsReference;

public class AddQuestionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner subjectsSpinner;
    String correctAnswer = "";
    EditText editText1, editText2, editText3, editText4, questionEt;
    String currentSubject = "", currentUserName;
    Map<String, Object> map;
    int currentCheckedRadioButton;
    RadioButton r1, r2, r3, r4;
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

        ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.subjects_array_with_default, android.R.layout.simple_spinner_item);
            subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(this);

        ButterKnife.bind(this);

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

        String question = questionEt.getText().toString();
        String et1 = editText1.getText().toString();
        String et2 = editText2.getText().toString();
        String et3 = editText3.getText().toString();
        String et4 = editText4.getText().toString();
        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(et1) || TextUtils.isEmpty(et2) || TextUtils.isEmpty(et3) || TextUtils.isEmpty(et4)) {
            Toast.makeText(this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
        } else if (correctAnswer.equals("")) {
            Toast.makeText(this, "من فضلك قم بتحديد الإجابة الصحيحة للسؤال", Toast.LENGTH_SHORT).show();
        }
        else if(currentSubject.equals("اختر المادة")) {
            Toast.makeText(this, "من فضلك اختر المادة التي ينتمى لها هذا السؤال", Toast.LENGTH_SHORT).show();
        }else
         {
            map = new HashMap<>();
            map.put("writerName", currentUserName);
            map.put("writerEmail", currentUserEmail);
            map.put("writerUid", currentUserUid);
            map.put("subject", currentSubject);
            map.put("al question", question);
            map.put("answer 1", et1);
            map.put("answer 2", et2);
            map.put("answer 3", et3);
            map.put("answer 4", et4);
            map.put("reviewed", false);
            map.put("correctAnswer", correctAnswer);
            // Toast.makeText(this, "تم إضافة السؤال بنجاح وسيتم مراجعته", Toast.LENGTH_SHORT).show();
            //TODO : add icon to the dialog
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("تنبيه هام!!");
            alertDialog.setMessage("الهدف من هذه الصفحة أن يقوم الطلبة بتأليف أسئلة خاصة بهم أو يقوم المدرسون برفع أسئلة من تأليفهم ممنوع نقل الأسئلة من الكتب الخارجية أو استخدام أسئلة خاصة بأى مدرس إلا بعد أخذ موافقته");
            alertDialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(AddQuestionActivity.this, "جارى رفع السؤال", Toast.LENGTH_SHORT).show();
                    questionsReference.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(AddQuestionActivity.this, "تم رفع السؤال بنجاح", Toast.LENGTH_SHORT).show();
                        }
                    });
                    clearViews();
                }
            });
            alertDialog.create();
            alertDialog.show();
            //TODO : think about removing finish and clear the edit texts and radio buttons
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

        r1.setChecked(false);
        r2.setChecked(false);
        r3.setChecked(false);
        r4.setChecked(false);
    }
}
