package com.mk.playAndLearn.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mk.playAndLearn.fragment.FriendsFragment;
import com.mk.playAndLearn.fragment.LastChallengersFragment;

public class ChallengersViewPagerAdapter extends FragmentPagerAdapter {
    Context context;

    public ChallengersViewPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return 2;
    }

    // BEGIN_INCLUDE (pageradapter_getpagetitle)

    /**
     * Return the title of the item at {@code position}. This is important as what this method
     * <p>
     * Here we construct one using the position value, but for real application the title should
     * refer to the item's contents.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "كل الطلبة";
                break;
            case 1:
                title = "الأصدقاء";
                break;
        }
        return title;
    }
    // END_INCLUDE (pageradapter_getpagetitle)

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FriendsFragment();
            case 1:
                return new LastChallengersFragment();

        }

        return null;
    }

}
