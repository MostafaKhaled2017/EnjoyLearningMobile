package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.mk.enjoylearning.R;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.Integers.increaseValueForPoints;
import static com.mk.playAndLearn.utils.sharedPreference.setSavedPoints;

public class DailyRewardsActivity extends AppCompatActivity {
    long consecutiveDays = -1;
    TextView noOfDaysTv, increasePointsTv, dayRemainsToCompleteWeekTv;
    String currentUserUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_rewards);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            consecutiveDays = intent.getLongExtra("consecutiveDays", -1);
            currentUserUid = intent.getStringExtra("userUid");
        }

        noOfDaysTv = findViewById(R.id.numberOfDays);
        increasePointsTv = findViewById(R.id.increaseInPointsTv);
        dayRemainsToCompleteWeekTv = findViewById(R.id.dayRemainsToCompleteWeek);

        double periodNumber = Math.ceil((double) consecutiveDays / 6.0);
        final long dailyAddedPoints = (long) periodNumber * increaseValueForPoints;
        long nextPeriodNumber = (long) periodNumber + 1;
        long nextAddedPoints = nextPeriodNumber * increaseValueForPoints;
        long remainingDays = 7 - (consecutiveDays % 7);

        String numberOfDaysText = "أنت تستخدم البرنامج يوميا منذ " + consecutiveDays + "من الأيام";
        String increaseInPointsText = "لقد زادت نقاطك عدد " + dailyAddedPoints + " من النقاط";
        String dayRemainsToCompleteWeekText =  "باقى عدد " + remainingDays +
                 " من الأيام حتى تحصل على عدد" + nextAddedPoints + " من النقاط";

        noOfDaysTv.setText(numberOfDaysText);
        increasePointsTv.setText(increaseInPointsText);
        dayRemainsToCompleteWeekTv.setText(dayRemainsToCompleteWeekText);

        final DocumentReference currentUserReference = fireStoreUsers.document(currentUserUid);

        fireStore.runTransaction(new Transaction.Function<Long>() {
            @Nullable
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(currentUserReference);
                long newPoints = snapshot.getLong("points") + dailyAddedPoints;
                transaction.update(currentUserReference, "points", newPoints);
                return newPoints;
            }
        }).addOnSuccessListener(new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                setSavedPoints(DailyRewardsActivity.this, aLong);
            }
        });

    }
}
