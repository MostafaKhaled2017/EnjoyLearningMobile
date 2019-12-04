package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.mk.enjoylearning.R;

import java.lang.reflect.Field;

import static com.mk.playAndLearn.utils.Integers.dailyChallengesNumber;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedTodayChallengesNo;


public class ChallengeDetailsActivity extends AppCompatActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private AdView mAdView;
    int selectedItemtirm = -1;
    int selectedItemunite = -1;
    int selectedItemglesson = -1;
    int selectedItemlecture = -1;

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


        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(unitOrderSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);

            Log.v("spinnerHeight", "try");
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.v("spinnerHeight", "catch , exception is : " + e);
        }


        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(lessonOrderSpinner);

            // Set popupWindow height to 850px
            popupWindow.setHeight(850);

            Log.v("spinnerHeight", "try");
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
            Log.v("spinnerHeight", "catch , exception is : " + e);
        }


        setTermSpinner(R.array.term_array);
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

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));

        mAdView = findViewById(R.id.adds);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B65A7976E9008CADC60414029149C78E")
                .build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                Log.v("adsLog", "addLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.v("adsLog", "error code is : " + errorCode);
            }
        });

    }


    void setTermSpinner(int array) {


        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter = new ArrayAdapter<String>(ChallengeDetailsActivity.this, R.layout.testactiv, subjects) {


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

        termSpinner.setSelection(1);
        termSpinner.setEnabled(false);
        termSpinner.setClickable(false);
        termSpinner.setAdapter(customAdapter);

        termSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemtirm = i;
                currentTerm = convertTermToLong(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setSubjectsSpinner() {
        int array;

        Log.v("gradeLogging", "saved grade is : " + getSavedGrade(this));

        if (getSavedGrade(this).equals("الصف الأول الإعدادى")) {
            array = R.array.first_preparatory_subjects_array;
        } else {
            array = R.array.preparatory_subjects_array;
        }

        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter = new ArrayAdapter<String>(ChallengeDetailsActivity.this, R.layout.testactiv, subjects) {

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

        subjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                selectedItemlecture = i;
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

    void setUnitOrderSpinner() {


        String[] subjects = this.getResources().getStringArray(R.array.units_array);

        ArrayAdapter<String> customAdapter = new ArrayAdapter<String>(ChallengeDetailsActivity.this, R.layout.testactiv, subjects) {


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
                currentUnit = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setLessonOrderSpinner() {

        String[] subjects = this.getResources().getStringArray(R.array.lessons_array);

        ArrayAdapter<String> customAdapter = new ArrayAdapter<String>(ChallengeDetailsActivity.this, R.layout.testactiv, subjects) {


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
            finish();
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


    public void finishch(View view) {

        finish();
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
