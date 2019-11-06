package com.mk.playAndLearn.activity;

import android.content.IntentSender;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mk.enjoylearning.R;

public class TestLeaderboardActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;

    public boolean accountConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_leaderboard);

        //TODO : check if google play services is not available

    /*    mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES_LITE)
                .useDefaultAccount()
                .build();*/

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addScope(Games.SCOPE_GAMES_LITE)
                .build();*/

        mGoogleApiClient.connect();

    }

    public void submitScore(long score) {
     /*   if (mGoogleApiClient.isConnected()) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if(account != null) {
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .submitScore(getString(R.string.leaderboard_id), score);
                Toast.makeText(this, "submitting score called", Toast.LENGTH_SHORT).show();
            } else {
                mGoogleApiClient.connect();
                Toast.makeText(this, "Else called", Toast.LENGTH_SHORT).show();
            }
          /*  Games.Leaderboards.submitScore(mGoogleApiClient, String.valueOf(R.string.leaderboard_id), score);
            Games.Leaderboards.submitScoreImmediate(mGoogleApiClient, String.valueOf(R.string.leaderboard_id), score);
        } else {
            Toast.makeText(this, "unable to submit high score", Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        submitScore(1000);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                Toast.makeText(this, "trying to reconnect", Toast.LENGTH_SHORT).show();
                connectionResult.startResolutionForResult(this, 0);
                mGoogleApiClient.connect();
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(this, "excetion is : " + e.getMessage() + " , " + e.getCause().toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "else", Toast.LENGTH_SHORT).show();
            GooglePlayServicesUtil.showErrorDialogFragment(connectionResult.getErrorCode(), this, 0);
        }
    }
}
