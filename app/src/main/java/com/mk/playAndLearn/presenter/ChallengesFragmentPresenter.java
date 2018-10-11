package com.mk.playAndLearn.presenter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.mk.playAndLearn.model.Challenge;

public class ChallengesFragmentPresenter {
    private Challenge challenge;
    private View view;

    ChallengesFragmentPresenter(View view){
        this.challenge = new Challenge();
        this.view = view;
    }

    public interface View {
        void setFragment(Fragment fragment);

    }
}
