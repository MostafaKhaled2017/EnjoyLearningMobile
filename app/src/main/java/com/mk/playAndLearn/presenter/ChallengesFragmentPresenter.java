package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class ChallengesFragmentPresenter {
    Challenge challenge;
    View view;
    ArrayList<Challenge> completedChallengesList = new ArrayList<>(), uncompletedChallengesList = new ArrayList<>();
    int player1childrenCount = 0, player2childrenCount = 0, currentPlayer;
    ChildEventListener player1Listener, player2Listener;
    int no = 0;
    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


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
                    recreatingLists();
                    ChildEventListener generalChallengesListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.v("onChildAdded", "child added");
                            getChallengeData(dataSnapshot, "onChildAdded");
                            view.onDataFound();

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            //TODO : note that the only changing handled is when challenge moves from uncompleted to completed state
                            for (int i = 0; i < uncompletedChallengesList.size(); i++) {
                                if (uncompletedChallengesList.get(i).getId().equals(dataSnapshot.getKey())) {
                                    uncompletedChallengesList.remove(i);
                                    Log.v("challenge Removed", "challengeRemoved");
                                    getChallengeData(dataSnapshot, "onChildChanged");
                                    view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "onChildChanged");
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
                    player1Listener = challengesReference.orderByChild("player1Uid").equalTo(localCurrentUserUid).addChildEventListener(generalChallengesListener);
                    //this code gives data where current user is player 2
                    player2Listener = challengesReference.orderByChild("player2Uid").equalTo(localCurrentUserUid).addChildEventListener(generalChallengesListener);

                    challengesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            onInitialDataLoaded();

                            Log.v("ChallengesFragPresenter", "completed list size :" + completedChallengesList + " , uncompleted list size : " + uncompletedChallengesList);
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

        if (player1Uid.equals(localCurrentUserUid)) {
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
        challenge.setPlayer1Score(player1Score);
        challenge.setPlayer2Score(player2Score);
        challenge.setQuestionsList(challengeQuestionsList);
        String score;
        if (currentPlayer == 1) {
            score = player1Score + " : " + player2Score;
        } else {
            score = player2Score + " : " + player1Score;
        }
        challenge.setScore(score);
        if (challenge.getState().equals("اكتمل")) {
            Log.v("loggingC1", "value is " + !existsInCompletedChallengesList(challengeId));
            if (!existsInCompletedChallengesList(dataSnapshot.getKey())) {
                Log.v("challengesDebug", "completedListItemAdded");
                view.showCompletedChallengesTv();
                completedChallengesList.add(0, challenge);
                view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "getChallengeData1");
            }
        } else if (challenge.getState().equals(refusedChallengeText)) {
            Log.v("loggingC2", "value is " + !existsInCompletedChallengesList(challengeId));
            if (!existsInCompletedChallengesList(dataSnapshot.getKey())) {
                Log.v("challengesDebug", "completedListItemAdded");
                view.showCompletedChallengesTv();
                completedChallengesList.add(0, challenge);
                view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "getChallengeData3");
            }
        } else if (challenge.getState().equals(uncompletedChallengeText)) {
            Log.v("loggingC3", "value is " + !existsInUncompletedChallengesList(dataSnapshot.getKey()));
            if (!existsInUncompletedChallengesList(challengeId)) {
                Log.v("challengesDebug", "uncompletedListItemAdded");
                view.showUncompletedChallengesTv();
                uncompletedChallengesList.add(0, challenge);
                view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "getChallengeData2");
            }
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

    private void recreatingLists() {
        completedChallengesList = new ArrayList<>();
        view.startCompletedChallengesAdapter(completedChallengesList);

        uncompletedChallengesList = new ArrayList<>();
        view.startUnCompletedChallengesAdapter(uncompletedChallengesList);
    }

    public void removeListeners() {
        if (player1Listener != null)
            challengesReference.removeEventListener(player1Listener);
        if (player2Listener != null)
            challengesReference.removeEventListener(player2Listener);
    }

    public void onInitialDataLoaded() {
        view.hideProgressBar();
        checkListsSizeAndAdjustViews();
    }

    boolean existsInCompletedChallengesList(String currentChallengeId) {
        for (Challenge c : completedChallengesList) {
            if (c.getId().equals(currentChallengeId)) {
                return true;
            }
        }
        return false;
    }

    boolean existsInUncompletedChallengesList(String currentChallengeId) {
        for (Challenge c : uncompletedChallengesList) {
            if (c.getId().equals(currentChallengeId)) {
                Log.v("uncompletedChallengesLi", "id of challenge in uncompletedChallengesList : " + c.getId() + " returned true" + currentChallengeId);
                return true;
            }
        }
        Log.v("uncompletedChallengesLi", "returned false");
        return false;
    }

    public interface View {
        void navigate();

        void retryConnection();

        void startUnCompletedChallengesAdapter(ArrayList uncompletedChallengesList);

        void startCompletedChallengesAdapter(ArrayList completedChallengesList);

        void notifyAdapters(int completedListSize, int uncompletedListSize, String src);

        void onDataFound();

        void hideProgressBar();

        void hideUncompletedChallengesTv();

        void hideCompletedChallengesTv();

        void showCompletedChallengesTv();

        void showUncompletedChallengesTv();

        void hideNoChallengesTv();

        void showNoChallengesTv();

        void onNoInternetConnection();
    }
}
