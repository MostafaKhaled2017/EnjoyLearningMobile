package com.mk.playAndLearn.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.SignUpActivityPresenter;

import jp.wasabeef.blurry.Blurry;

public class SignUpActivity extends AppCompatActivity implements SignUpActivityPresenter.View {
    EditText nameEt, emailEt, passwordEt, rePasswordEt;
    TextView termsTv;
    RadioGroup usersTypeRadioGroup;
    RadioButton studentRB, teacherRB;
    Spinner genderSpinner, schoolTypeSpinner, userGradeSpinner;
    CheckBox acceptTerms;
    ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    AuthCredential credential;

    SignUpActivityPresenter presenter;
    Button addEmailButton;

    ProgressDialog progressDialog;
    Button registerBtn;

    String userSchoolType, userType, gender, grade, imageUrl;
    long points = 0;

    ImageView backgroundIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        termsTv = findViewById(R.id.termsTv);
        nameEt = findViewById(R.id.etName);
        emailEt = findViewById(R.id.etEmail);
        passwordEt = findViewById(R.id.etPassword);
        rePasswordEt = findViewById(R.id.etRePassword);
        usersTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        studentRB = findViewById(R.id.studentRadioButton);
        teacherRB = findViewById(R.id.teacherRadioButton);
        genderSpinner = findViewById(R.id.genderSpinner);
        schoolTypeSpinner = findViewById(R.id.schoolTypeSpinner);
        userGradeSpinner = findViewById(R.id.userGradeSpinner);
        progressBar = findViewById(R.id.progressbar);
        acceptTerms = findViewById(R.id.acceptTerms);
        addEmailButton = findViewById(R.id.addEmailButton);
        backgroundIv = findViewById(R.id.backgroundIv);
        registerBtn = findViewById(R.id.signUpBtn);

        presenter = new SignUpActivityPresenter(this, this);

        initializeGoogleLoginVariables();

        //set spinners
        setUserSchoolTypeSpinner();
        setUserGenderSpinner();
        setUserGradeSpinner();

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sign_background);
        Blurry.with(this).from(bitmap).into(backgroundIv);

        addEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle(1);
            }
        });

        emailEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SignUpActivity.this, "اضغط على زر إضافة وسيتم كتابة بريدك الالكترونى تلقائيا", Toast.LENGTH_SHORT).show();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableRegisterButton();

                String name = nameEt.getText().toString().trim();
                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();
                String rePassword = rePasswordEt.getText().toString().trim();
                boolean acceptTermsChecked = acceptTerms.isChecked();

                int selectedId = usersTypeRadioGroup.getCheckedRadioButtonId();

                if (selectedId == studentRB.getId()) {
                    userType = "طالب";
                } else if (selectedId == teacherRB.getId()) {
                    userType = "معلم";
                }

                presenter.validateSignUpAndUploadData(name, email, password, rePassword, gender, userSchoolType, userType, grade, acceptTermsChecked, points);
            }
        });

        SpannableString ss = new SpannableString("أوافق على شروط الإستخدام");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(SignUpActivity.this, TermsActivity.class));
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
                        Toast.makeText(SignUpActivity.this, "حدث خطأ برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void signInWithGoogle(final int RC_SIGN_IN) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1 && resultCode == RESULT_OK) { //1 for adding email
            presenter.addEmailAddress(data);
        } else {
            Toast.makeText(this, "فشل إضافة الحساب برجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
        }

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
    public void setEmailEt(String text) {
        emailEt.setText(text);
    }

    @Override
    public void setPoints(long points) {
        this.points = points;
    }

    public void setUserSchoolTypeSpinner() {
        ArrayAdapter<CharSequence> userSchoolTypesAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_school_types_array, android.R.layout.simple_spinner_item);
        userSchoolTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolTypeSpinner.setAdapter(userSchoolTypesAdapter);

        schoolTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userSchoolType = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void setUserGenderSpinner() {
        ArrayAdapter<CharSequence> genderTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderTypeAdapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if (selectedItem.equals("Male") || selectedItem.equals("ذكر")) {
                    gender = "ذكر";
                } else if (selectedItem.equals("Female") || selectedItem.equals("أنثى")) {
                    gender = "أنثى";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    public void setUserGradeSpinner() {
        ArrayAdapter<CharSequence> userGradeAdapter = ArrayAdapter.createFromResource(this,
                R.array.sign_up_grades_array, android.R.layout.simple_spinner_item);
        userGradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userGradeSpinner.setAdapter(userGradeAdapter);

        userGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                grade = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void disableRegisterButton() {
        registerBtn.setClickable(false);
        registerBtn.setEnabled(false);
    }

    @Override
    public void enableRegisterButton() {
        registerBtn.setClickable(true);
        registerBtn.setEnabled(true);
    }

    @Override
    public void navigate() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void setNameError() {
        nameEt.setError(getString(R.string.emptyEditText));
        nameEt.requestFocus();
    }

    @Override
    public void setPasswordError(String message) {
        passwordEt.setError(message);
        passwordEt.requestFocus();
    }

    @Override
    public void setRePasswordError(String message) {
        rePasswordEt.setError(message);
        rePasswordEt.requestFocus();
    }


    @Override
    public void setAcceptTermsCheckedError() {
        acceptTerms.setError("يجب تعليم هذا الحقل للمتابعة");
    }

    @Override
    public void setUserTypeError() {
        Toast.makeText(this, "برجاء تحديد إذا كنت طالب أم معلم", Toast.LENGTH_SHORT).show();
    }
}
