package com.mk.playAndLearn.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.MainActivity;

import static com.mk.playAndLearn.NotificationID.getID;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.drawChallengeText;
import static com.mk.playAndLearn.utils.Strings.loseChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;
import static com.mk.playAndLearn.utils.Strings.wonChallengeText;

public class NotificationsService extends Service {
    int currentPlayer;
    ChildEventListener player1Listener, player2Listener, commentsListener;
    String onChildAddedPreviusKey = "", onChildChangedpreviusKey = "";
    //the full number which is coming from the challenges fragment
    String localCurrentUserUid;

    DatabaseReference localChallengesReference;
    DatabaseReference localCommentsReference;
    FirebaseAuth localAuth;
    FirebaseDatabase localDatabase;
    //TODO : solve the problem of that when new action occurs when the service isn't working no action happens when the app works again
    //TODO : change the text of notification according to the written and unwritten states in the database like refused, win, lose, draw ...
    //TODO : know why the app shows notifications for the uncompleted challenges when it is started and solve this problem
    public NotificationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("notificationsDebug", "onCreate" + localCurrentUserUid);
        localAuth = FirebaseAuth.getInstance();
        localDatabase = FirebaseDatabase.getInstance();
        localChallengesReference = localDatabase.getReference("challenges");
        localCommentsReference = localDatabase.getReference("comments");
        localCurrentUserUid = localAuth.getCurrentUser().getUid();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForegroundForOreoAndPie();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("notificationsDebug", "onStartCommand" + localCurrentUserUid);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForegroundForOreoAndPie();
        }

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

        ChildEventListener generalChallengesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                String player1Name = dataSnapshot.child("player1Name").getValue().toString();
                String subject = dataSnapshot.child("subject").getValue().toString();
                String challengeId = dataSnapshot.getKey();

                subject =  adjustSubject(subject);

                currentPlayer = getCurrentPlayer(player1Uid);
                Log.v("Logging2", "onChildAdded");
                if (challengeState.equals(uncompletedChallengeText) && currentPlayer == 2 && !dataSnapshot.getKey().equals(onChildAddedPreviusKey) && !dataSnapshot.getKey().equals("questionsList")) {
                    showNotification("لديك تحدى جديد", "تم تحديك فى " + subject + " بواسطة " + player1Name);
                    localChallengesReference.child(challengeId).child("player2notified").setValue(true);
                }
                onChildAddedPreviusKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String challengeState = dataSnapshot.child("state").getValue().toString();
                String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
                String player2Name = dataSnapshot.child("player2Name").getValue().toString();
                String subject = dataSnapshot.child("subject").getValue().toString();

                subject =  adjustSubject(subject);
                String state = currentUserState(dataSnapshot);

                String challengeId = dataSnapshot.getKey();
                currentPlayer = getCurrentPlayer(player1Uid);
                if (currentPlayer == 1 && (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText)) && !dataSnapshot.getKey().equals(onChildChangedpreviusKey) && !dataSnapshot.getKey().equals("questionsList")) {
                    showNotification("لديك تحدى مكتمل جديد",  "لقد " + state +
                            " تحديك ضد " + player2Name
                            /*+ " فى مادة " + subject*/);//TODO : add this
                    localChallengesReference.child(challengeId).child("player1notified").setValue(true);
                    onChildChangedpreviusKey = dataSnapshot.getKey();
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
        Log.v("notificationsDebug", "localCurrentUserUid is : " + localCurrentUserUid);
        player1Listener =  localChallengesReference.orderByChild("player1notified").equalTo(localCurrentUserUid + "false").addChildEventListener(generalChallengesListener);
        //this code gives data where current user is player 2
        player2Listener = localChallengesReference.orderByChild("player2notified").equalTo(localCurrentUserUid + "false").addChildEventListener(generalChallengesListener);


        //commentsListener
        commentsListener = localCommentsReference.orderByChild("notified").equalTo(localCurrentUserUid + "false").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String writerName = dataSnapshot.child("userName").getValue().toString();
                String postWriterUid = dataSnapshot.child("postWriterUid").getValue().toString();
                String commentWriterUid = dataSnapshot.child("writerUid").getValue().toString();
                if(!postWriterUid.equals(commentWriterUid)) {//TODO : think about changing the notification text to a shorter one
                    showNotification("لديك تعليق جديد", "تم إضافة تعليق جديد لمنشورك بواسطة " + writerName);
                }
                localCommentsReference.child(dataSnapshot.getKey()).child("notified").setValue(true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }


    private String currentUserState(DataSnapshot dataSnapshot) {
        String challengeState = dataSnapshot.child("state").getValue().toString();
        String player1Uid = dataSnapshot.child("player1Uid").getValue().toString();
        long player1Score = (long) dataSnapshot.child("player1score").getValue();
        long player2Score = (long) dataSnapshot.child("player2score").getValue();

        if (challengeState.equals(completedChallengeText)){
            if(player1Score == player2Score){
                 return  drawChallengeText + " فى";
            }
            else {
               currentPlayer = getCurrentPlayer(player1Uid);
                if(currentPlayer == 1){
                    if(player1Score > player2Score){
                        return wonChallengeText;
                    }
                    else {
                        return loseChallengeText;
                    }
                }
                else if(currentPlayer == 2){
                    if(player2Score > player1Score){
                        return wonChallengeText;
                    }
                    else {
                        return loseChallengeText;
                    }
                }
            }
        }
        return "تم رفض";
    }

    private String adjustSubject(String subject) {

        switch (subject){
            case "لغة عربية":
                subject = "اللغة العربية";
                break;
            case "لغة انجليزية":
                subject = "اللغة الإنجليزية";
                break;
            case "جغرافيا":
                subject = "الجغرافيا";
                break;
            case "تاريخ":
                subject = "التاريخ";
                break;
            case "أحياء":
                subject = "الأحياء";
                break;
            case "فيزياء":
                subject = "الفيزياء";
                break;
            case "كيمياء":
                subject = "الكيمياء";
                break;
            case "رياضيات":
                subject = "الرياضيات";
                break;
            case "فلسفة":
                subject = "الفلسفة";
                break;
            default:  subject =  "ال " + subject;//TODO : check this and make this code a method returns the new subject
        }
        return subject;
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
                    "CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.app_icon) //TODO : add setLargeIcon
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

    private void startMyOwnForegroundForOreoAndPie(){
        NotificationChannel chan = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel("default",
                    "العب .. تعلم",
                    NotificationManager.IMPORTANCE_DEFAULT);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default");
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("التطبيق يعمل فى الخلفية")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(getID(), notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
       /*if(player1Listener != null){
            localChallengesReference.removeEventListener(player1Listener);
        }
        if(player2Listener != null){
            localChallengesReference.removeEventListener(player2Listener);
        }
        if(commentsListener != null){
            localCommentsReference.removeEventListener(commentsListener);
        }*/
    }

    public int getCurrentPlayer(String player1Uid) {
        if (player1Uid.equals(localCurrentUserUid)) {
            return 1;
        } else {
            return 2;
        }
    }
}
