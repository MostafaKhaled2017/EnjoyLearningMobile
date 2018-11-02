package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.GeneralChallengesActivityPresenter;

public class GeneralChallengesActivity extends AppCompatActivity implements GeneralChallengesActivityPresenter.View{
    GeneralChallengesActivityPresenter presenter;

    ProgressBar progressBar;
    TextView challengeTextTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_challenges);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("تحديات عامة");

        presenter = new GeneralChallengesActivityPresenter(this);

        progressBar = findViewById(R.id.generalChallengesProgressBar);
        challengeTextTv = findViewById(R.id.challengeTextTv);

        presenter.startAsynkTask();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.removeListener();
    }

    @Override
    public void hideProgressBar() {
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void setChallengeText(String text) {
        challengeTextTv.setText(text);
    }
}
