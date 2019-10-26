package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
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
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;

import jp.wasabeef.blurry.Blurry;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.setSharedPreference;

public class SignInActivity extends AppCompatActivity {
    String userName = "", userImage = "", userEmail = "";
    GoogleApiClient mGoogleApiClient;
    ImageView googleBtnIv;
    GoogleSignInOptions gso;
    CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    public SharedPreferences pref; // 0 - for private mode
    SharedPreferences.Editor editor;
    EditText emailEt, passwordEt;
    TextView forgotPasswordTv;
    ImageView backgroundIv;
    Intent i;
    private final static int RC_SIGN_IN = 2;
    private final String TAG = "SignInActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        pref = getSharedPreferences("MyPref", 0);
        progressBar = findViewById(R.id.progressbar);
        googleBtnIv = findViewById(R.id.googleBtn);
        mAuth = FirebaseAuth.getInstance();

        emailEt = findViewById(R.id.etEmailSignIn);
        passwordEt = findViewById(R.id.etPasswordSignIn);
        forgotPasswordTv = findViewById(R.id.tvForgotPassword);

        initializeGoogleLoginVariables();

        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEt.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    emailEt.setError("قم بكتابة بريدك الالكترونى");
                } else {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignInActivity.this, "سيتم إرسال رسالة إلى بريدك الالكترونى", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignInActivity.this, "برجاء التأكد من بياناتك وإعادة المحاولة", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });


        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        googleBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        Button signWithEmailAndPasswordBtn = findViewById(R.id.signInWithEmailAndPasswordBtn);
        signWithEmailAndPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailEt.getText().toString();
                String password = passwordEt.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    emailEt.setError(getString(R.string.emptyEditText));
                } else if (TextUtils.isEmpty(password)) {
                    passwordEt.setError(getString(R.string.emptyEditText));
                } else {
                    showProgressBar();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        // Sign in success, update UI with the signed-in user's information
                                        fireStoreUsers.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    long todayChallengesNo = 0, points = 0;

                                                    String name = document.getString("userName");
                                                    String grade = document.getString("grade");
                                                    String schoolType = document.getString("userSchoolType");
                                                    String type = document.getString("userType");
                                                    String email = document.getString("userEmail");
                                                    String image = document.getString("userImage");
                                                    String lastOnlineDay = document.getString("lastOnlineDay");
                                                    String phoneNo = document.getString("phoneNo");
                                                    String governorate = document.getString("governorate");

                                                    if (document.getLong("todayChallengesNo") != null)
                                                        todayChallengesNo = document.getLong("todayChallengesNo");

                                                    if (document.getLong("points") != null)
                                                        points = document.getLong("points");

                                                    Log.v("todayChallengesNo", "todayChallengesNo in sign in activity : " + todayChallengesNo);

                                                    setSharedPreference(SignInActivity.this, name, grade, schoolType, type, email
                                                            , image, lastOnlineDay, todayChallengesNo, points, phoneNo, governorate);
                                                    navigate();
                                                } else {
                                                    Toast.makeText(SignInActivity.this, "يوجد مشكلة فى هذا الحساب برجاء التواصل مع إدارة التطبيق", Toast.LENGTH_SHORT).show();
                                                    hideProgressBar();
                                                }
                                            }
                                        });
                                    } else {
                                        hideProgressBar();
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidUserException e) {
                                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                            if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                                                emailEt.setError("هذا الحساب غيرموجود فى التطبيق");
                                            }
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(SignInActivity.this, "البيانات التى أدخلتها غير صحيحة", Toast.LENGTH_SHORT).show();
                                        } catch (FirebaseNetworkException e) {
                                            Toast.makeText(SignInActivity.this, "لا يوجد اتصال بالانترنت", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(SignInActivity.this, "فشل التسجيل في التطبيق رجاء إعادة المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }

                                        // If sign in fails, display a message to the user.
                                        Log.v("sign in exception :", task.getException().toString());
                                        //updateUI(null);
                                    }

                                    // ...
                                }
                            });
                }
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
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
                        Toast.makeText(SignInActivity.this, "حدث خطأ برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.VISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            showProgressBar();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    //TODO : edit this and don't make it load all data
                                    fireStoreUsers.document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (!documentSnapshot.exists()) {
                                                Toast.makeText(SignInActivity.this, "هذا الحساب غير موجود برجاء تسجيل حساب جديد", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                hideProgressBar();
                                            } else {
                                                String databaseGender = documentSnapshot.getString("gender");
                                                long todayChallengesNo = 0, points = 0;

                                                if (databaseGender == null) {
                                                    Toast.makeText(SignInActivity.this, "هذا الحساب بياناته غير مكتملة برجاء إضافة هذا الحساب من صفحة الاشتراك وإعادة المحاولة", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut();
                                                    hideProgressBar();
                                                } else {
                                                    String name = documentSnapshot.getString("userName");
                                                    String grade = documentSnapshot.getString("grade");
                                                    String schoolType = documentSnapshot.getString("userSchoolType");
                                                    String type = documentSnapshot.getString("userType");
                                                    String email = documentSnapshot.getString("userEmail");
                                                    String image = documentSnapshot.getString("userImage");
                                                    String lastOnlineDay = documentSnapshot.getString("lastOnlineDay");
                                                    String phoneNo = documentSnapshot.getString("phoneNo");
                                                    String governorate = documentSnapshot.getString("governorate");
                                                    if (documentSnapshot.getLong("todayChallengesNo") != null)
                                                        todayChallengesNo = documentSnapshot.getLong("todayChallengesNo");
                                                    if (documentSnapshot.getLong("points") != null)
                                                        points = documentSnapshot.getLong("points");

                                                    Log.v("todayChallengesNo", "todayChallengesNo in sign in activity : " + todayChallengesNo);

                                                    setSharedPreference(SignInActivity.this, name, grade, schoolType, type, email, image, lastOnlineDay, todayChallengesNo, points, phoneNo, governorate);
                                                    navigate();
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignInActivity.this, "فشل تسجيل الدخول برجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignInActivity.this, "فشل التسجيل في التطبيق من فضلك تأكد من الإتصال بالإنترنت وأعد المحاولة", Toast.LENGTH_SHORT).show();
                                    hideProgressBar();
                                    Log.v("sign in exception :", task.getException().toString());
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });

                //Toast.makeText(this, "نجح تسجيل الدخول",Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                hideProgressBar();
                Toast.makeText(SignInActivity.this, "حدثت مشكلة أثناء محاولة التسجيل برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "sign in exception : " + e.toString());

            }
        }//TODO

        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
    }

    void showProgressBar() {
        if (progressBar.getVisibility() != View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE);
    }

    public void navigate() {
        i = new Intent(SignInActivity.this, MainActivity.class);

        startActivity(i);
        finish();
        // hideProgressBar();
        //Toast.makeText(GeneralSignActivity.this, "data in sharedPrefrences : user name : " , Toast.LENGTH_SHORT).show();
    }

}
