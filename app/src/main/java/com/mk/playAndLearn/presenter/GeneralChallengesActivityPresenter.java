package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.activity.GeneralChallengesActivity;
import com.mk.playAndLearn.activity.QuestionActivity;
import com.mk.playAndLearn.model.Question;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.utils.Firebase.generalChallengeReference;
import static com.mk.playAndLearn.utils.Firebase.questionsReference;

public class GeneralChallengesActivityPresenter {
    View view;
    Context context;

    ValueEventListener generalChallengeListener;
    ArrayList list = new ArrayList<>();

    public GeneralChallengesActivityPresenter(View view) {
        this.view = view;
        this.context = ((Context) view);
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
                    view.onNoInternetConnection();
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

                if (activeNow) {
                    view.showButtonGroup();
                    view.hideChallengeText();
                } else {
                    view.hideButtonGroup();
                }

                view.setChallengeText(challengeText);
                view.hideProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadQuestions(final String schoolType) {
        if (!list.isEmpty())
            list.clear();
       generalChallengeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Question>> t = new GenericTypeIndicator<List<Question>>() {};


                if(schoolType.equals("arabic"))
                    list = (ArrayList) dataSnapshot.child("arabicQuestions").getValue(t);

                if(schoolType.equals("languages"))
                    list = (ArrayList) dataSnapshot.child("languagesQuestions").getValue(t);

                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putParcelableArrayListExtra("questionList", list);
                intent.putExtra("questionNo", 0);
                intent.putExtra("score", 0);
                intent.putExtra("isGeneralChallenge", true);
                context.startActivity(intent);
                view.finishActivity();
                view.hideHorizontalProgressBar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public interface View {
        void hideProgressBar();

        void showHorizontalProgressBar();

        void hideHorizontalProgressBar();

        void setChallengeText(String text);

        void hideChallengeText();

        void onNoInternetConnection();

        void hideButtonGroup();

        void showButtonGroup();

        void finishActivity();
    }
}
