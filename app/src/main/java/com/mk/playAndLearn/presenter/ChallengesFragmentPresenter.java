package com.mk.playAndLearn.presenter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.service.NotificationsService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class ChallengesFragmentPresenter {
    Challenge challenge;
    View view;
    ArrayList<Challenge> completedChallengesList = new ArrayList<>(), uncompletedChallengesList = new ArrayList<>();
    int player1childrenCount = 0, player2childrenCount = 0, currentPlayer;
    ChildEventListener player1Listener, player2Listener;


    public ChallengesFragmentPresenter(View view) {
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
                    clearLists();
                    ChildEventListener generalChallengesListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            String challengeState = dataSnapshot.child("state").getValue().toString();
                            Log.v("Logging2", "onChildAdded");
                            if (challengeState.equals(uncompletedChallengeText)) {
                                getChallengeData(dataSnapshot, "onChildAdded");
                                view.onDataFound();
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            //TODO : note that the only changing handled is when challenge moves from uncompleted to completed state
                            getChallengeData(dataSnapshot, "onChildChanged");
                            for (int i = 0; i < uncompletedChallengesList.size(); i++) {
                                if (uncompletedChallengesList.get(i).getId().equals(dataSnapshot.getKey())) {
                                    uncompletedChallengesList.remove(i);
                                    view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size());
                                    break;
                                }
                            }
                            checkListsSizeAndAdjustViews();
                            view.hideProgressBar();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            startAsynkTask();
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //Toast.makeText(getActivity(), "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت", Toast.LENGTH_SHORT).show();
                            view.hideProgressBar();
                            Log.v("Logging", "error loading data : " + databaseError);
                        }
                    };

                    //this code gives data where current user is player 1
                    player1Listener = challengesReference.orderByChild("player1Uid").equalTo(currentUserUid).addChildEventListener(generalChallengesListener);
                    //this code gives data where current user is player 2
                    player2Listener = challengesReference.orderByChild("player2Uid").equalTo(currentUserUid).addChildEventListener(generalChallengesListener);

                    challengesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            onInitialDataLoaded();
                            Log.v("ChallengesFragPresenter", "completed list size :" + completedChallengesList + " , uncompleted list size : " + uncompletedChallengesList);
                            challengesReference.removeEventListener(this);
                        }


                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public String getChallengeData(DataSnapshot dataSnapshot, String tag) {
        Log.v("ChallengesFragPresenter", "get challenge data called");
        Log.v("ChallengesFragPresenter", "completedChallengesList : " + completedChallengesList.size()
                + ", uncompletedChallengesList " + uncompletedChallengesList.size());
        view.startCompletedChallengesAdapter(completedChallengesList);
        view.startUnCompletedChallengesAdapter(uncompletedChallengesList);

        GenericTypeIndicator<List<Question>> t = new GenericTypeIndicator<List<Question>>() {
        };
        challenge = new Challenge();
        String challengeDate = dataSnapshot.child("date").getValue().toString();
        String challengeSubject = dataSnapshot.child("subject").getValue().toString();
        String challengeState = dataSnapshot.child("state").getValue().toString();
        String challengeId = dataSnapshot.getKey();
        ArrayList challengeQuestionsList = (ArrayList) dataSnapshot.child("questionsList").getValue(t);
        long player1Score = (long) dataSnapshot.child("player1score").getValue();
        long player2Score = (long) dataSnapshot.child("player2score").getValue();

        String player1Name = dataSnapshot.child("player1Name").getValue().toString();
        String player1Image = dataSnapshot.child("player1Image").getValue().toString();
        String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
        String player2Name = dataSnapshot.child("player2Name").getValue().toString();
        String player2Image = dataSnapshot.child("player2Image").getValue().toString();
        String player2Uid = dataSnapshot.child("player2Uid").getValue().toString();

        String challengerName, challengerImage;

        if (player1Uid.equals(currentUserUid)) {
            currentPlayer = 1;
            challengerName = player2Name;
            challengerImage = player2Image;
            challenge.setSecondChallengerUid(player2Uid);//second means that it is not the player who starts the challenge
            if (tag.equals("onChildAdded")) {
                player1childrenCount++;
            }
        } else {
            currentPlayer = 2;
            challengerName = player1Name;
            challengerImage = player1Image;
            challenge.setSecondChallengerUid(player1Uid);//second means that it is not the player who starts the challenge
            if (tag.equals("onChildAdded")) {
                player2childrenCount++;
            }
        }
        challenge.setCurrentPlayer(currentPlayer);
        challenge.setChallengerName(challengerName);
        challenge.setDate(challengeDate);
        challenge.setImage(challengerImage);
        challenge.setSubject(challengeSubject);
        challenge.setState(challengeState);
        challenge.setId(challengeId);
        challenge.setQuestionsList(challengeQuestionsList);
        String score;
        if (currentPlayer == 1) {
            score = player2Score + " : " + player1Score;
        } else {
            score = player1Score + " : " + player2Score;
        }
        challenge.setScore(score);
        if (challenge.getState().equals("اكتمل")) {
            view.showCompletedChallengesTv();
            if(!completedChallengesList.contains(challenge)) {
                completedChallengesList.add(0, challenge);
            }
            view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size());
        } else if (challenge.getState().equals(refusedChallengeText)) {
            view.showCompletedChallengesTv();
            if(!completedChallengesList.contains(challenge)) {
                completedChallengesList.add(0, challenge);
            }
            view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size());
        } else if (challenge.getState().equals(uncompletedChallengeText)) {
            view.showUncompletedChallengesTv();
            if(!uncompletedChallengesList.contains(challenge)) {
                uncompletedChallengesList.add(0, challenge);
            }
            view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size());
        }

        return player1Uid;
    }

    private void checkListsSizeAndAdjustViews() {
        if (uncompletedChallengesList.size() == 0) {
            view.hideUncompletedChallengesTv();
        } else {
            view.showUncompletedChallengesTv();
        }
        if (completedChallengesList.size() == 0) {
            view.hideCompletedChallengesTv();
        } else {
            view.showCompletedChallengesTv();
        }
        if (completedChallengesList.size() == 0 && uncompletedChallengesList.size() == 0)
            view.showNoChallengesTv();
        else {
            view.hideNoChallengesTv();
        }
    }

    private void clearLists() {
        if (!completedChallengesList.isEmpty()) {
            completedChallengesList.clear();
            view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size());
        }
        if (!uncompletedChallengesList.isEmpty()) {
            uncompletedChallengesList.clear();
            view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size());
        }
    }

    public void removeListeners() {
        challengesReference.removeEventListener(player1Listener);
        challengesReference.removeEventListener(player2Listener);
    }

    public void onInitialDataLoaded() {
        view.hideProgressBar();
        checkListsSizeAndAdjustViews();
        view.startNotificationService(player1childrenCount, player2childrenCount);
    }

    public interface View {
        void navigate();

        void retryConnection();

        void startUnCompletedChallengesAdapter(ArrayList uncompletedChallengesList);

        void startCompletedChallengesAdapter(ArrayList completedChallengesList);

        void notifyAdapters(int completedListSize, int uncompletedListSize);

        void onDataFound();

        void hideProgressBar();

        void hideUncompletedChallengesTv();

        void hideCompletedChallengesTv();

        void showCompletedChallengesTv();

        void showUncompletedChallengesTv();

        void hideNoChallengesTv();

        void showNoChallengesTv();

        void startNotificationService(int player1childrenCount, int player2childrenCount);

        void onNoInternetConnection();
    }
}
