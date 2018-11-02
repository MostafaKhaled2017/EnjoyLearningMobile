package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.utils.Firebase;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_welcome); TODO
        final Intent i = new Intent(this, GeneralSignActivity.class);
        //TODO : think about using that the data is stored with the app to make it able to works offline
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference localPostsReference = database.getReference("posts");
        DatabaseReference localCommentsReference = database.getReference("comments");
        DatabaseReference localLessonsReference = database.getReference("lessons");
        DatabaseReference localQuestionsReference = database.getReference("questions");
        DatabaseReference localChallengesReference = database.getReference("challenges");
        DatabaseReference localUsersReference = database.getReference("users");
        DatabaseReference localGeneralChallengeReference = database.getReference("generalChallenge");

            localPostsReference.keepSynced(true);
            localCommentsReference.keepSynced(true);
            localLessonsReference.keepSynced(true);
            localQuestionsReference.keepSynced(true);
            localGeneralChallengeReference.keepSynced(true);
            localChallengesReference.keepSynced(true); //TODO :think about removing this but note that caching isn't working
            localUsersReference.keepSynced(true);


        Thread timer = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
    }
}

