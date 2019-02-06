package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.AdminQuestionActivity;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.AdManager;
import com.mk.playAndLearn.utils.DateClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStore;
import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.Integers.drawChallengePoints;
import static com.mk.playAndLearn.utils.Integers.generalChallengeScoreMultiply;
import static com.mk.playAndLearn.utils.Integers.wonChallengePoints;
import static com.mk.playAndLearn.utils.Strings.drawChallengeText;
import static com.mk.playAndLearn.utils.Strings.loseChallengeText;
import static com.mk.playAndLearn.utils.Strings.wonChallengeText;

public class ChallengeResultActivityPresenter {
    View view;
    Context context;

    public ChallengeResultActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void showAd() {
        AdManager adManager = AdManager.getInstance();
        InterstitialAd ad = adManager.getAd();
        Log.v("contestLogging", "ad is : " + ad + " , ad loaded is : " + ad.isLoaded());
        if (ad != null && ad.isLoaded()) {
            ad.show();
        }
    }

    public void downloadOpponentDataAndDetermineChallengeResult(String challengeId, final int score) {
        fireStoreChallenges.document(challengeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot documentSnapshot = task.getResult();
                    final long opponentScore = documentSnapshot.getLong("player1score");

                    String player1uid = documentSnapshot.getString("player1Uid");
                    fireStoreUsers.document(player1uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                String opponentName = document.getString("userName");
                                String opponentImage = document.getString("userImage");

                                view.setOpponentData(opponentScore, opponentName, opponentImage);
                            }
                        }
                    });

                    int opponentScoreInt = (int) opponentScore;

                    if (score == opponentScoreInt) {
                        view.setChallengeTvText(drawChallengeText);
                    } else {
                        if (score > opponentScoreInt) {
                            view.setChallengeTvText(wonChallengeText);
                            view.setChallengeTvBGColor(context.getResources().getColor(R.color.green));
                        } else {
                            view.setChallengeTvText(loseChallengeText);
                            view.setChallengeTvBGColor(context.getResources().getColor(R.color.red));
                        }
                    }
                }
            }
        });
    }

    public void uploadPlayer1Data(final String correctAnswersList, final String playerAnswersList
            , final String secondPlayerUid, final int score, final String currentUserName, final String localCurrentUserImage, final String secondPlayerName
            , final String secondPlayerImage, final ArrayList questionsList, final String subject) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        final String grade = pref.getString("grade", "غير معروف");
        final Map<String, Object> map = new HashMap<>();

        final String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final String questionIds = getQuestionsId(questionsList);

        Date today = new Date();
        final DateClass dateClass = new DateClass();
        dateClass.setDate(today);

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        final String todayDate = format.format(today);

        map.put("correctAnswers", correctAnswersList);
        map.put("date", dateClass.getDate());
        map.put("dayDate", todayDate);
        map.put("grade", grade);
        map.put("player1Answers", playerAnswersList);
        map.put("player1notified", localCurrentUserUid + "false");
        map.put("player2notified", secondPlayerUid + "false");
        map.put("player1score", score);
        map.put("player2score", 0);
        map.put("player1Uid", localCurrentUserUid);
        map.put("player2Uid", secondPlayerUid);
        map.put("player1Name", currentUserName);
        map.put("player1Image", localCurrentUserImage);
        map.put("player2Name", secondPlayerName);
        map.put("player2Image", secondPlayerImage);
        map.put("questionsId", questionIds);
        map.put("score1Added", false);
        map.put("subject", subject);
        map.put("state", "لم يكتمل"); // TODO : edit this
        map.put("term", 2);//TODO : make this dynamic


        fireStoreChallenges.document().set(map);


    }

    public void uploadPlayer2Data(final String challengeId, final int score, final String playerAnswersList) {
        Date today = new Date();
        final DateClass dateClass = new DateClass();
        dateClass.setDate(today);

        fireStoreChallenges.document(challengeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    fireStoreChallenges.document(challengeId).update("player2score", score);
                    //fireStoreChallenges.document(challengeId).update("player2AnswersBooleans", playerAnswersBooleansList.trim());
                    fireStoreChallenges.document(challengeId).update("player2Answers", playerAnswersList);
                    fireStoreChallenges.document(challengeId).update("state", "اكتمل");

                   // usersReference.child(documentSnapshot.getString("player2Uid")).child("lastChallengeDate").setValue(dateClass.getDate());

                    addPoints(documentSnapshot, score);
                }
            }
        });
    }

    void addPoints(DocumentSnapshot dataSnapshot, int score) {
        final String player1Uid = dataSnapshot.getString("player1Uid");
        final String player2Uid = dataSnapshot.getString("player2Uid");

        long player1Score = (long) dataSnapshot.getLong("player1score");
        long player2Score = (long) score;


        if (getCurrentPlayer(player1Uid) == 2) {

            if (player1Score == player2Score) {
                final DocumentReference player2Reference = fireStoreUsers.document(player2Uid);;

                fireStore.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(player2Reference);
                        long newPoints = snapshot.getLong("points") + (long) drawChallengePoints;
                        transaction.update(player2Reference, "points", newPoints);
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Transaction success!");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Transaction failure.", e);
                    }
                });
            } else if (player2Score > player1Score) {
                final DocumentReference player2Reference = fireStoreUsers.document(player2Uid);;

                fireStore.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(player2Reference);
                        long newPoints = snapshot.getLong("points") + (long)wonChallengePoints;
                        transaction.update(player2Reference, "points", newPoints);
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Transaction success!");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Transaction failure.", e);
                    }
                });
            }
        }
    }

    public String getQuestionsId(ArrayList questionsList) {
        String Ids = "";
        ArrayList<Question> list = questionsList;
        for (Question question : list) {
            Ids += question.getQuestionId() + " ";//TODO : check this
        }
        return Ids.trim();
    }

    public int getCurrentPlayer(String player1Uid) {
        String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (player1Uid.equals(localCurrentUserUid)) {
            return 1;
        } else {
            return 2;
        }
    }

    public void uploadGeneralChallengeData(final int score){
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();;

        /*usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int lastGeneralChallengePoints = Integer.parseInt(dataSnapshot.child("lastGeneralChallengeScore").getValue().toString());
                int userPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                int finalChallengePoints = score * generalChallengeScoreMultiply;
                view.setChallengeResultTvText("نتيجة التحدى : " + 100 + " / " + finalChallengePoints); //TODO : edit this to (questionsList.size() * generalChallengeScoreMultiply)
                if (lastGeneralChallengePoints == 0) {
                    usersReference.child(currentUser.getUid()).child("lastGeneralChallengeScore").setValue(finalChallengePoints);
                    usersReference.child(currentUser.getUid()).child("points").setValue(userPoints + finalChallengePoints);
                } else {
                    Toast.makeText(context, "لقد قمت بالمشاركة فى هذا التحدى من قبل ولن يتم احتساب نقاطك الحالية", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    public interface View {
        void setOpponentData(long opponentScore, String opponentName, String opponentImage);

        void setChallengeTvText(String text);

        void setChallengeTvBGColor(int color);

        void setChallengeResultTvText(String text);
    }
}
