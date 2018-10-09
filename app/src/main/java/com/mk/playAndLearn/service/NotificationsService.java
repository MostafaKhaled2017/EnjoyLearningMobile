package com.mk.playAndLearn.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.NotificationID.getID;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class NotificationsService extends Service {
    FirebaseDatabase database;
    FirebaseAuth auth;
    DatabaseReference challengesReference;
    String currentSubject, currentUserUid;
    int currentPlayer;
    //the full number which is coming from the challenges fragment
    int player1childrenCount, player2childrenCount;

    int currentPlayer1childrenCount = 0, currentPlayer2childrenCount = 0;

    //TODO : solve the problem of that when new action occurs when the service isn't working no action happens when the app works again
    //TODO : change the text of notification according to the written and unwritten states in the database like refused, win, lose, draw ...
    //TODO : know why the app shows notifications for the uncompleted challenges when it is started and solve this problem
    public NotificationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        challengesReference = database.getReference("challenges");
        auth = FirebaseAuth.getInstance();
        currentUserUid = auth.getCurrentUser().getUid();
        player1childrenCount = -1;
        player2childrenCount = -1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            player1childrenCount = intent.getIntExtra("player1childrenCount", -1);
            player2childrenCount = intent.getIntExtra("player2childrenCount", -1);
        }
        challengesReference.orderByChild("player1Uid").equalTo(currentUserUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                currentPlayer1childrenCount++;
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                getCurrentPlayer(player1Uid);
                if (player1childrenCount != -1 && currentPlayer1childrenCount > player1childrenCount && challengeState.equals(uncompletedChallengeText) && currentPlayer == 2) {
                    showNotification("لديك تحدى", "لديك تحدي جديد");
                    currentPlayer1childrenCount = player1childrenCount;
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                getCurrentPlayer(player1Uid);
                if (player1childrenCount != -1 && currentPlayer == 1 && (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText))) {
                    showNotification("اكتمل التحدى", "لديك تحدي مكتمل جديد");
                    currentPlayer1childrenCount = player1childrenCount;
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getActivity(), "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "error loading data : " + databaseError);
            }
        });

        //this code gives data where current user is player 2
        challengesReference.orderByChild("player2Uid").equalTo(currentUserUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                currentPlayer2childrenCount++;
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                getCurrentPlayer(player1Uid);
                if (player2childrenCount != -1 && currentPlayer2childrenCount > player2childrenCount && challengeState.equals(uncompletedChallengeText) && currentPlayer == 2) {
                    showNotification("لديك تحدى", "لديك تحدي جديد");
                    currentPlayer2childrenCount = player2childrenCount;

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                getCurrentPlayer(player1Uid);
                if (player2childrenCount != -1 &&currentPlayer == 1 && (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText))) {
                    showNotification("اكتمل التحدى", "لديك تحدي مكتمل جديد");
                    currentPlayer2childrenCount = player2childrenCount;
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getActivity(), "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت", Toast.LENGTH_SHORT).show();
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
        mNotificationManager.notify(getID(), mBuilder.build());
        //TODO : edit the id if needed
        //TODO : think about making the notification opens the challenges fragment directly
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO : remove the listeners
    }

    public void getCurrentPlayer(String player1Uid) {
        if (player1Uid.equals(currentUserUid)) {
            currentPlayer = 1;
        } else {
            currentPlayer = 2;
        }
    }

}