package com.mk.playAndLearn.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.SignUpActivityPresenter;

import jp.wasabeef.blurry.Blurry;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;

public class SignUpActivity extends AppCompatActivity {
    EditText nameEt, emailEt, passwordEt, rePasswordEt;
    RadioGroup usersTypeRadioGroup;
    RadioButton studentRB, teacherRB;
    Spinner genderSpinner;
    CheckBox acceptTerms;
    ProgressBar progressBar;
    AuthCredential credential;

    Button addEmailButton;

    ProgressDialog progressDialog;
    Button nextBtn;

    String userType, gender, imageUrl;
    long points = 0;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    ImageView backgroundIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEt = findViewById(R.id.etName);
        emailEt = findViewById(R.id.etEmail);
        passwordEt = findViewById(R.id.etPassword);
        rePasswordEt = findViewById(R.id.etRePassword);
        usersTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        studentRB = findViewById(R.id.studentRadioButton);
        teacherRB = findViewById(R.id.teacherRadioButton);
        genderSpinner = findViewById(R.id.genderSpinner);
        progressBar = findViewById(R.id.progressbar);
        addEmailButton = findViewById(R.id.addEmailButton);
        backgroundIv = findViewById(R.id.backgroundIv);
        nextBtn = findViewById(R.id.nextBtn);

        initializeGoogleLoginVariables();

        //set spinners
        setUserGenderSpinner();

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

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEt.getText().toString().trim();
                String email = emailEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();
                String rePassword = rePasswordEt.getText().toString().trim();

                int selectedId = usersTypeRadioGroup.getCheckedRadioButtonId();

                if (selectedId == studentRB.getId()) {
                    userType = "طالب";
                } else if (selectedId == teacherRB.getId()) {
                    userType = "معلم";
                }

                if (TextUtils.isEmpty(name)) {
                    setNameError();
                } else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignUpActivity.this, "برجاء إضافة البريد الالكترونى الخاص بك", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    setPasswordError(SignUpActivity.this.getApplicationContext().getString(R.string.emptyEditText));
                } else if (TextUtils.isEmpty(rePassword)) {
                    setRePasswordError(SignUpActivity.this.getApplicationContext().getString(R.string.emptyEditText));
                } else if (!password.equals(rePassword)) {
                    setRePasswordError("كلمة السر وتأكيد كلمة السر غير متطابقين");
                } else if (password.length() < 6) {
                    setPasswordError("كلمة المرور يجب ألا تقل عن 6 حروف أو أرقام");
                } else if (TextUtils.isEmpty(userType)) {
                   setUserTypeError();
                } else {

                    Intent intent = new Intent(SignUpActivity.this, SignUp2Activity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("gender", gender);
                    intent.putExtra("userType", userType);
                    intent.putExtra("points", points);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    public void signInWithGoogle(final int RC_SIGN_IN) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1 && resultCode == RESULT_OK) { //1 for adding email
            addEmailAddress(data);
        } else {
            Toast.makeText(this, "فشل إضافة الحساب برجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
        }

    }

    public void addEmailAddress(Intent data) {
        showProgressBar();
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {

            final FirebaseAuth auth = FirebaseAuth.getInstance();

            // Google Sign In was successful, authenticate with Firebase
            final GoogleSignInAccount account = task.getResult(ApiException.class);
            final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> signUpTask) {
                            if (signUpTask.isSuccessful()) {
                                final String currentUserUid = auth.getCurrentUser().getUid();
                                // Sign in success, update UI with the signed-in user's information
                                fireStoreUsers.document(currentUserUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String email = auth.getCurrentUser().getEmail();

                                        if (documentSnapshot.exists()) {
                                            String databaseGender = documentSnapshot.getString("gender");
                                            if (databaseGender != null) {
                                                Toast.makeText(SignUpActivity.this, "هذا الحساب موجود بالفعل برجاء اختيار حساب اخر أو تسجيل الدخول", Toast.LENGTH_SHORT).show();
                                            } else {
                                                long points = documentSnapshot.getLong("points");
                                                setEmailEt(email);
                                                setPoints(points);
                                            }
                                        } else if (!documentSnapshot.exists()) {
                                            setEmailEt(email);
                                        }
                                        hideProgressBar();
                                        //  auth.signOut();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this, "فشل إضافة الحساب برجاء التأكد من الاتصال بالانترنت و إعادة المحاولة", Toast.LENGTH_SHORT).show();
                                        Log.v("signUpException", "exception is : " + e.getMessage());
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(SignUpActivity.this, "فشل التسجيل في التطبيق من فضلك تأكد من الإتصال بالإنترنت وأعد المحاولة", Toast.LENGTH_SHORT).show();
                                Log.v("sign in exception :", signUpTask.getException().toString());
                                hideProgressBar();
                                //updateUI(null);
                            }
                        }
                    });
        } catch (ApiException e) {
            Toast.makeText(SignUpActivity.this, "حدثت مشكلة أثناء محاولة التسجيل برجاء التأكد من الإتصال بالانترنت وإعادة المحاولة", Toast.LENGTH_SHORT).show();
            Log.v("Logging", "sign in exception : " + e.toString());
            hideProgressBar();
        }
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

    public void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
    }

    public void setNameError() {
        nameEt.setError(getString(R.string.emptyEditText));
        nameEt.requestFocus();
    }

    public void setEmailEt(String text) {
        emailEt.setText(text);
    }

    public void setPasswordError(String message) {
        passwordEt.setError(message);
        passwordEt.requestFocus();
    }

    public void setRePasswordError(String message) {
        rePasswordEt.setError(message);
        rePasswordEt.requestFocus();
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public void setUserTypeError() {
        Toast.makeText(this, "برجاء تحديد إذا كنت طالب أم معلم", Toast.LENGTH_SHORT).show();
    }
}
