package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.ChallengersViewPagerAdapter;

public class ChallengersActivity extends AppCompatActivity {
    ChallengersViewPagerAdapter adapter;
    private ViewPager mViewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challengers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mViewPager = findViewById(R.id.viewpager_in_challengers);
        adapter = new ChallengersViewPagerAdapter(getSupportFragmentManager(), this);
        tabLayout = findViewById(R.id.tablayout_in_challengers);

        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("كل الطلبة");
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("الأصدقاء");
        tabLayout.getTabAt(1).setCustomView(tabTwo);
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
