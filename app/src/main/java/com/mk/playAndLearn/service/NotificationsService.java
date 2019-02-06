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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.activity.MainActivity;

import org.w3c.dom.Document;

import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;
import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.NotificationID.getID;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.drawChallengeText;
import static com.mk.playAndLearn.utils.Strings.loseChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;
import static com.mk.playAndLearn.utils.Strings.wonChallengeText;

public class NotificationsService extends Service {
    int currentPlayer;
    String onChildAddedPreviusKey = "", onChildChangedpreviusKey = "";
    //the full number which is coming from the challenges fragment
    String localCurrentUserUid;

    FirebaseAuth localAuth;
    //TODO : solve the problem of that when new action occurs when the service isn't working no action happens when the app works again
    //TODO : change the text of notification according to the written and unwritten states in the database like refused, win, lose, draw ...
    //TODO : know why the app shows notifications for the uncompleted challenges when it is started and solve this problem
    public NotificationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("notificationsDebug", "onCreate" + localCurrentUserUid);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForegroundForOreoAndPie();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        localAuth = FirebaseAuth.getInstance();
        localCurrentUserUid = localAuth.getCurrentUser().getUid();
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


        EventListener generalSnapShotListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG, listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    final DocumentSnapshot challengeDocument = dc.getDocument();
                    switch (dc.getType()) {
                        case ADDED:{
                            final String challengeState = challengeDocument.getString("state");
                            final String player1Uid = challengeDocument.getString("player1Uid");
                            final String subject = adjustSubject(challengeDocument.getString("subject"));
                            final String challengeId = challengeDocument.getId();

                            fireStoreUsers.document(player1Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        currentPlayer = getCurrentPlayer(player1Uid);
                                        final String player1Name = (String) document.getString("userName");
                                        if (challengeState.equals(uncompletedChallengeText) && currentPlayer == 2 && !challengeDocument.getId().equals(onChildAddedPreviusKey) && !challengeDocument.getId().equals("questionsList") && challengeDocument.exists()) {
                                            fireStoreChallenges.document(challengeId).update("player2notified", true);
                                            showNotification("لديك تحدى جديد", "تم تحديك فى " + subject + " بواسطة " + player1Name);
                                        }
                                    }
                                }
                            });

                            Log.v("Logging2", "onChildAdded");

                            break;
                        }
                        case MODIFIED:
                            final String challengeState = challengeDocument.getString("state");
                            final String player2Uid = challengeDocument.getString("player2Uid");
                            final String player1Uid = challengeDocument.getString("player1Uid");
                            String subject = adjustSubject(challengeDocument.getString("subject"));

                            final String state = currentUserState(challengeDocument);

                            final String challengeId = challengeDocument.getId();

                            fireStoreUsers.document(player2Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        currentPlayer = getCurrentPlayer(player1Uid);
                                        final String player2Name = (String) document.getString("userName");

                                        if (currentPlayer == 1 && (challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText)) && !challengeDocument.getId().equals(onChildChangedpreviusKey) && !challengeDocument.getId().equals("questionsList")) {
                                            showNotification("لديك تحدى مكتمل جديد", "لقد " + state +
                                                            " تحديك ضد " + player2Name
                                                    /*+ " فى مادة " + subject*/);//TODO : add this
                                            fireStoreChallenges.document(challengeId).update("player1notified", true);
                                            onChildChangedpreviusKey = challengeDocument.getId();
                                        }
                                    }
                                }
                            });
                        case REMOVED:
                    }
                }
            }
        };

        //this gives the challenges that the current user has started
        Log.v("notificationsDebug", "localCurrentUserUid is : " + localCurrentUserUid);
         fireStoreChallenges.whereEqualTo("player1notified",localCurrentUserUid + "false").addSnapshotListener(generalSnapShotListener);
        //this code gives data where current user is player 2
        fireStoreChallenges.whereEqualTo("player2notified", localCurrentUserUid + "false").addSnapshotListener(generalSnapShotListener);


        //commentsListener
         fireStoreComments.whereEqualTo("notified",localCurrentUserUid + "false").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
             @Override
             public void onSuccess(QuerySnapshot documentSnapshots) {
                 for(DocumentSnapshot document : documentSnapshots.getDocuments()){
                     String writerName = document.getString("userName");
                     String postWriterUid = document.getString("postWriterUid");
                     String commentWriterUid = document.getString("writerUid");
                     if(!postWriterUid.equals(commentWriterUid) && document.exists()) {//TODO : think about changing the notification text to a shorter one
                         showNotification("لديك تعليق جديد", "تم إضافة تعليق جديد لمنشورك بواسطة " + writerName);
                     }
                     fireStoreComments.document(document.getId()).update("notified", true);
                 }
             }
         });

        return START_STICKY;
    }


    private String currentUserState(DocumentSnapshot dataSnapshot) {
        String challengeState = dataSnapshot.getString("state");
        String player1Uid = dataSnapshot.getString("player1Uid");
        long player1Score = (long) dataSnapshot.getLong("player1score");
        long player2Score = (long) dataSnapshot.getLong("player2score");

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

    public static String adjustSubject(String subject) {

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
