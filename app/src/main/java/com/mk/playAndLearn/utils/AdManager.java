package com.mk.playAndLearn.utils;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.mk.enjoylearning.R;

public class AdManager {
    private static AdManager singleton;
    InterstitialAd mInterstitialAd;

    public AdManager() {
    }

    public static AdManager getInstance() {
        if (singleton == null) {
            singleton = new AdManager();
        }

        return singleton;
    }

    public void createAd(Context context) {

        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.ad_mob_test_id));
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("106C57971641BAE74B5A237183F61E44")
                .build());
    }

    public InterstitialAd getAd() {
        return mInterstitialAd;
    }
}
