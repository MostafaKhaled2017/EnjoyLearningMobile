package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;

public class SignInActivity extends AppCompatActivity {
    String userName = "", userImage = "", userEmail = "";
    GoogleApiClient mGoogleApiClient;
    SignInButton button;
    GoogleSignInOptions gso;
    CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    public SharedPreferences pref; // 0 - for private mode
    SharedPreferences.Editor editor;
    FirebaseDatabase database;
    //EditText emailEt, passwordEt;
    //TextView forgotPasswordTv;
    DatabaseReference myRef;
    Intent i;
    private final static int RC_SIGN_IN = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        pref = getSharedPreferences("MyPref", 0);
        progressBar = findViewById(R.id.progressbar);
        button = findViewById(R.id.googleBtn);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();

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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

     /*   forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEt.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignInActivity.this, "قم بكتابة بريدك الالكترونى", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignInActivity.this, "سيتم ارسال رسالة إلى بريدك الالكترونى", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(SignInActivity.this, "برجاء التأكد من بياناتك وإعادة المحاولة", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });*/

        mCallbackManager = CallbackManager.Factory.create();
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.VISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
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
                                    myRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(final DataSnapshot dataSnapshot) {
                                            String uid =(String) dataSnapshot.child("uid").getValue();
                                            if (!dataSnapshot.exists()) {
                                                Toast.makeText(SignInActivity.this, "هذا الحساب غير موجود برجاء تسجيل حساب جديد", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                hideProgressBar();
                                            }
                                            else if(uid == null){
                                                Toast.makeText(SignInActivity.this, "هذا الحساب بياناته غير مكتملة برجاء إضافة هذا الحساب من صفحة الاشتراك وإعادة المحاولة", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                hideProgressBar();
                                            }else {
                                                long grade = (long) dataSnapshot.child("grade").getValue();
                                                navigate(grade);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

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
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SignInActivity.this, "حدثت مشكلة أثناء محاولة التسجيل برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "sign in exception : " + e.toString());

            }
        }//TODO
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    void hideProgressBar(){
        if(progressBar.getVisibility() == View.VISIBLE)
        progressBar.setVisibility(View.GONE);
    }

    public void navigate(long grade) {
        FirebaseUser user = mAuth.getCurrentUser();
        userName = user.getDisplayName();
        userImage = user.getPhotoUrl().toString();
        userEmail = user.getEmail();
        i = new Intent(SignInActivity.this, MainActivity.class);

        editor = pref.edit();
        editor.putString("currentUserName", userName);
        editor.putLong("grade", grade);
        editor.apply();
        startActivity(i);
        hideProgressBar();
        finish();
        //Toast.makeText(GeneralSignActivity.this, "data in sharedPrefrences : user name : " , Toast.LENGTH_SHORT).show();
    }

   /* public void signInWithEmailAndPassword(View view) {
        final String writtenEmail = emailEt.getText().toString();
        final String writtenPassword = passwordEt.getText().toString();

        mAuth.signInWithEmailAndPassword(writtenEmail, writtenPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            myRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String uid = (String) dataSnapshot.child("uid").getValue();

                                    if(uid == null){
                                        Toast.makeText(SignInActivity.this, "هذا الحساب بياناته غير مكتملة برجاء إضافة هذا الحساب من صفحة الاشتراك وإعادة المحاولة", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                    else {
                                        navigate();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Toast.makeText(SignInActivity.this, "فشل تسجيل الدخول برجاء التأكد من الببيانات وإعادة المحاولة أو تسجيل الاشتراك إذا لم تكن قد قمت بتحديد كلمة السر قبل ذلك", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }*/
}
