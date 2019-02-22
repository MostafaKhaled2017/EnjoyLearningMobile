package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;

//TODO : solve the shared prefrence tutorial
//TODO : change the app in facebook for developers from in development to live
//TODO : remove the tradition sign in and sign up and think about removing sign in with facebook

public class GeneralSignActivity extends AppCompatActivity {
    private final String TAG = "Logging";
    Button signUpActivity;
    TextView signInActivity;
    DatabaseReference currentUserReference;
    // Spinner userTypesSpinner, userSchoolTypeSpinner;
    LinearLayout userSchoolTypeLinearLayout;
    TextView unStudentSignAlertText;

    String userType = "", userSchoolType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_sign);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        signUpActivity = findViewById(R.id.signUpActivity);
        signInActivity = findViewById(R.id.signInActivity);
        // database.setPersistenceEnabled(false);
     /*   userTypesSpinner = findViewById(R.id.userTypesSpinner);
        userSchoolTypeSpinner = findViewById(R.id.userSchoolTypesSpinner);
        userSchoolTypeLinearLayout = findViewById(R.id.userSchoolTypeLinearLayout);
        unStudentSignAlertText = findViewById(R.id.unStudentSignAlertText);*/

        signUpActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GeneralSignActivity.this, SignUpActivity.class));
            }
        });

        signInActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GeneralSignActivity.this, SignInActivity.class));
            }
        });


        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        /* */



       /* mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebookBtn);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                Toast.makeText(GeneralSignActivity.this, "onSuccess" + loginResult, Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toast.makeText(GeneralSignActivity.this, "onCancel" , Toast.LENGTH_SHORT).show();

                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(GeneralSignActivity.this, "onError : " + error, Toast.LENGTH_SHORT).show();

                // ...
            }
        });*/

// ...


    }

    private void handleFacebookAccessToken(AccessToken token) {
       /* Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(GeneralSignActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                           // progressBar.setVisibility(View.GONE);
                            //updateUI(null);
                        }

                        // ...
                    }
                });*/
    }

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.mk.playAndLearn", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        //TODO : note : don't try to update the users data here again
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !getSavedGrade(this).equals("غير معروف")) {
            startActivity(new Intent(GeneralSignActivity.this, MainActivity.class));
        } else {
            if (currentUser != null) {
                FirebaseAuth.getInstance().signOut();
            }
        }
    }

    public void showDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);//TODO : check this
        android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

        FirebaseAuth localAuth = FirebaseAuth.getInstance();
        FirebaseUser localCurrentUser = localAuth.getCurrentUser();
        final DatabaseReference localUsersReference = FirebaseDatabase.getInstance().getReference("users");
        final String localCurrentUserUid = localCurrentUser.getUid();

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("تم", null)
                .create();

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("اكتب اسم المستخدم الذى تريده");


        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        final String commentText = inputComment.getText().toString();
                        if (TextUtils.isEmpty(commentText.trim())) {
                            inputComment.setError("لا يمكنك ترك هذا الحقل فارغا");
                        } else {
                            Toast.makeText(GeneralSignActivity.this, "جارى إعداد حسابك", Toast.LENGTH_SHORT).show();
                            localUsersReference.child(localCurrentUserUid).child("userName").setValue(commentText).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(GeneralSignActivity.this, "تم إضافة حسابك بنجاح", Toast.LENGTH_SHORT).show();
                                    // editor.putString("currentUserName", commentText.trim());
                                    //editor.apply();
                                    // i.putExtra("newUser", true);
                                    //  startActivity(i);

                                    //alertDialogBuilderUserInput.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        });

        alertDialogBuilderUserInput.show();

    }
}