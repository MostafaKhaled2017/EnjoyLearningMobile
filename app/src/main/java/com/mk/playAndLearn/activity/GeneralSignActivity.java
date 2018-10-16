package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
//TODO : solve the shared prefrence tutorial
//TODO : change the app in facebook for developers from in development to live
//TODO : remove the tradition sign in and sign up and think about removing sign in with facebook

public class GeneralSignActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    SignInButton button;
    private final static int RC_SIGN_IN = 2;
    private final String TAG = "Logging";
    CallbackManager mCallbackManager;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    DatabaseReference myRef;
    DatabaseReference currentUserReference;
    FirebaseDatabase database;
    Spinner userTypesSpinner;
    TextView unStudentSignAlertText;

    private GoogleSignInClient mGoogleSignInClient;
    String userName = "", userImage = "", userEmail = "", userType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_sign);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        button = findViewById(R.id.googleBtn);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        userTypesSpinner = findViewById(R.id.userTypesSpinner);
        unStudentSignAlertText = findViewById(R.id.unStudentSignAlertText);


        ArrayAdapter<CharSequence> userTypesAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_types_array, android.R.layout.simple_spinner_item);
        userTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypesSpinner.setAdapter(userTypesAdapter);

        userTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userType = adapterView.getItemAtPosition(i).toString();
                if (userType.equals("طالب")) {
                    unStudentSignAlertText.setVisibility(View.GONE);
                } else {
                    unStudentSignAlertText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(GeneralSignActivity.this, "حدث خطأ برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                                    navigate();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(GeneralSignActivity.this, "فشل التسجيل في التطبيق قد يكون لديك مشكلة في الإتصال بالانترنت أو أن إدارة البرنامج قامت بإلغاء تفعيل حسابك", Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });

                //Toast.makeText(this, "نجح تسجيل الدخول",Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                Toast.makeText(GeneralSignActivity.this, "حدثت مشكلة أثناء محاولة التسجيل برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();

            }
        }//TODO
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

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
                            //updateUI(null);
                        }

                        // ...
                    }
                });
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

    public void navigate() {
        FirebaseUser user = mAuth.getCurrentUser();
        userName = user.getDisplayName();
        userImage = user.getPhotoUrl().toString();
        userEmail = user.getEmail();
        assert user != null;


        Log.v("Logging", "user name is : " + userName
                + " user image is : " + userImage
                + " user email is : " + userEmail);

        /*SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        editor.putString("userName", userName);
        editor.putString("userImage", userImage);
        editor.putString("userEmail", userEmail);

        editor.apply();*/
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(mAuth.getUid()).exists()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userName", userName);
                    map.put("userImage", userImage);
                    map.put("userEmail", userEmail);
                    map.put("points", 0);
                    map.put("acceptedQuestions", 0);
                    map.put("refusedQuestions", 0);
                    map.put("acceptedLessons", 0);
                    map.put("refusedLessons", 0);
                    map.put("userType", userType);
                    myRef.child(mAuth.getUid()).setValue(map);
                }
                else {
                    //TODO : think about moving this part to welcome screen
                    currentUserReference =  myRef.child(mAuth.getUid());
                    currentUserReference.child("userName").setValue(userName);
                    currentUserReference.child("userImage").setValue(userImage);
                    currentUserReference.child("userType").setValue(userType);
                }
                startActivity(new Intent(GeneralSignActivity.this, MainActivity.class));
                myRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Toast.makeText(GeneralSignActivity.this, "data in sharedPrefrences : user name : " , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO : note : don't try to update the users data here again
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.v("GeneralSignActivity", "current user value is : " + currentUser
            +"current user Email is : " + currentUser.getEmail());
            startActivity(new Intent(GeneralSignActivity.this, MainActivity.class));
            //Toast.makeText(this, "mAuth : " + mAuth + " , current user : " + currentUser, Toast.LENGTH_SHORT).show();
        }
    }
}