package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
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

    String phoneNo;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public SharedPreferences pref; // 0 - for private mode

    public SignUpActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void validateSignUpAndUploadData(final String name, final String email, final String password
            , final String gender, final String userType, final long points, final String grade, final String userSchoolType
            , final String writtenPhoneNumber, final String governorate, final String invitingFriendId) {
        view.showProgressBar();

        final String imageUrl = auth.getCurrentUser().getPhotoUrl().toString();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        auth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                final String currentUserUid = auth.getCurrentUser().getUid();

                phoneNo = writtenPhoneNumber;

                FirebaseUser user = auth.getCurrentUser();

                String gmailPhoneNo = auth.getCurrentUser().getPhoneNumber();

                if (TextUtils.isEmpty(writtenPhoneNumber) || writtenPhoneNumber.length() < 11) {
                    if (gmailPhoneNo != null) {
                        phoneNo = "gmail Phone Number : " + gmailPhoneNo;
                    } else {
                        for (UserInfo userInfo : user.getProviderData()) {
                            if (userInfo.getPhoneNumber() != null) {
                                phoneNo = "gmail Phone Number : " + userInfo.getPhoneNumber();
                            }
                        }
                    }
                }

                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                final String todayDate = format.format(date);

                final String email = auth.getCurrentUser().getEmail();

                final DateClass dateClass = new DateClass();
                dateClass.setDate(date);
                Log.v("dateLogging", "date is : " + dateClass.getDate());

                fireStoreUsers.document(currentUserUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            String databaseGender = (String) documentSnapshot.getString("gender");
                            if (databaseGender != null) {
                                Toast.makeText(context, "هذا الحساب موجود بالفعل برجاء اختيار حساب اخر أو تسجيل الدخول", Toast.LENGTH_SHORT).show();
                                view.hideProgressBar();
                                view.enableBtn();
                            } else {
                                WriteBatch batch = fireStore.batch();
                                DocumentReference currentUserDocument = fireStoreUsers.document(currentUserUid);

                                batch.update(currentUserDocument, "admin", false);
                                batch.update(currentUserDocument, "userName", name);
                                batch.update(currentUserDocument, "userImage", imageUrl);
                                batch.update(currentUserDocument, "userEmail", email);
                                batch.update(currentUserDocument, "userType", userType);
                                batch.update(currentUserDocument, "pointsHistory", "");
                                batch.update(currentUserDocument, "grade", grade);
                                batch.update(currentUserDocument, "gender", gender);
                                batch.update(currentUserDocument, "friends", "users: ");
                                batch.update(currentUserDocument, "lastOnlineDay", todayDate);
                                batch.update(currentUserDocument, "userSchoolType", userSchoolType);
                                batch.update(currentUserDocument, "consecutiveDays", 1);
                                batch.update(currentUserDocument, "phoneNo", phoneNo);
                                batch.update(currentUserDocument, "governorate", governorate);
                                batch.update(currentUserDocument, "invitingFriendId", invitingFriendId);
                                batch.update(currentUserDocument, "competitionPoints", 0);
                                batch.update(currentUserDocument, "inviteesNo", 0);
                                batch.update(currentUserDocument, "ambassador", false);

                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        setSharedPreference(context, name, grade, userSchoolType, userType, email, imageUrl, todayDate, 0, points, phoneNo, governorate);
                                        view.navigate();
                                    }
                                });
                            }
                        } else if (!documentSnapshot.exists()) {
                            String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            Map<String, Object> map = new HashMap<>();
                            map.put("refusedQuestions", 0);//1
                            map.put("acceptedQuestions", 0);//2
                            map.put("userName", name.trim());
                            map.put("admin", false);
                            map.put("ambassador", false);
                            map.put("userEmail", email.trim());
                            map.put("userId", currentUserUid);
                            map.put("userImage", imageUrl);
                            map.put("userType", userType);
                            map.put("pointsHistory", "");
                            map.put("grade", grade);
                            map.put("gender", gender);
                            map.put("friends", "users: ");
                            map.put("lastOnlineDay", todayDate);
                            map.put("creationDate", dateClass.getDate());
                            map.put("points", 0);//3
                            map.put("competitionPoints", 0);
                            map.put("inviteesNo", 0);
                            map.put("noOfDraws", 0);
                            map.put("noOfWins", 0);
                            map.put("noOfLoses", 0);
                            map.put("consecutiveDays", 1);
                            map.put("userSchoolType", userSchoolType);
                            map.put("phoneNo", phoneNo);
                            map.put("governorate", governorate);
                            map.put("invitingFriendId", invitingFriendId);

                            // map.put("lastGeneralChallengeScore", 0);
                            fireStoreUsers.document(currentUserUid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        setSharedPreference(context, name, grade, userSchoolType, userType, email, imageUrl, todayDate, 0, 0, phoneNo, governorate);
                                        view.navigate();
                                    } else {
                                        Toast.makeText(context, "لم يتم إنشاء الحساب الجديد برجاء المحاولة فى وقت لاحق", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "فشل إنشاء الحساب برجاء إعادة المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                        view.hideProgressBar();
                        view.enableBtn();
                        // Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "فشل إنشاء الحساب برجاء المحاولة مرة أخرى", Toast.LENGTH_SHORT).show();
                // Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                view.hideProgressBar();
                view.enableBtn();
                Log.v("signUpLinking", "exception is : " + e.getMessage());
            }
        });
    }

    public interface View {

        void navigate();

        void showProgressBar();

        void hideProgressBar();

        void setPoints(long points);

        void disableBtn();

        void enableBtn();
    }
}
