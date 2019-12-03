package com.mk.playAndLearn.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mk.playAndLearn.fragment.OnboardingFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    Context context;
    OnboardingFragment onboardingFragment;
    int count;


    public SectionsPagerAdapter(FragmentManager fragmentManager, Context context, OnboardingFragment onboardingFragment, int count) {
        super(fragmentManager);
        this.context = context;
        this.onboardingFragment = onboardingFragment;
        this.count = count;
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Fragment getItem(int position) {
        OnboardingFragment onboardingFragment = new OnboardingFragment(position);

        return onboardingFragment;
    }

}
