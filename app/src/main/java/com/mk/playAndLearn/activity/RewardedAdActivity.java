package com.mk.playAndLearn.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.mk.enjoylearning.R;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.Integers.adReward;
import static com.mk.playAndLearn.utils.sharedPreference.setSavedPoints;

public class RewardedAdActivity extends AppCompatActivity{

    private RewardedVideoAd mRewardedVideoAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewarded_ad);
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);

        Button openAdBtn = findViewById(R.id.openAdBtn);
        openAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRewardedVideoAd();
                Toast.makeText(RewardedAdActivity.this, "جاري تحميل الإعلان", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.rewarded_ad_unit_id),
                new AdRequest.Builder()
                        .addTestDevice("B65A7976E9008CADC60414029149C78E")
                        .build());

        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                mRewardedVideoAd.show();
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                final DocumentReference currentUserReference = fireStoreUsers.document(currentUserUid);

                fireStore.runTransaction(new Transaction.Function<Long>() {
                    @Nullable
                    @Override
                    public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(currentUserReference);
                        long newPoints = snapshot.getLong("points") + adReward;

                        Long currentCompetitionPoints = snapshot.getLong("competitionPoints");
                        Log.v("competitionPointsLog", "currentCompetitionPoints is : " + currentCompetitionPoints);
                        if(currentCompetitionPoints == null)
                            currentCompetitionPoints = Long.valueOf(0);
                        long newCompetitionPoints = currentCompetitionPoints + adReward;
                        transaction.update(currentUserReference, "competitionPoints", newCompetitionPoints);

                        transaction.update(currentUserReference, "points", newPoints);
                        return newPoints;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        setSavedPoints(RewardedAdActivity.this, aLong);
                        Toast.makeText(RewardedAdActivity.this, "تم زيادة نقاطك بنجاح", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });
    }

    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }
}
