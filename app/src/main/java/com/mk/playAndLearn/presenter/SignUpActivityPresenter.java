package com.mk.playAndLearn.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.utils.DateClass;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivityPresenter {
    View view;
    Context context;

    String imageUrl;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersReference = database.getReference("users");

    public SharedPreferences pref; // 0 - for private mode
    SharedPreferences.Editor editor;

    public SignUpActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void checkEmailAndUploadData(Intent data, final String name, final String gender, final String schoolType
            , final String type, final String grade) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential)
                    .addOnCompleteListener((Activity) view, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final String currentUserUid = auth.getCurrentUser().getUid();
                                // Sign in success, update UI with the signed-in user's information
                                usersReference.child(currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {

                                        String databaseUid = (String) dataSnapshot.child("uid").getValue();

                                        if (dataSnapshot.exists() && databaseUid != null) {
                                            Toast.makeText(context, "هذا الحساب موجود بالفعل برجاء اختيار حساب اخر أو تسجيل الدخول", Toast.LENGTH_SHORT).show();
                                            view.hideProgressDialog();
                                        } else {
                                            final String email = auth.getCurrentUser().getEmail();
                                            imageUrl = auth.getCurrentUser().getPhotoUrl().toString();

                                            Date today = new Date();
                                            DateClass dateClass = new DateClass();
                                            dateClass.setDate(today);
                                            Log.v("dateLogging", "date is : " + dateClass.getDate());

                                            if (!dataSnapshot.exists()) {
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("refusedQuestions", 0);
                                                map.put("acceptedQuestions", 0);
                                                map.put("adminTeacher", false);
                                                map.put("adminTeacherSubject", "");
                                                map.put("adminStudent", false);
                                                map.put("generalAdmin", false);
                                                map.put("userName", name.trim());
                                                map.put("userImage", imageUrl);
                                                map.put("userEmail", email.trim());
                                                map.put("userType", type);
                                                map.put("pointsHistory", "");
                                                map.put("grade", grade);
                                                map.put("gender", gender);
                                                map.put("uid", currentUserUid);
                                                map.put("friends", "users: ");
                                                map.put("lastOnlineDate", dateClass.getDate());
                                                map.put("points", 0);
                                                map.put("userSchoolType", schoolType);

                                                // map.put("lastGeneralChallengeScore", 0);
                                                usersReference.child(currentUserUid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        setSharedPreferenceAndNavigate(name, grade);
                                                    }
                                                });
                                            }else if (dataSnapshot.exists()) {
                                                usersReference.child(currentUserUid).child("adminTeacherSubject").setValue("");
                                                usersReference.child(currentUserUid).child("adminTeacher").setValue(false);
                                                usersReference.child(currentUserUid).child("adminStudent").setValue(false);
                                                usersReference.child(currentUserUid).child("generalAdmin").setValue(false);
                                                usersReference.child(currentUserUid).child("userName").setValue(name);
                                                usersReference.child(currentUserUid).child("userImage").setValue(imageUrl);
                                                usersReference.child(currentUserUid).child("userEmail").setValue(email);
                                                usersReference.child(currentUserUid).child("userType").setValue(type);
                                                usersReference.child(currentUserUid).child("pointsHistory").setValue("");
                                                usersReference.child(currentUserUid).child("grade").setValue(grade);
                                                usersReference.child(currentUserUid).child("gender").setValue(gender);
                                                usersReference.child(currentUserUid).child("uid").setValue(currentUserUid);
                                                usersReference.child(currentUserUid).child("friends").setValue("users: ");
                                                usersReference.child(currentUserUid).child("lastOnlineDate").setValue(dateClass.getDate());
                                                usersReference.child(currentUserUid).child("userSchoolType").setValue(schoolType).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        setSharedPreferenceAndNavigate(name, grade);
                                                    }
                                                });


                                            }

                                        }
                                        auth.signOut();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(context, "فشل التسجيل في التطبيق من فضلك تأكد من الإتصال بالإنترنت وأعد المحاولة", Toast.LENGTH_SHORT).show();
                                Log.v("sign in exception :", task.getException().toString());
                                //updateUI(null);
                            }

                            // ...
                        }
                    });

            //Toast.makeText(this, "نجح تسجيل الدخول",Toast.LENGTH_SHORT).show();
        } catch (ApiException e) {
            Toast.makeText(context, "حدثت مشكلة أثناء محاولة التسجيل برجاء اعادة المحاولة", Toast.LENGTH_SHORT).show();
            Log.v("Logging", "sign in exception : " + e.toString());

        }
    }

    public void validateSignUp(final String name, final String type, boolean acceptTerms) {
        if (TextUtils.isEmpty(name)) {
            view.setNameError();
        }else if (!acceptTerms) {
            view.setAcceptTermsCheckedError();
        } else if (TextUtils.isEmpty(type)) {
            view.setUserTypeError();
        } else {
            view.signInWithGoogle(1);
        }

    }

    void setSharedPreferenceAndNavigate(String name, String grade) {
        pref = context.getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        editor.putString("currentUserName", name);
        editor.putString("grade", grade);
        editor.apply();

        view.navigate();
    }

    public interface View {

        void hideProgressDialog();


        void setNameError();

        void setAcceptTermsCheckedError();

        void setUserTypeError();

        void navigate();

        void signInWithGoogle(final int RC_SIGN_IN);
    }
}
