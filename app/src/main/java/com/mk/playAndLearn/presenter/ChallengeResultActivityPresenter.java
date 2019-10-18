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
import static com.mk.playAndLearn.utils.Integers.dailyChallengesNumber;
import static com.mk.playAndLearn.utils.Integers.drawChallengePoints;
import static com.mk.playAndLearn.utils.Integers.generalChallengeScoreMultiply;
import static com.mk.playAndLearn.utils.Integers.wonChallengePoints;
import static com.mk.playAndLearn.utils.Strings.drawChallengeText;
import static com.mk.playAndLearn.utils.Strings.loseChallengeText;
import static com.mk.playAndLearn.utils.Strings.wonChallengeText;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedPoints;
import static com.mk.playAndLearn.utils.sharedPreference.setSavedPoints;
import static com.mk.playAndLearn.utils.sharedPreference.setSavedTodayChallengesNo;

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
                            if (task.isSuccessful()) {
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

        final String challengeId = fireStoreChallenges.document().getId();

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
        map.put("challengeId", challengeId);
        map.put("player1Image", localCurrentUserImage);
        map.put("player2Name", secondPlayerName);
        map.put("player2Image", secondPlayerImage);
        map.put("questionsId", questionIds);
        map.put("score1Added", false);
        map.put("subject", subject);
        map.put("state", "لم يكتمل"); // TODO : edit this
        map.put("term", 2);//TODO : make this dynamic



        fireStoreChallenges.document(challengeId).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                fireStoreChallenges.document(challengeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            addPointsAndUpdateChallengersData(documentSnapshot, score);
                        } else {
                            //TODO
                        }
                    }
                });
            }
        });


    }

    public void uploadPlayer2DataAndUpdateUsersData(final String challengeId, final int score, final String playerAnswersList, final int currentPlayer) {
        Date today = new Date();
        final DateClass dateClass = new DateClass();
        dateClass.setDate(today);

        if (currentPlayer == 2) {
            fireStoreChallenges.document(challengeId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        fireStoreChallenges.document(challengeId).update("player2score", score);
                        //fireStoreChallenges.document(challengeId).update("player2AnswersBooleans", playerAnswersBooleansList.trim());
                        fireStoreChallenges.document(challengeId).update("player2Answers", playerAnswersList);
                        fireStoreChallenges.document(challengeId).update("state", "اكتمل");

                        // usersReference.child(documentSnapshot.getString("player2Uid")).child("lastChallengeDate").setValue(dateClass.getDate());

                        addPointsAndUpdateChallengersData(documentSnapshot, score);
                    }

                }
            });
        }
    }

    void addPointsAndUpdateChallengersData(DocumentSnapshot dataSnapshot, int score) {
        final String player1Uid = dataSnapshot.getString("player1Uid");
        final String player2Uid = dataSnapshot.getString("player2Uid");

        final long player1Score = dataSnapshot.getLong("player1score");
        final long player2Score = (long) score;


        final DocumentReference player1Reference = fireStoreUsers.document(player1Uid);
        final DocumentReference player2Reference = fireStoreUsers.document(player2Uid);

        fireStore.runTransaction(new Transaction.Function<Long>() {
            @Nullable
            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                Log.v("limitingChallenges", "begging of transaction");

                long noOfWins = 0, noOfLoses = 0, noOfDraws = 0, newNoOfWins = 0, newNoOfLoses = 0, newNoOfDraws = 0;
                long newPoints = 0, totalChallengesNo = 0, todayChallengesNo = 0;

                if (getCurrentPlayer(player1Uid) == 2) {
                    Log.v("limitingChallenges", "current player is 2");
                    DocumentSnapshot snapshotForPlayer2 = transaction.get(player2Reference);

                    if (snapshotForPlayer2.getLong("totalChallengesNo") != null) {
                        totalChallengesNo = snapshotForPlayer2.getLong("totalChallengesNo");
                    }

                    if (snapshotForPlayer2.getLong("todayChallengesNo") != null) {
                        todayChallengesNo = snapshotForPlayer2.getLong("todayChallengesNo");
                    }

                    if (player1Score == player2Score) {
                        if (snapshotForPlayer2.getLong("noOfDraws") != null)
                            noOfDraws = snapshotForPlayer2.getLong("noOfDraws");

                        newPoints = snapshotForPlayer2.getLong("points") + (long) drawChallengePoints;
                        newNoOfDraws = noOfDraws + (long) 1;
                        transaction.update(player2Reference, "points", newPoints);
                        transaction.update(player2Reference, "totalChallengesNo", totalChallengesNo + 1);
                        transaction.update(player2Reference, "todayChallengesNo", todayChallengesNo + 1);

                        setSavedPoints(context, newPoints);

                        Log.v("sfPoints", "new SavedPoints is : " + getSavedPoints(context));

                        if (totalChallengesNo != 0) {
                            transaction.update(player2Reference, "noOfDraws", newNoOfDraws);
                        } else {
                            transaction.update(player2Reference, "noOfDraws", 1);
                            transaction.update(player2Reference, "noOfWins", 0);
                            transaction.update(player2Reference, "noOfLoses", 0);
                        }
                    } else if (player2Score > player1Score) {
                        if (snapshotForPlayer2.getLong("noOfWins") != null)
                            noOfWins = snapshotForPlayer2.getLong("noOfWins");

                        newPoints = snapshotForPlayer2.getLong("points") + (long) wonChallengePoints;
                        newNoOfWins = noOfWins + (long) 1;
                        transaction.update(player2Reference, "points", newPoints);
                        transaction.update(player2Reference, "totalChallengesNo", totalChallengesNo + 1);
                        transaction.update(player2Reference, "todayChallengesNo", todayChallengesNo + 1);

                        setSavedPoints(context, newPoints);

                        Log.v("sfPoints", "new SavedPoints is : " + getSavedPoints(context));

                        if (totalChallengesNo != 0) {
                            transaction.update(player2Reference, "noOfWins", newNoOfWins);
                        } else {
                            transaction.update(player2Reference, "noOfDraws", 0);
                            transaction.update(player2Reference, "noOfWins", 1);
                            transaction.update(player2Reference, "noOfLoses", 0);
                        }
                    } else {
                        if (snapshotForPlayer2.getLong("noOfLoses") != null)
                            noOfLoses = snapshotForPlayer2.getLong("noOfLoses");

                        newNoOfLoses = noOfLoses + (long) 1;
                        transaction.update(player2Reference, "totalChallengesNo", totalChallengesNo + 1);
                        transaction.update(player2Reference, "todayChallengesNo", todayChallengesNo + 1);

                        if (totalChallengesNo != 0) {
                            transaction.update(player2Reference, "noOfLoses", newNoOfLoses);
                        } else {
                            transaction.update(player2Reference, "noOfDraws", 0);
                            transaction.update(player2Reference, "noOfWins", 0);
                            transaction.update(player2Reference, "noOfLoses", 1);
                        }
                    }

                    return todayChallengesNo + 1;
                } else if (getCurrentPlayer(player1Uid) == 1) {
                    DocumentSnapshot snapshotForPlayer1 = transaction.get(player1Reference);

                    if (snapshotForPlayer1.getLong("todayChallengesNo") != null) {
                        todayChallengesNo = snapshotForPlayer1.getLong("todayChallengesNo");
                    }
                    transaction.update(player1Reference, "totalChallengesNo", totalChallengesNo + 1);
                    transaction.update(player1Reference, "todayChallengesNo", todayChallengesNo + 1);

                    Log.v("limitingChallenges", "current player is 1"
                            + " , new todayChallengesNo is " + todayChallengesNo + 1);
                    return todayChallengesNo + 1;
                }
                Log.v("limitingChallenges", "value is : " + todayChallengesNo + 1);

                return todayChallengesNo + 1;

            }
        }).addOnSuccessListener(new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                Log.v("limitingChallenges", "aLong is : " + aLong);

                long remainedDailyChallenges = dailyChallengesNumber - aLong;
                if (remainedDailyChallenges > 3) {
                    Toast.makeText(context, "يمكنك لعب " + (dailyChallengesNumber - aLong) + " تحديات فقط اليوم بعد هذا التحدى", Toast.LENGTH_LONG).show();
                } else if (remainedDailyChallenges == 2) {
                    Toast.makeText(context, "يمكنك لعب " + " تحديان فقط اليوم بعد هذا التحدى", Toast.LENGTH_LONG).show();
                } else if (remainedDailyChallenges == 1) {
                    Toast.makeText(context, "يمكنك لعب " + " تحدي واحد فقط اليوم بعد هذا التحدى", Toast.LENGTH_LONG).show();
                } else if (remainedDailyChallenges < 1) {
                    Toast.makeText(context, "لا يمكنك بدء تحديات جديدة هذا اليوم يمكنك العودة غدا للعب تحديات جديدة أو طلب من أصدقائك بدء تحديات ضدك", Toast.LENGTH_LONG).show();
                }
                setSavedTodayChallengesNo(context, aLong);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Transaction failure.", e);
                Log.v("limitingChallenges", "onFailure , exception is : " + e.getMessage());
            }
        });
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

    public void uploadGeneralChallengeData(final int score, final int size) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final int finalChallengePoints = score * generalChallengeScoreMultiply;

        Log.v("generalChallengePoints", "list size : " + size + " , finalChallengePoints : " + finalChallengePoints);

        view.setChallengeResultTvText("نتيجة التحدى : " + 100 + " / " + finalChallengePoints); //TODO : edit this to (questionsList.size() * generalChallengeScoreMultiply)

        fireStoreUsers.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    long lastGeneralChallengePoints = 0, userPoints;
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.getLong("lastGeneralChallengeScore") != null)
                        lastGeneralChallengePoints = documentSnapshot.getLong("lastGeneralChallengeScore");
                    userPoints = documentSnapshot.getLong("points");
                    if (lastGeneralChallengePoints == 0) {
                        long newPoints = userPoints + finalChallengePoints;

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("lastGeneralChallengeScore", finalChallengePoints);
                        updates.put("points", newPoints);

                        setSavedPoints(context, newPoints);

                        fireStoreUsers.document(currentUser.getUid()).update(updates);
                    } else {
                        Toast.makeText(context, "لقد قمت بالمشاركة فى هذا التحدى من قبل ولن يتم احتساب نقاطك الحالية", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public interface View {
        void setOpponentData(long opponentScore, String opponentName, String opponentImage);

        void setChallengeTvText(String text);

        void setChallengeTvBGColor(int color);

        void setChallengeResultTvText(String text);

    }
}
