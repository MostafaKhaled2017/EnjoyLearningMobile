package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.utils.DateClass;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;

public class AddArticleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText etArabicPosition, etContent, etUnitPosition, etSubject, etTitle;
    //TODO : push to the database who is the user who writes the lesson or the post and push it by a primary data
    //TODO : make a page where the student can see his note on every lesson and think about changing the idea for that the student writes his notes on the lesson but I on't prefer that I prefer make adding lesson adds a lot of XPs
    // TODO : think about replacing toasts with snackbar
    //TODO : determine the points of every thing

    Button addLessonButton;
    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner;

    String currentSubject = "", currentUnitOrder = "", currentLessonOrder = "", userName = "", userEmail = "";
    Map<String, Object> map;
    public SharedPreferences pref; // 0 - for private mode

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);
        Toolbar toolbar = findViewById(R.id.toolbarInAddLesson);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
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

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);

        etContent.setMovementMethod(LinkMovementMethod.getInstance());

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
                R.array.subjects_array_with_general_subjects_item, android.R.layout.simple_spinner_item);
        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        ArrayAdapter<CharSequence> unitsAdapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        unitsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        unitOrderSpinner.setAdapter(unitsAdapter);

        ArrayAdapter<CharSequence> lessonsAdapter = ArrayAdapter.createFromResource(this,
                R.array.lessons_array, android.R.layout.simple_spinner_item);
        lessonsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        lessonOrderSpinner.setAdapter(lessonsAdapter);

        addLessonButton = findViewById(R.id.addLessonButton);
        addLessonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = etContent.getText().toString();
                String title = etTitle.getText().toString();
                if (TextUtils.isEmpty(content) || TextUtils.isEmpty(title)) {
                    Toast.makeText(AddArticleActivity.this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
                }
                else if(currentSubject.equals("اختر المادة")) {
                    Toast.makeText(AddArticleActivity.this, "من فضلك اختر المادة التي ينتمى لها هذا الموضوع", Toast.LENGTH_SHORT).show();
                }else
                {
                    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
                    Date today = new Date();
                    final DateClass dateClass = new DateClass();
                    format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    dateClass.setDate(today);

                    userName = pref.getString("currentUserName","غير معروف");
                    map = new HashMap<>();
                    map.put("title", title);
                    map.put("content", content);
                    map.put("position", currentUnitOrder + currentLessonOrder);
                    map.put("subject", currentSubject);
                    map.put("writerName", userName);
                    map.put("writerEmail", localCurrentUserEmail);
                    map.put("writerUid", localCurrentUserUid);
                    map.put("type", "موضوع");
                    map.put("date", dateClass.getDate());
                    map.put("reviewed", false);//TODO :remove this

                    Toast.makeText(AddArticleActivity.this, "جارى رفع الموضوع", Toast.LENGTH_SHORT).show();
                    fireStoreLessons.add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(AddArticleActivity.this, "تم رفع الموضوع بنجاح", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddArticleActivity.this, "لم يتم رفع الموضوع برجاء التأكد من الإتصال بالانترنت", Toast.LENGTH_SHORT).show();
                            Log.v("AddArticleActivity", "exception is : " + e);
                        }
                    });

                /*   //TODO : add icon to the dialog
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddArticleActivity.this);
                    alertDialog.setTitle("تنبيه هام!!");
                    alertDialog.setMessage("الهدف من هذه الصفحة أن يقوم الطالب برفع ملخصه عن الدرس أو يقوم المدرسون بكتابة ملخصاتهم عن الدرس ممنوع نقل الدروس من الكتب الخارجية أو استخدام ملخصات لأي مدرس إلا بعد أخذ موافقته");
                    alertDialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.create();
                    alertDialog.show();*/
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