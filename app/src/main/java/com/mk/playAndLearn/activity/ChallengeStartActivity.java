package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.ChallengeStartActivityPresenter;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChallengeStartActivity extends AppCompatActivity implements ChallengeStartActivityPresenter.View {
    //TODO : make this page loads until all data finished loading by hiding the 4 main views until the data loads
    //TODO : make the app loads only five questions from the database
    ChallengeStartActivityPresenter presenter;

    Context context;

    boolean previousLessonsIncluded;

    ImageView player1ImageIv, player2Image;
    TextView player1NameTv, player1PointsTv, player2Name, player2Points;
    Button startChallengeButton;
    ProgressBar horizontalProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
       /* final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(upArrow);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);*/


        player1NameTv = findViewById(R.id.firstPlayerName);
        player1ImageIv = findViewById(R.id.firstPlayerImage);
        player1PointsTv = findViewById(R.id.firstPlayerPoints);
        player2Name = findViewById(R.id.secondPlayerName);
        player2Image = findViewById(R.id.secondPlayerImage);
        player2Points = findViewById(R.id.secondPlayerPoints);
        startChallengeButton = findViewById(R.id.lastStartChallengeButton);
        horizontalProgressBar = findViewById(R.id.horizontalProgressbarInChallengeStartActivity);

        context = this;

        presenter = new ChallengeStartActivityPresenter(this);

        presenter.prepareAd();

        final Intent intent = getIntent();

        presenter.getCurrentPlayerData();//currentPlayer is player1
        presenter.getOpponentData(intent); //opponentData is player2

        startChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setQuestionsListAndNavigate(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void showProgressBar() {
        horizontalProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        horizontalProgressBar.setVisibility(View.GONE);
    }


    @Override
    public void showCurrentPlayerData(String currentPlayerName, String currentPlayerImage, int currentPlayerPoints) {
        player1NameTv.setText(currentPlayerName);
        Picasso.with(this).load(currentPlayerImage).placeholder(R.drawable.picasso_placeholder).into(player1ImageIv);
        player1PointsTv.setText(currentPlayerPoints + " XP");
    }

    @Override
    public void showOpponentData(String opponentName, String opponentImage, int opponentPoints) {
        player2Name.setText(opponentName);
        Picasso.with(ChallengeStartActivity.this).load(opponentImage).into(player2Image);
        player2Points.setText(opponentPoints + " XP");
    }

    public void finishch(View view) {

        finish();
    }
}
