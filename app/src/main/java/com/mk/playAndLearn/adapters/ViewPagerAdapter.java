package com.mk.playAndLearn.adapters;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.MainActivity;
import com.mk.playAndLearn.fragment.ChallengesFragment;
import com.mk.playAndLearn.fragment.HomeFragment;
import com.mk.playAndLearn.fragment.LearnFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    Context context;

    public ViewPagerAdapter(FragmentManager fragmentManager, Context context) {
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
                return new LearnFragment();
            case 1:
                return new HomeFragment();
            case 2:
                return new ChallengesFragment();
        }

        return null;
    }

}
