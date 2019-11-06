package com.mk.playAndLearn.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
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
        Log.v("adLog", "creating Ad");

        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.ad_mob_live_id));
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("106C57971641BAE74B5A237183F61E44")
                .build());

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.v("adLog", "Ad Loaded");

            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.v("adLog", "Ad failed to load , " + i);
            }

        });


    }

    public InterstitialAd getAd() {
        return mInterstitialAd;
    }
}
