package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.SignUpActivityPresenter;

public class SignUpActivity extends AppCompatActivity implements SignUpActivityPresenter.View{
    EditText nameEt;
    RadioGroup usersTypeRadioGroup;
    RadioButton studentRB, teacherRB;
    Spinner genderSpinner, schoolTypeSpinner, userGradeSpinner;
    CheckBox acceptTerms;

    SignUpActivityPresenter presenter;

    private GoogleSignInClient mGoogleSignInClient;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    AlertDialog dialog;

    String userSchoolType, userType, gender, grade, imageUrl, uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEt = findViewById(R.id.etName);
        usersTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        studentRB = findViewById(R.id.studentRadioButton);
        teacherRB = findViewById(R.id.teacherRadioButton);
        genderSpinner = findViewById(R.id.genderSpinner);
        schoolTypeSpinner = findViewById(R.id.schoolTypeSpinner);
        userGradeSpinner = findViewById(R.id.userGradeSpinner);
        acceptTerms = findViewById(R.id.acceptTerms);

        presenter = new SignUpActivityPresenter(this, this);

        //initialize alertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setMessage("جارى إضافة حسابك ...");
        dialog = dialogBuilder.create();

        initializeGoogleLoginVariables();

        //set spinners
        setUserSchoolTypeSpinner();
        setUserGenderSpinner();
        setUserGradeSpinner();

    }



    void initializeGoogleLoginVariables(){
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

    public void setUserSchoolTypeSpinner(){
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

    public void signInWithGoogle(final int RC_SIGN_IN) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void setUserGenderSpinner(){
        ArrayAdapter<CharSequence> genderTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderTypeAdapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if(selectedItem.equals("Male") || selectedItem.equals("ذكر")){
                    gender = "ذكر";
                }
                else if(selectedItem.equals("Female") || selectedItem.equals("أنثى")){
                    gender = "أنثى";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    public void setUserGradeSpinner(){
            ArrayAdapter<CharSequence> userGradeAdapter = ArrayAdapter.createFromResource(this,
                    R.array.grades_array, android.R.layout.simple_spinner_item);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) { //1 for adding email
            String name = nameEt.getText().toString();

            presenter.checkEmailAndUploadData(data, name, gender, userSchoolType, userType, convertGrade(grade));
        }

    }

    public void selectUserType(View view) {
        int selectedId = usersTypeRadioGroup.getCheckedRadioButtonId();

        if(selectedId == studentRB.getId()){
            userType = "طالب";
        } else if(selectedId == teacherRB.getId()){
            userType = "معلم";
        }
    }

    public void signIn(View view){
        String name = nameEt.getText().toString().trim();
        boolean acceptTermsChecked = acceptTerms.isChecked();
        presenter.validateSignUp(name, userType, acceptTermsChecked);
    }

    public void showProgressDialog(){
        dialog.show();
    }

     public void hideProgressDialog(){
     dialog.dismiss();
    }

    @Override
    public void isSignedInToast() {
        Toast.makeText(this, R.string.isSignedInUser, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigate() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void setNameError() {
        nameEt.setError(getString(R.string.emptyEditText));
    }


    @Override
    public void setAcceptTermsCheckedError() {
        acceptTerms.setError(getString(R.string.emptyEditText));
    }

    @Override
    public void setUserTypeError() {
        Toast.makeText(this, "برجاء تحديد إذا كنت طالب أم معلم", Toast.LENGTH_SHORT).show();
    }

    //TODO: complete
    public static long convertGrade(String grade){
        switch (grade){
            case "الصف الأول الثانوى" : return 10;
            default: return 0;
        }
    }
}
