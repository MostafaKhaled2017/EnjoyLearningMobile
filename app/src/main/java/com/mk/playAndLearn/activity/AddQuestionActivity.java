package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddQuestionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner subjectsSpinner;
    String correctAnswer = "";
    EditText editText1, editText2, editText3, editText4, questionEt;
    String currentSubject = "", userName = "", userEmail = "";
    SharedPreferences sharedPreferences;
    Map<String, Object> map;

    DatabaseReference myRef;
    FirebaseDatabase database;
    FirebaseAuth mAuth;


    //TODO : fix the problems of signing in if exists
    //TODO : make push to github then revise the names to be used in the database well and change them when put and when get in all things then clear all the database before starting real use

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        mAuth = FirebaseAuth.getInstance();

        sharedPreferences = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            if (sharedPreferences.contains("userName")) {
                userName = sharedPreferences.getString("userName", "");
            }
            if (sharedPreferences.contains("userEmail")) {
                userEmail = sharedPreferences.getString("userEmail", "");
            }
        }
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
            subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            subjectsSpinner.setAdapter(subjectsAdapter);

            subjectsSpinner.setOnItemSelectedListener(this);

            ButterKnife.bind(this);

        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            finish();
            return true;
        }

        public void onRadioButtonClicked (View view){
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
        public void addQuestion (View view){
            String question = questionEt.getText().toString();
            String et1 = editText1.getText().toString();
            String et2 = editText2.getText().toString();
            String et3 = editText3.getText().toString();
            String et4 = editText4.getText().toString();
            if (TextUtils.isEmpty(question) || TextUtils.isEmpty(et1) || TextUtils.isEmpty(et2) || TextUtils.isEmpty(et3) || TextUtils.isEmpty(et4)) {
                Toast.makeText(this, "من فضلك ادخل كل البيانات المطلوبة", Toast.LENGTH_SHORT).show();
            } else if (correctAnswer.equals("")) {
                Toast.makeText(this, "من فضلك قم بتحديد الإجابة الصحيحة للسؤال", Toast.LENGTH_SHORT).show();
            } else {
                map = new HashMap<>();
                map.put("writerName", userName);
                map.put("writerEmail", userEmail);
                map.put("subject", currentSubject);
                map.put("al question", question);
                map.put("answer 1", et1);
                map.put("answer 2", et2);
                map.put("answer 3", et3);
                map.put("answer 4", et4);
                map.put("reviewed", false);
                map.put("correctAnswer", correctAnswer);

                final DatabaseReference usersRefrence = database.getReference("users");
                usersRefrence.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                        usersRefrence.child(mAuth.getCurrentUser().getUid()).child("points").setValue(userPoints + 5);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

               // Toast.makeText(this, "تم إضافة السؤال بنجاح وسيتم مراجعته", Toast.LENGTH_SHORT).show();
                //TODO : add icon to the dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("تنبيه هام!!");
                alertDialog.setMessage("بعد رفع السؤال سيتم مراجعته أولا قبل نشره وإذا كان فيه شئ خارج المنهج أو مختلف عن أهداف هذا البرنامج فسيتم حذف الحساب");
                alertDialog.setNegativeButton("موافق", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef.push().setValue(map);
                        Toast.makeText(AddQuestionActivity.this, "تم رفع السؤال بنجاح", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                alertDialog.create();
                alertDialog.show();
                alertDialog.show();
                //TODO : think about removing finish and clear the edit texts and radio buttons
            }
        }

        @Override
        public void onItemSelected (AdapterView < ? > adapterView, View view,int i, long l){
            currentSubject = adapterView.getItemAtPosition(i).toString();
        }

        @Override
        public void onNothingSelected (AdapterView < ? > adapterView){

        }
    }
