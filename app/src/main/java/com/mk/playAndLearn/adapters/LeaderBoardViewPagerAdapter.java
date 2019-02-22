package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mk.playAndLearn.fragment.BestQuestionsUploadersFragment;
import com.mk.playAndLearn.fragment.BestStudentsFragment;

public class LeaderBoardViewPagerAdapter extends FragmentPagerAdapter {
    Context context;

    public LeaderBoardViewPagerAdapter(FragmentManager fragmentManager, Context context) {
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
                title = "النقاط";
                break;
            case 1:
                title = "الأسئلة";
                break;
        }
        return title;
    }
    // END_INCLUDE (pageradapter_getpagetitle)

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BestStudentsFragment();
            case 1:
                return new BestQuestionsUploadersFragment();
        }

        return null;
    }

}
