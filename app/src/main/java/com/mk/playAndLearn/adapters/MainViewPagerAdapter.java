package com.mk.playAndLearn.adapters;

import androidx.fragment.app.Fragment;
import android.content.Context;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mk.playAndLearn.fragment.ChallengesFragment;
import com.mk.playAndLearn.fragment.HomeFragment;
import com.mk.playAndLearn.fragment.LessonsFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    Context context;

    public MainViewPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return 3;
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
                title = "تعلم";
                break;
            case 1:
                title = "الصفحة الرئيسية";
                break;
            case 2:
                title = "التحديات";
                break;
        }
        return title;
    }
    // END_INCLUDE (pageradapter_getpagetitle)

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LessonsFragment();
            case 1:
                return new HomeFragment();
            case 2:
                return new ChallengesFragment();
        }

        return null;
    }

}
