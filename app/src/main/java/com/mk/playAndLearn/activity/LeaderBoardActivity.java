package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.LeaderBoardViewPagerAdapter;
import com.mk.playAndLearn.adapters.MainViewPagerAdapter;

public class LeaderBoardActivity extends AppCompatActivity {
    LeaderBoardViewPagerAdapter adapter;
    private ViewPager mViewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mViewPager = findViewById(R.id.viewpager_in_leader_board);
        adapter = new LeaderBoardViewPagerAdapter(getSupportFragmentManager(), this);
        tabLayout = findViewById(R.id.tablayout_in_leader_board);

        mViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(mViewPager);

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("النقاط");
        tabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("الأسئلة");
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
