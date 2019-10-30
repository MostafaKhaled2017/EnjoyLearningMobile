package com.mk.playAndLearn.presenter;

import android.content.Context;
import androidx.annotation.NonNull;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;

public class ProfileActivityPresenter {
    View view;
    Context context;

    public ProfileActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void getUserData() {
        String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fireStoreUsers.document(localCurrentUserUid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userName = documentSnapshot.getString("userName");
                        String grade = documentSnapshot.getString("grade");
                        long points = documentSnapshot.getLong("points");
                        String schoolType = documentSnapshot.getString("userSchoolType");
                        String imageUrl = documentSnapshot.getString("userImage");
                        String email = documentSnapshot.getString("userEmail");
                        String governorate = documentSnapshot.getString("governorate");
                        String studentId = documentSnapshot.getId();

                        view.setData(userName, grade, points, schoolType, imageUrl, email, studentId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "لم نستطع تحميل بياناتك برجاء المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public interface View {
        void setData(String userName, String grade, long points, String schoolType, String imageUrl, String email, String studentId);
    }
}
