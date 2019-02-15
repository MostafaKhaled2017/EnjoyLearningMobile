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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.utils.DateClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.setSharedPreference;


public class SignUpActivityPresenter {
    View view;
    Context context;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public SharedPreferences pref; // 0 - for private mode

    public SignUpActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void addEmailAddress(Intent data) {
        view.showProgressBar();
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            final GoogleSignInAccount account = task.getResult(ApiException.class);
            final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential)
                    .addOnCompleteListener((Activity) view, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> signUpTask) {
                            view.hideProgressBar();
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
                                                Toast.makeText(context, "هذا الحساب موجود بالفعل برجاء اختيار حساب اخر أو تسجيل الدخول", Toast.LENGTH_SHORT).show();
                                            } else {
                                                view.setEmailEt(email);
                                            }
                                        } else if(!documentSnapshot.exists()){
                                            view.setEmailEt(email);
                                        }
                                        //  auth.signOut();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "فشل إضافة الحساب برجاء التأكد من الاتصال بالانترنت و إعادة المحاولة", Toast.LENGTH_SHORT).show();
                                        Log.v("signUpException", "exception is : " + e.getMessage());
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(context, "فشل التسجيل في التطبيق من فضلك تأكد من الإتصال بالإنترنت وأعد المحاولة", Toast.LENGTH_SHORT).show();
                                Log.v("sign in exception :", signUpTask.getException().toString());
                                //updateUI(null);
                            }
                        }
                    });
        } catch (ApiException e) {
            Toast.makeText(context, "حدثت مشكلة أثناء محاولة التسجيل برجاء التأكد من الإتصال بالانترنت وإعادة المحاولة", Toast.LENGTH_SHORT).show();
            Log.v("Logging", "sign in exception : " + e.toString());
            view.hideProgressBar();
        }
    }

    public void validateSignUpAndUploadData(final String name, final String email, final String password, String rePassword
            , final String gender, final String userSchoolType, final String userType, final String grade
            , boolean acceptTermsChecked) {
        if (TextUtils.isEmpty(name)) {
            view.setNameError();
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "برجاء إضافة البريد الالكترونى الخاص بك", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            view.setPasswordError(context.getApplicationContext().getString(R.string.emptyEditText));
        } else if (TextUtils.isEmpty(rePassword)) {
            view.setRePasswordError(context.getApplicationContext().getString(R.string.emptyEditText));
        } else if (!password.equals(rePassword)) {
            view.setRePasswordError("كلمة السر وتأكيد كلمة السر غير متطابقين");
        } else if (password.length() < 6) {
            view.setPasswordError("كلمة المرور يجب ألا تقل عن 6 حروف أو أرقام");
        } else if (TextUtils.isEmpty(userType)) {
            view.setUserTypeError();
        } else if (!acceptTermsChecked) {
            view.setAcceptTermsCheckedError();
        } else {
            view.showProgressBar();

            final String imageUrl = auth.getCurrentUser().getPhotoUrl().toString();
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            auth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    final String currentUserUid = auth.getCurrentUser().getUid();

                    Date today = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                    format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    final String todayDate = format.format(today);

                    final String email = auth.getCurrentUser().getEmail();

                    final DateClass dateClass = new DateClass();
                    dateClass.setDate(today);
                    Log.v("dateLogging", "date is : " + dateClass.getDate());

                    fireStoreUsers.document(currentUserUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if (documentSnapshot.exists()) {
                                String databaseGender = (String) documentSnapshot.getString("gender");
                                if (databaseGender != null) {
                                    Toast.makeText(context, "هذا الحساب موجود بالفعل برجاء اختيار حساب اخر أو تسجيل الدخول", Toast.LENGTH_SHORT).show();
                                } else {
                                    WriteBatch batch = fireStore.batch();
                                    DocumentReference currentUserDocument = fireStoreUsers.document(currentUserUid);

                                    batch.update(currentUserDocument, "admin", false);
                                    batch.update(currentUserDocument, "userName", name);
                                    batch.update(currentUserDocument, "userImage", imageUrl);
                                    batch.update(currentUserDocument, "userEmail", email);
                                    batch.update(currentUserDocument, "userType", userType);
                                    batch.update(currentUserDocument, "consecutiveDays", 1);
                                    batch.update(currentUserDocument, "pointsHistory", "");
                                    batch.update(currentUserDocument, "grade", grade);
                                    batch.update(currentUserDocument, "gender", gender);
                                    batch.update(currentUserDocument, "friends", "users: ");
                                    batch.update(currentUserDocument, "lastOnlineDate", dateClass.getDate());
                                    batch.update(currentUserDocument, "userSchoolType", userSchoolType);

                                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            setSharedPreference(context, name, grade, userSchoolType, userType, email, imageUrl, todayDate, 0);
                                            view.navigate();
                                        }
                                    });
                                }
                            } else {
                                if (!documentSnapshot.exists()) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("refusedQuestions", 0);
                                    map.put("acceptedQuestions", 0);
                                    map.put("userName", name.trim());
                                    map.put("admin", false);
                                    map.put("userEmail", email.trim());
                                    map.put("userImage", imageUrl);
                                    map.put("userType", userType);
                                    map.put("pointsHistory", "");
                                    map.put("grade", grade);
                                    map.put("gender", gender);
                                    map.put("friends", "users: ");
                                    map.put("lastOnlineDay", todayDate);
                                    map.put("points", 0);
                                    map.put("consecutiveDays", 1);
                                    map.put("userSchoolType", userSchoolType);

                                    // map.put("lastGeneralChallengeScore", 0);
                                    fireStoreUsers.document(currentUserUid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            setSharedPreference(context, name, grade, userSchoolType, userType, email, imageUrl, todayDate, 0);
                                            view.navigate();
                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "فشل إنشاء الحساب برجاء إعادة المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "فشل إنشاء الحساب برجاء إعادة المحاولة", Toast.LENGTH_SHORT).show();
                    view.hideProgressBar();
                    Log.v("signUpLinking", "exception is : " + e.getMessage());
                }
            });
        }
    }

    public interface View {

        void setNameError();

        void setPasswordError(String message);

        void setRePasswordError(String message);

        void setAcceptTermsCheckedError();

        void setUserTypeError();

        void navigate();

        void showProgressBar();

        void hideProgressBar();

        void setEmailEt(String text);

    }
}
