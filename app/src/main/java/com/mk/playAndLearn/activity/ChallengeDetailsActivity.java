package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mk.enjoylearning.R;

import static com.mk.playAndLearn.utils.Integers.dailyChallengesNumber;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedTodayChallengesNo;


public class ChallengeDetailsActivity extends AppCompatActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private AdView mAdView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Spinner subjectsSpinner, unitOrderSpinner, lessonOrderSpinner, termSpinner;
    Button startBtn;

    String currentSubject, currentUnit, currentLesson;
    long currentTerm;
    CheckBox includePreviousLessons;

    public ChallengeDetailsActivity() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_details);

  //      includePreviousLessons = findViewById(R.id.checkBox);

        subjectsSpinner = findViewById(R.id.subjectsSpinnerInChallengeDetails);
        unitOrderSpinner = findViewById(R.id.unitSpinnerInChallengeDetails);
        lessonOrderSpinner = findViewById(R.id.lessonSpinnerInChallengeDetails);
        termSpinner = findViewById(R.id.termSpinnerInChallengeDetails);
        startBtn = findViewById(R.id.startBtn);

        setTermSpinner();
        setSubjectsSpinner();
        setUnitOrderSpinner();
        setLessonOrderSpinner();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentTerm == -1) {
                    Toast.makeText(ChallengeDetailsActivity.this, "قم باختيار الفصل الدراسى", Toast.LENGTH_SHORT).show();
                } else if (currentSubject.equals("اختر المادة")) {
                    Toast.makeText(ChallengeDetailsActivity.this, "قم باختيار المادة التى تريدها", Toast.LENGTH_SHORT).show();
                } else if (currentUnit.equals("الوحدة") && unitOrderSpinner.isEnabled()) {
                    Toast.makeText(ChallengeDetailsActivity.this, "قم باختيار الوحدة ", Toast.LENGTH_SHORT).show();
                } else if (currentLesson.equals("الدرس") && lessonOrderSpinner.isEnabled()) {
                    Toast.makeText(ChallengeDetailsActivity.this, "قم باختيار الدرس", Toast.LENGTH_SHORT).show();
                } else {
                    navigate();
                }
            }
        });

    }

    
    void setTermSpinner(){
        ArrayAdapter<CharSequence> termAdapter = ArrayAdapter.createFromResource(ChallengeDetailsActivity.this,
                R.array.term_array, android.R.layout.simple_spinner_item);
        termAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        termSpinner.setSelection(1);
        termSpinner.setEnabled(false);
        termSpinner.setClickable(false);
        termSpinner.setAdapter(termAdapter);

        termSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentTerm = convertTermToLong(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    
    void setSubjectsSpinner(){
        ArrayAdapter<CharSequence> subjectsAdapter;

        Log.v("gradeLogging", "saved grade is : " + getSavedGrade(this));

        if(getSavedGrade(this).equals("الصف الأول الإعدادى")) {
            subjectsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.first_preparatory_subjects_array, R.layout.simple_spinner_item);
        } else {
            subjectsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.preparatory_subjects_array, R.layout.simple_spinner_item);
        }

        subjectsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        subjectsSpinner.setAdapter(subjectsAdapter);

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                currentSubject = adapterView.getItemAtPosition(i).toString();
                switch (currentSubject) {
                    case "لغة انجليزية":
                        unitOrderSpinner.setEnabled(true);
                        unitOrderSpinner.setClickable(true);
                        lessonOrderSpinner.setEnabled(false);
                        lessonOrderSpinner.setClickable(false);
                        lessonOrderSpinner.setSelection(0);
                        unitOrderSpinner.setSelection(0);
                        break;
                    case "لغة عربية: نحو":
                        lessonOrderSpinner.setEnabled(true);
                        lessonOrderSpinner.setClickable(true);
                        unitOrderSpinner.setEnabled(false);
                        unitOrderSpinner.setClickable(false);
                        unitOrderSpinner.setSelection(0);
                        lessonOrderSpinner.setSelection(0);
                        break;
                    default:
                        unitOrderSpinner.setEnabled(true);
                        unitOrderSpinner.setClickable(true);
                        lessonOrderSpinner.setEnabled(true);
                        lessonOrderSpinner.setClickable(true);
                        unitOrderSpinner.setSelection(0);
                        lessonOrderSpinner.setSelection(0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    
    void setUnitOrderSpinner(){
        ArrayAdapter<CharSequence> unitOrderAdapter = ArrayAdapter.createFromResource(ChallengeDetailsActivity.this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        unitOrderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        unitOrderSpinner.setAdapter(unitOrderAdapter);

        unitOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentUnit = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setLessonOrderSpinner(){
        ArrayAdapter<CharSequence> lessonsOrderAdapter = ArrayAdapter.createFromResource(ChallengeDetailsActivity.this,
                R.array.lessons_array, android.R.layout.simple_spinner_item);
        lessonsOrderAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        lessonOrderSpinner.setAdapter(lessonsOrderAdapter);

        lessonOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentLesson = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    public void navigate() {
        Log.v("todayChallengesNo", "todayChallengesNo is : " + getSavedTodayChallengesNo(ChallengeDetailsActivity.this));
        if (currentSubject.equals("كل المواد")) {
            Toast.makeText(ChallengeDetailsActivity.this, "برجاء اختيار المادة التى تريدها", Toast.LENGTH_SHORT).show();
        } else if (dailyChallengesNumber - getSavedTodayChallengesNo(ChallengeDetailsActivity.this) < 1) {
            Toast.makeText(ChallengeDetailsActivity.this, "لقد أنهيت عدد التحديات المسموح لك اليوم يمكنك العودة غدا للعب تحديات أخرى أو طلب من أحد أصدقائك بدء تحدى جديد ضدك", Toast.LENGTH_LONG).show();
        } else {
            Log.v("termLogging", "term in challengeStart is : " + currentTerm);
            Intent i = new Intent(ChallengeDetailsActivity.this, ChallengersActivity.class);
            i.putExtra("subject", currentSubject);
            i.putExtra("term", currentTerm);
            i.putExtra("unit", currentUnit);
            i.putExtra("lesson", currentLesson);
//          i.putExtra("includePreviousLessons", includePreviousLessons.isChecked());
            startActivity(i);
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
    
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
