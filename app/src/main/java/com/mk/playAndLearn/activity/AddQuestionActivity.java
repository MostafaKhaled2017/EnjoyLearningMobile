package com.mk.playAndLearn.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.OnClick;

public class AddQuestionActivity extends AppCompatActivity {
    EditText etArabicPosition, etContent, etPosition, etSubject, etTitle;
    static DatabaseReference myRef;
    FirebaseDatabase database;
    Button addLessonButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        etArabicPosition = findViewById(R.id.arabicPositionInAddLesson);
        etContent = findViewById(R.id.contentInAddLesson);
        etPosition = findViewById(R.id.englishPositionInAddLesson);
        etSubject = findViewById(R.id.subjectInAddLesson);
        etTitle = findViewById(R.id.titleInAddLesson);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("lessons");

        addLessonButton = findViewById(R.id.addLessonButton);
        addLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String arabicPosition = etArabicPosition.getText().toString();
                String content = etContent.getText().toString();
                String position = etPosition.getText().toString();
                String subject = etSubject.getText().toString();
                String title = etTitle.getText().toString();
                if (TextUtils.isEmpty(arabicPosition) || TextUtils.isEmpty(content) || TextUtils.isEmpty(position) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(title)) {
                    Toast.makeText(AddQuestionActivity.this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put("arabic_position", arabicPosition);
                    map.put("title", title);
                    map.put("content", content);
                    map.put("position", position);
                    map.put("subject", subject);
                    myRef.push().setValue(map);
                    etTitle.setText("");
                    etSubject.setText("");
                    etPosition.setText("");
                    etContent.setText("");
                    etArabicPosition.setText("");
                    Toast.makeText(AddQuestionActivity.this, "تم إضافة الدرس بنجاح", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
