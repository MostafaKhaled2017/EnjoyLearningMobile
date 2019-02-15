package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.presenter.ChallengeResultActivityPresenter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChallengeResultActivity extends AppCompatActivity implements ChallengeResultActivityPresenter.View {
    //TODO : think about removing challenge result activity but think well before determine what to do in this
    //TODO : handle loosing internet connection before uploading data for example show a dialog when try to go out.

    String subject, challengeId, currentUserName;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints;
    int score, currentChallenger;
    long opponentScore;

    ArrayList questionsList = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "", correctAnswersList = "";
    boolean isGeneralChallenge;

    public SharedPreferences pref; // 0 - for private mode

    ImageView player1ImageTv, player2ImageTv;
    TextView player1NameTv, player1ScoreTv, player2NameTv, player2ScoreTv;
    TextView challengeResultTv, challengeStateTv, againstTv;

    ChallengeResultActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        presenter = new ChallengeResultActivityPresenter(this, this);

        player1NameTv = findViewById(R.id.firstPlayerName);
        player1ImageTv = findViewById(R.id.firstPlayerImage);
        player1ScoreTv = findViewById(R.id.firstPlayerScore);
        player2NameTv = findViewById(R.id.secondPlayerName);
        player2ImageTv = findViewById(R.id.secondPlayerImage);
        player2ScoreTv = findViewById(R.id.secondPlayerScore);
        challengeResultTv = findViewById(R.id.againstChallengeTv);
        challengeStateTv = findViewById(R.id.challengeStateInResultActivity);
        againstTv = findViewById(R.id.againstTextView);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        currentUserName = pref.getString("currentUserName", "غير معروف");
        Log.v("sharedPreference", " current userName is : " + currentUserName);

        final String localCurrentUserImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();

        presenter.showAd();

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            getDataFromIntent(intent);
        }

        setCurrentPlayerData(localCurrentUserImage);

        if (currentChallenger == 1) {
            player2ScoreTv.setText(0 + "");
            player2NameTv.setText(secondPlayerName);
            Picasso.with(ChallengeResultActivity.this).load(secondPlayerImage).into(player2ImageTv);
            challengeStateTv.setText("فى إنتظار المنافس");
        } else if (currentChallenger == 2 && !isGeneralChallenge) {
            presenter.downloadOpponentDataAndDetermineChallengeResult(challengeId, score);
        }


        if (!isGeneralChallenge) {
            if (currentChallenger == 1) {

                presenter.uploadPlayer1Data(
                        correctAnswersList, playerAnswersList, secondPlayerUid
                        , score, currentUserName, localCurrentUserImage, secondPlayerName, secondPlayerImage, questionsList, subject);

            } else if(currentChallenger == 2){
                presenter.uploadPlayer2DataAndUpdateUsersData(challengeId, score, playerAnswersList, currentChallenger);
            }
        } else if(isGeneralChallenge){
            challengeResultTv.setVisibility(View.VISIBLE);
            hideAllViews();

            presenter.uploadGeneralChallengeData(score);
        }
    }

    void getDataFromIntent(Intent intent){
        score = intent.getIntExtra("score", -1);
        isGeneralChallenge = intent.getBooleanExtra("isGeneralChallenge", false);
        if (!isGeneralChallenge) {
            currentChallenger = intent.getIntExtra("currentChallenger", currentChallenger);
            subject = intent.getStringExtra("subject");
            playerAnswersBooleansList = intent.getStringExtra("currentPlayerAnswersBooleans");
            playerAnswersList = intent.getStringExtra("currentPlayerAnswers");
            correctAnswersList = intent.getStringExtra("correctAnswers");
        }

        if (currentChallenger == 1) {
            secondPlayerName = intent.getStringExtra("player2Name");
            secondPlayerEmail = intent.getStringExtra("player2Email");
            secondPlayerImage = intent.getStringExtra("player2Image");
            secondPlayerUid = intent.getStringExtra("player2Uid");
            secondPlayerUid = intent.getStringExtra("player2Uid");
            secondPlayerPoints = intent.getIntExtra("player2Points", -1);
            questionsList = intent.getParcelableArrayListExtra("questionsList");
        } else if (currentChallenger == 2) {
            challengeId = intent.getStringExtra("challengeId");
        }
    }

    void setCurrentPlayerData(String localCurrentUserImage){
        player1NameTv.setText(currentUserName);
        Picasso.with(this).load(localCurrentUserImage).into(player1ImageTv);
        player1ScoreTv.setText(score + "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    void hideAllViews() {
        player1NameTv.setVisibility(View.GONE);
        player1ImageTv.setVisibility(View.GONE);
        player1ScoreTv.setVisibility(View.GONE);
        player2NameTv.setVisibility(View.GONE);
        player2ImageTv.setVisibility(View.GONE);
        player2ScoreTv.setVisibility(View.GONE);
        challengeStateTv.setVisibility(View.GONE);
        againstTv.setVisibility(View.GONE);
    }

    @Override
    public void setOpponentData(long opponentScore, String opponentName, String opponentImage) {
        player2ScoreTv.setText(opponentScore + "");
        player2NameTv.setText(opponentName);
        Picasso.with(this).load(opponentImage).into(player2ImageTv);
    }

    @Override
    public void setChallengeTvText(String text) {
        challengeStateTv.setText(text);
    }

    @Override
    public void setChallengeTvBGColor(int color) {
        challengeStateTv.setBackgroundColor(color);
    }
}
