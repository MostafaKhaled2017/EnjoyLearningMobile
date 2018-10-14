package com.mk.playAndLearn.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.NotificationID.getID;
import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class NotificationsService extends Service {
    int currentPlayer;
    //the full number which is coming from the challenges fragment

    //TODO : solve the problem of that when new action occurs when the service isn't working no action happens when the app works again
    //TODO : change the text of notification according to the written and unwritten states in the database like refused, win, lose, draw ...
    //TODO : know why the app shows notifications for the uncompleted challenges when it is started and solve this problem
    public NotificationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*//start of media player(used for debug)

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

        //end of media player*/

        ChildEventListener generalListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                String challengeId = dataSnapshot.getKey();
                getCurrentPlayer(player1Uid);
                Log.v("Logging2", "onChildAdded");
                if (challengeState.equals(uncompletedChallengeText) && currentPlayer == 2) {
                    showNotification("لديك التحدى", "لديك تحدي جديد");
                    challengesReference.child(challengeId).child("player2notified").setValue(true);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                String challengeId = dataSnapshot.getKey();
                getCurrentPlayer(player1Uid);
                if (currentPlayer == 1 && (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText))) {
                    showNotification("اكتمل التحدى", "لديك تحدي مكتمل جديد");
                    challengesReference.child(challengeId).child("player1notified").setValue(true);
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
        };

        //this gives the challenges that the current user has started
        challengesReference.orderByChild("player1notified").equalTo(currentUserUid + "false").addChildEventListener(generalListener);
        //this code gives data where current user is player 2
        challengesReference.orderByChild("player2notified").equalTo(currentUserUid + "false").addChildEventListener(generalListener);

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
    }

    public void getCurrentPlayer(String player1Uid) {
        if (player1Uid.equals(currentUserUid)) {
            currentPlayer = 1;
        } else {
            currentPlayer = 2;
        }
    }

}
