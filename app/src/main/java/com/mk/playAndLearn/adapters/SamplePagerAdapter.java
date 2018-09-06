package com.mk.playAndLearn.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mk.enjoylearning.R;

public class SamplePagerAdapter extends PagerAdapter {
    Context context;

    public SamplePagerAdapter(Context context) {
        this.context = context;
    }

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return 3;
    }

    /**
     * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
     */
    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
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

    /**
     * Instantiate the {@link View} which should be displayed at {@code position}. Here we
     * inflate a layout from the apps resources and then change the text view to signify the position.
     */
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Inflate a new layout from our resources
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert li != null;
        View view = null;
        switch (position) {
            case 0:
                view = li.inflate(R.layout.fragment_learn, container, false);
                break;
            case 1:
                view = li.inflate(R.layout.fragment_home, container, false);
                break;
            case 2:
                view = li.inflate(R.layout.fragment_challenges, container, false);
                break;
        }
        // Add the newly created View to the ViewPager
        container.addView(view);

        // Return the View
        return view;
    }

    /**
     * {@link View}.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
