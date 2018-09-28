package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddQuestionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner subjectsSpinner;
    String correctAnswer = "";
    EditText editText1, editText2, editText3, editText4, questionEt;
    String currentSubject="";

    DatabaseReference myRef;
    FirebaseDatabase database;

    //TODO : fix the problems of signing in
    //TODO :

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        Toolbar toolbar = findViewById(R.id.toolbarInAddQuestion);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        editText1 = findViewById(R.id.et1);
        editText2 = findViewById(R.id.et2);
        editText3 = findViewById(R.id.et3);
        editText4 = findViewById(R.id.et4);
        questionEt = findViewById(R.id.addQuestionEditText);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("questions");

        ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                    correctAnswer = editText1.getText().toString();
                break;
            case R.id.radio2:
                if (checked)
                    correctAnswer = editText2.getText().toString();
                break;
            case R.id.radio3:
                if (checked)
                    correctAnswer = editText3.getText().toString();
                break;
            case R.id.radio4:
                if (checked)
                    correctAnswer = editText4.getText().toString();
                break;
        }
    }
    @OnClick(R.id.addQuestionBtn)
    public void addQuestion(View view){
        String question = questionEt.getText().toString();
        String et1 = editText1.getText().toString();
        String et2 = editText2.getText().toString();
        String et3 = editText3.getText().toString();
        String et4 = editText4.getText().toString();
        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(et1) || TextUtils.isEmpty(et2) || TextUtils.isEmpty(et3) || TextUtils.isEmpty(et4) ) {
            Toast.makeText(this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
        }
        else if(correctAnswer.equals("")){
            Toast.makeText(this, "من فضلك قم بتحديد الإجابة الصحيحة للسؤال", Toast.LENGTH_SHORT).show();
        }
        else {
            Map<String, Object> map = new HashMap<>();
            map.put("writer", "Mostafa Khaled");
            map.put("subject", currentSubject);
            map.put("al question", question);
            map.put("answer 1", et1);
            map.put("answer 2", et2);
            map.put("answer 3", et3);
            map.put("answer 4", et4);
            map.put("reviewed", false);
            map.put("correctAnswer", correctAnswer);
            myRef.push().setValue(map);
            Toast.makeText(this, "تم إضافة السؤال بنجاح وسيتم مراجعته", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentSubject = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
