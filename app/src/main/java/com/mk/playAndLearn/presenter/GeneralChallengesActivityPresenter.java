package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.activity.GeneralChallengesActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.mk.playAndLearn.utils.Firebase.generalChallengeReference;

public class GeneralChallengesActivityPresenter {
    View view;

    ValueEventListener generalChallengeListener;

    public GeneralChallengesActivityPresenter(View view) {
        this.view = view;
    }
    public void startAsynkTask() {
        //TODO : search for a solution to this error
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Boolean doInBackground(Object[] objects) {
                try {
                    Socket sock = new Socket();
                    sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                    sock.close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                if ((boolean) o) {
                    loadChallengeState();
                } else {
                    //view.onNoInternetConnection();TODO
                }
            }
        };

        asyncTask.execute();
    }

    public void loadChallengeState() {
        generalChallengeListener = generalChallengeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String challengeText = dataSnapshot.child("text").getValue().toString();
                boolean activeNow = (boolean) dataSnapshot.child("activeNow").getValue();

                Log.v("generalChallengeLogging","dataSnapShot is : " + dataSnapshot.toString() + "challengeText is : " + challengeText);

                view.setChallengeText(challengeText);
                view.hideProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeListener() {
        if (generalChallengeListener != null) {
            generalChallengeReference.removeEventListener(generalChallengeListener);
        }
    }

    public interface View {
        void hideProgressBar();

        void setChallengeText(String text);
    }
}
