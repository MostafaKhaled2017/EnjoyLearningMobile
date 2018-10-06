package com.mk.playAndLearn.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.MainActivity;
import com.mk.playAndLearn.model.Challenge;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class NotificationsService extends Service {
    ArrayList<Challenge> completedChallengesList = new ArrayList<>(), uncompletedChallengesList = new ArrayList<>();
    String currentSubject, currentUserUid;
    int currentPlayer, previousCompetedChallengeListSize = -1, previousUnCompetedChallengeListSize = -1;
    public NotificationsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference challengesReference = database.getReference("challenges");
        challengesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        GenericTypeIndicator<List<Question>> t = new GenericTypeIndicator<List<Question>>() {
                        };
                        Challenge challenge = new Challenge();
                        String challengeDate = dataSnapshot1.child("date").getValue().toString();//TODO : solve the date problem
                        String challengeSubject = dataSnapshot1.child("subject").getValue().toString();
                        String challengeState = dataSnapshot1.child("state").getValue().toString();
                        String challengeId = dataSnapshot1.getKey();
                        ArrayList challengeQuestionsList = (ArrayList) dataSnapshot1.child("questionsList").getValue(t);
                        long player1Score = (long) dataSnapshot1.child("player1score").getValue();
                        long player2Score = (long) dataSnapshot1.child("player2score").getValue();

                        String player1Name = dataSnapshot1.child("player1Name").getValue().toString();
                        String player1Image = dataSnapshot1.child("player1Image").getValue().toString();
                        String player1Uid = dataSnapshot1.child("player1Uid").getValue().toString();
                        String player2Name = dataSnapshot1.child("player2Name").getValue().toString();
                        String player2Image = dataSnapshot1.child("player2Image").getValue().toString();
                        String player2Uid = dataSnapshot1.child("player2Uid").getValue().toString();

                        String challengerName, challengerImage;

                        if (player1Uid.equals(currentUserUid)) {
                            currentPlayer = 1;
                            challengerName = player2Name;
                            challengerImage = player2Image;
                            challenge.setSecondChallengerUid(player2Uid);//second means that it is not the player who starts the challenge

                        } else {
                            currentPlayer = 2;
                            challengerName = player1Name;
                            challengerImage = player1Image;
                            challenge.setSecondChallengerUid(player1Uid);//second means that it is not the player who starts the challenge
                        }
                        challenge.setCurrentPlayer(currentPlayer);
                        challenge.setChallengerName(challengerName);
                        challenge.setDate(challengeDate);
                        challenge.setImage(challengerImage);
                        challenge.setSubject(challengeSubject);
                        challenge.setState(challengeState);
                        challenge.setId(challengeId);
                        challenge.setQuestionsList(challengeQuestionsList);
                        //TODO : find a better way to do that as this will load all the data and this will take time
                        if (player1Uid.equals(currentUserUid) || player2Uid.equals(currentUserUid)) {
                            String score;
                            if (currentPlayer == 1) {
                                score = player2Score + " : " + player1Score;
                            } else {
                                score = player1Score + " : " + player2Score;
                            }
                            challenge.setScore(score);
                            if (challenge.getState().equals("اكتمل")) {
                                completedChallengesList.add(0, challenge);
                            } else if (challenge.getState().equals(refusedChallengeText)) {
                                completedChallengesList.add(0, challenge);
                            } else if (challenge.getState().equals(uncompletedChallengeText)) {
                                uncompletedChallengesList.add(0, challenge);
                            }
                        }
                    }
                }

                if (previousCompetedChallengeListSize != -1 && previousCompetedChallengeListSize < completedChallengesList.size() && currentPlayer == 1) {
                    //TODO : think about changing the text
                    showNotification("اكتمل التحدى", "لديك تحدي مكتمل جديد");
                }

                if (previousUnCompetedChallengeListSize != -1 && previousUnCompetedChallengeListSize < uncompletedChallengesList.size() && currentPlayer == 2) {
                    //TODO : think about changing the text
                    showNotification("لديك تحدى", "لديك تحدي جديد");
                }

                previousCompetedChallengeListSize = completedChallengesList.size();
                previousUnCompetedChallengeListSize = uncompletedChallengesList.size();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Logging", "error loading data : " + databaseError);
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public void showNotification(String title, String content) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.app_icon) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content)// message for notification
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // set alarm sound for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
        //TODO : edit the id if needed
        //TODO : think about making the notification opens the challenges fragment directly
    }
}
