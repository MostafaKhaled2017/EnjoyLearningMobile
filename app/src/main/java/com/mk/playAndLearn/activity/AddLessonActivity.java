package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;

import java.util.HashMap;
import java.util.Map;

public class AddLessonActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText etArabicPosition, etContent, etUnitPosition, etSubject, etTitle;
    //TODO : push to the database who is the user who writes the lesson or the post and push it by a primary data
    //TODO : make a page where the student can see his note on every lesson and think about changing the idea for that the student writes his notes on the lesson but I on't prefer that I prefer make adding lesson adds a lot of XPs
    // TODO : think about replacing toasts with snackbar
    static DatabaseReference myRef;
    FirebaseDatabase database;
    Button addLessonButton;
    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner;

    String currentSubject = "", currentUnitOrder = "", currentLessonOrder = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);
        Toolbar toolbar = findViewById(R.id.toolbarInAddLesson);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        etContent = findViewById(R.id.contentInAddLesson);
        etTitle = findViewById(R.id.titleInAddLesson);
        subjectsSpinner = findViewById(R.id.subjectsSpinner);
        unitOrderSpinner = findViewById(R.id.unitOrderSpinner);
        lessonOrderSpinner = findViewById(R.id.lessonOrderSpinner);

        subjectsSpinner.setOnItemSelectedListener(this);
        lessonOrderSpinner.setOnItemSelectedListener(this);
        unitOrderSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> subjectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        ArrayAdapter<CharSequence> unitsAdapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        unitsAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        unitOrderSpinner.setAdapter(unitsAdapter);

        ArrayAdapter<CharSequence> lessonsAdapter = ArrayAdapter.createFromResource(this,
                R.array.lessons_array, android.R.layout.simple_spinner_item);
        lessonsAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        lessonOrderSpinner.setAdapter(lessonsAdapter);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("lessons");

        addLessonButton = findViewById(R.id.addLessonButton);
        addLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = etContent.getText().toString();
                String title = etTitle.getText().toString();
                if (TextUtils.isEmpty(content) || TextUtils.isEmpty(title)) {
                    Toast.makeText(AddLessonActivity.this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", title);
                    map.put("content", content);
                    map.put("unit", Integer.parseInt(currentUnitOrder));
                    map.put("lesson", Integer.parseInt(currentLessonOrder));
                    map.put("subject", currentSubject);
                    map.put("lessonWriter", "Mostafa Khaled");
                    map.put("reviewed", false);
                    myRef.push().setValue(map);
                    etTitle.setText("");
                    etContent.setText("");
                    Toast.makeText(AddLessonActivity.this, "تم إضافة الدرس بنجاح و سيتم مراجعته", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.subjectsSpinner:
                currentSubject = adapterView.getItemAtPosition(i).toString();
                break;
            case R.id.unitOrderSpinner:
                currentUnitOrder = adapterView.getItemAtPosition(i).toString();
                break;
            case R.id.lessonOrderSpinner:
                currentLessonOrder = adapterView.getItemAtPosition(i).toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
