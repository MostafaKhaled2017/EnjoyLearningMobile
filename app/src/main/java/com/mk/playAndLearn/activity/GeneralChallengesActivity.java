package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.GeneralChallengesActivityPresenter;

public class GeneralChallengesActivity extends AppCompatActivity implements GeneralChallengesActivityPresenter.View {
    GeneralChallengesActivityPresenter presenter;

    ProgressBar progressBar;
    TextView challengeTextTv, noInternetConnection;
    TextView noInternetConnectionText;
    ProgressBar horizontalProgressBar;
    Button startForArabicBtn, startForLanguagesBtn;
    boolean arabicButtonPressed = false, languagesButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_challenges);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        presenter = new GeneralChallengesActivityPresenter(this);

        progressBar = findViewById(R.id.generalChallengesProgressBar);
        horizontalProgressBar = findViewById(R.id.horizontalProgressbar);
        challengeTextTv = findViewById(R.id.challengeTextTv);
        noInternetConnection = findViewById(R.id.noInternetConnectionText);
        startForArabicBtn = findViewById(R.id.startGeneralChallengeForArabicBtn);
        startForLanguagesBtn = findViewById(R.id.startGeneralChallengeForLanguagesBtn);

        startForArabicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!arabicButtonPressed) {
                    showHorizontalProgressBar();
                    Toast.makeText(GeneralChallengesActivity.this, "جارى إعداد الاسئلة", Toast.LENGTH_SHORT).show();
                    presenter.loadQuestions("arabic");
                    arabicButtonPressed = true;
                }
            }
        });

        startForLanguagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!languagesButtonPressed) {
                    showHorizontalProgressBar();
                    Toast.makeText(GeneralChallengesActivity.this, "جارى إعداد الاسئلة", Toast.LENGTH_SHORT).show();
                    presenter.loadQuestions("languages");
                    languagesButtonPressed = true;
                }
            }
        });


        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        presenter.startAsynkTask();
        presenter.prepareAd();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showHorizontalProgressBar() {
        if (horizontalProgressBar.getVisibility() != View.VISIBLE) {
            horizontalProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideHorizontalProgressBar() {
        if (horizontalProgressBar.getVisibility() == View.VISIBLE) {
            horizontalProgressBar.setVisibility(View.GONE);
        }
    }


    @Override
    public void retryConnection(){
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        presenter.startAsynkTask();
    }

    @Override
    public void setChallengeText(String text) {
        challengeTextTv.setText(text);
    }

    @Override
    public void onNoInternetConnection() {
        hideProgressBar();
        noInternetConnection.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hideButtonGroup() {
        startForArabicBtn.setVisibility(View.GONE);
        startForLanguagesBtn.setVisibility(View.GONE);
    }

    @Override
    public void showButtonGroup() {
        startForArabicBtn.setVisibility(View.VISIBLE);
        startForLanguagesBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideChallengeText() {
        if (challengeTextTv.getVisibility() == View.VISIBLE) {
            challengeTextTv.setVisibility(View.INVISIBLE);
        }
    }
}
