package com.mk.playAndLearn.presenter;

import android.content.Context;
import androidx.annotation.NonNull;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedEmail;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedId;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedImage;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedPoints;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedSchoolType;

public class ProfileActivityPresenter {
    View view;
    Context context;

    public ProfileActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void getUserData() {
        String userName = getSavedName(context);
        String grade =getSavedGrade(context);
        long points = getSavedPoints(context);
        String schoolType = getSavedSchoolType(context);
        String imageUrl =getSavedImage(context);
        String email = getSavedEmail(context);
        String studentId = getSavedId(context);

        view.setData(userName, grade, points, schoolType, imageUrl, email, studentId);
    }

    public interface View {
        void setData(String userName, String grade, long points, String schoolType, String imageUrl, String email, String studentId);
    }
}
