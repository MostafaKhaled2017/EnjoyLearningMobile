package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class ChallengesFragmentPresenter {
    Challenge challenge;
    View view;
    ArrayList<Challenge> completedChallengesList = new ArrayList<>(), uncompletedChallengesList = new ArrayList<>();
    int player1childrenCount = 0, player2childrenCount = 0, currentPlayer;
    ChildEventListener player1Listener, player2Listener;
    int no = 0;
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
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



                    EventListener generalSnapShotListener = new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("TAG, listen:error", e);
                                return;
                            }

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                DocumentSnapshot challengeDocument = dc.getDocument();
                                switch (dc.getType()) {
                                    case ADDED:
                                        getChallengeData(challengeDocument, "onChildAdded");
                                        view.onDataFound();
                                        break;
                                    case MODIFIED:
                                        //TODO : note that the only changing handled is when challenge moves from uncompleted to completed state
                                        for (int i = 0; i < uncompletedChallengesList.size(); i++) {
                                            if (uncompletedChallengesList.get(i).getId().equals(challengeDocument.getId())) {
                                                uncompletedChallengesList.remove(i);
                                                Log.v("challenge Removed", "challengeRemoved");
                                                getChallengeData(challengeDocument, "onChildChanged");
                                                view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "onChildChanged");
                                                break;
                                            }
                                        }
                                        checkListsSizeAndAdjustViews();
                                        view.hideProgressBar();
                                        break;
                                    case REMOVED:
                                        startAsynkTask();//TODO : think about removeing the child from the list only                            break;
                                }
                            }
                            onInitialDataLoaded();
                        }
                    };

                    //this code gives data where current user is player 1
                    fireStoreChallenges.whereEqualTo("player1Uid", localCurrentUserUid).orderBy("date", Query.Direction.ASCENDING).addSnapshotListener(generalSnapShotListener);
                    //this code gives data where current user is player 2
                    fireStoreChallenges.whereEqualTo("player2Uid", localCurrentUserUid).orderBy("date", Query.Direction.ASCENDING).addSnapshotListener(generalSnapShotListener);

                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public String getChallengeData(DocumentSnapshot dataSnapshot, String tag) {
        Log.v("ChallengesFragPresenter", "get challenge data called");
        Log.v("ChallengesFragPresenter", "completedChallengesList : " + completedChallengesList.size()
                + ", uncompletedChallengesList " + uncompletedChallengesList.size());
        view.startCompletedChallengesAdapter(completedChallengesList);
        view.startUnCompletedChallengesAdapter(uncompletedChallengesList);

        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        challenge = new Challenge();
        String challengeDate = format.format(dataSnapshot.get("date"));
        String challengeSubject = dataSnapshot.getString("subject");
        String challengeState = dataSnapshot.getString("state");
        String challengeId = dataSnapshot.getId();
        String challengeQuestionsList =  dataSnapshot.getString("questionsId");
        long player1Score = (long) dataSnapshot.getLong("player1score");
        long player2Score = (long) dataSnapshot.getLong("player2score");

        String player1Name = dataSnapshot.getString("player1Name");
        String player1Image = dataSnapshot.getString("player1Image");
        String player1Uid = dataSnapshot.getString("player1Uid");
        String player2Name = dataSnapshot.getString("player2Name");
        String player2Image = dataSnapshot.getString("player2Image");
        String player2Uid = dataSnapshot.getString("player2Uid");

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
            if (!existsInCompletedChallengesList(dataSnapshot.getId())) {
                Log.v("challengesDebug", "completedListItemAdded");
                view.showCompletedChallengesTv();
                completedChallengesList.add(0,challenge);
                view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "getChallengeData1");
            }
        } else if (challenge.getState().equals(refusedChallengeText)) {
            Log.v("loggingC2", "value is " + !existsInCompletedChallengesList(challengeId));
            if (!existsInCompletedChallengesList(dataSnapshot.getId())) {
                Log.v("challengesDebug", "completedListItemAdded");
                view.showCompletedChallengesTv();
                completedChallengesList.add(0,challenge);
                view.notifyAdapters(completedChallengesList.size(), uncompletedChallengesList.size(), "getChallengeData3");
            }
        } else if (challenge.getState().equals(uncompletedChallengeText)) {
            Log.v("loggingC3", "value is " + !existsInUncompletedChallengesList(dataSnapshot.getId()));
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
