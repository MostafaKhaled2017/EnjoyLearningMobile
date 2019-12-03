package com.mk.playAndLearn.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.SectionsPagerAdapter;
import com.mk.playAndLearn.fragment.OnboardingFragment;

import static com.mk.playAndLearn.utils.sharedPreference.saveSharedSetting;

public class OnBoardingActivity extends AppCompatActivity implements OnboardingFragment.OnFragmentInteractionListener {
    ViewPager mViewPager;
    ImageButton mNextBtn;
    Button mFinishBtn;

    ImageView zero, one, two, three, four, five, six;
    ImageView[] indicators;

    SectionsPagerAdapter mSectionsPagerAdapter;

    int page = 0;
    int maxPageCount = 6;

    OnboardingFragment onboardingFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarging);

        mViewPager = findViewById(R.id.onboarding_container);
        mNextBtn = findViewById(R.id.intro_btn_next);
        mFinishBtn = findViewById(R.id.intro_btn_finish);

        onboardingFragment = new OnboardingFragment();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this, onboardingFragment, maxPageCount + 1);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        zero = findViewById(R.id.intro_indicator_0);
        one = findViewById(R.id.intro_indicator_1);
        two = findViewById(R.id.intro_indicator_2);
        three = findViewById(R.id.intro_indicator_3);
        four = findViewById(R.id.intro_indicator_4);
        five = findViewById(R.id.intro_indicator_5);
        six = findViewById(R.id.intro_indicator_6);


        //  CoordinatorLayout mCoordinator = (CoordinatorLayout) findViewById(R.id.main_content);

        indicators = new ImageView[]{zero, one, two, three, four, five, six};

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                mViewPager.setCurrentItem(page, true);
            }
        });

        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSharedSetting(OnBoardingActivity.this, MainActivity.PREF_USER_FIRST_TIME, "false");
                finish();
            }
        });



        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                page = position;
                updateIndicators(page);

                mNextBtn.setVisibility(position == maxPageCount ? View.GONE : View.VISIBLE);
                mFinishBtn.setVisibility(position == maxPageCount ? View.VISIBLE : View.GONE);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {

    }
}
