package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.SignUpActivityPresenter;
import com.mk.playAndLearn.spinnercustom.CustomAdapter;

public class SignUp2Activity extends FragmentActivity implements SignUpActivityPresenter.View {

    Spinner schoolTypeSpinner, userGradeSpinner, governoratesSpinner;
    TextView termsTv;
    CheckBox acceptTerms;
    Button registerBtn;
    EditText phoneNumberEt, invitingFriendEt;
    String userSchoolType, currentGovernorate, grade;
    String name, email, password, rePassword, gender, userType;
    long points;
    ProgressBar progressBar;
    SignUpActivityPresenter presenter;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    int  selectedItemtype =-1;
    int  selectedItemcity =-1;

    int  selectedItemlevel =-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        schoolTypeSpinner = findViewById(R.id.schoolTypeSpinner);
        userGradeSpinner = findViewById(R.id.userGradeSpinner);
        acceptTerms = findViewById(R.id.acceptTerms);
        registerBtn = findViewById(R.id.signUpBtn);
        termsTv = findViewById(R.id.termsTv);
        governoratesSpinner = findViewById(R.id.governorateName);
        phoneNumberEt = findViewById(R.id.phoneNo);
        invitingFriendEt = findViewById(R.id.invitingFriend);
        progressBar = findViewById(R.id.progressbarOfSignUp2);

        presenter = new SignUpActivityPresenter(this, this);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            gender = intent.getStringExtra("gender");
            userType = intent.getStringExtra("userType");
            points = intent.getLongExtra("points", -1);
        }

        initializeGoogleLoginVariables();

        SpannableString ss = new SpannableString("أوافق على شروط الإستخدام");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(SignUp2Activity.this, TermsActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 10, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsTv.setText(ss);
        termsTv.setMovementMethod(LinkMovementMethod.getInstance());
        termsTv.setHighlightColor(Color.TRANSPARENT);

        setUserSchoolTypeSpinner(R.array.user_school_types_array);
        setGovernoratesSpinner( R.array.governorates);
        setUserGradeSpinner( R.array.sign_up_grades_array);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableBtn();

                String phoneNumber = phoneNumberEt.getText().toString().trim();
                String invitingFriend = invitingFriendEt.getText().toString().trim();
                boolean acceptTermsChecked = acceptTerms.isChecked();

                if (grade.equals("اختر الصف الدراسى")) {
                    enableBtn();
                    Toast.makeText(SignUp2Activity.this, "برجاء إدخال الصف الدراسى", Toast.LENGTH_SHORT).show();
                } else if (currentGovernorate.equals("المحافظة")) {
                    enableBtn();
                    Toast.makeText(SignUp2Activity.this, "برجاء إدخال المحافظة", Toast.LENGTH_SHORT).show();
                } else if (!acceptTermsChecked) {
                    enableBtn();
                    acceptTerms.setError("يجب عليك الموافقة على شروط الإستخدام لتستطيع الاستمرار");
                } else {
                    presenter.validateSignUpAndUploadData(name, email, password, gender, userType, points
                            , grade, userSchoolType, phoneNumber, currentGovernorate, invitingFriend);
                }
            }
        });

    }

    void initializeGoogleLoginVariables() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignUp2Activity.this, "حدث خطأ برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void setUserSchoolTypeSpinner(int  array) {




        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(SignUp2Activity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemtype) {
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
        schoolTypeSpinner.setAdapter(customAdapter);

        schoolTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                selectedItemtype =  i;
                userSchoolType = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    public void setGovernoratesSpinner(int  array) {


        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(SignUp2Activity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemcity) {
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
        governoratesSpinner.setAdapter(customAdapter);


        governoratesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemcity = i;
                currentGovernorate = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setUserGradeSpinner(int  array) {

        String[] subjects = this.getResources().getStringArray(array);

        ArrayAdapter<String> customAdapter=new ArrayAdapter<String>(SignUp2Activity.this,R.layout.testactiv,subjects){


            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selectedItemlevel) {
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
        userGradeSpinner.setAdapter(customAdapter);


        userGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItemlevel  = i;
                grade = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void navigate() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setPoints(long points) {
        this.points = points;
    }

    @Override
    public void disableBtn() {
        registerBtn.setClickable(false);
        registerBtn.setEnabled(false);
    }

    @Override
    public void enableBtn() {
        registerBtn.setClickable(true);
        registerBtn.setEnabled(true);
    }
}
