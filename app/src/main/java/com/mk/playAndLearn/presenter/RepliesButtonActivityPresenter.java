package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.model.Reply;
import com.mk.playAndLearn.utils.DateClass;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreReplies;

public class RepliesButtonActivityPresenter {
    RepliesButtonActivityPresenter.View view;
    Context context;
    ArrayList<Reply> repliesList = new ArrayList();
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

    public RepliesButtonActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
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
                    getReplies();
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }


    public void getReplies() {
        clearList();
        view.startRecyclerAdapter(repliesList);
        Log.v("repliesLogging", "getReplies method , commentId is : " + view.getCommentId());
        fireStoreReplies.whereEqualTo("commentId", view.getCommentId()).orderBy("date", Query.Direction.ASCENDING).addSnapshotListener((new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG, listen:error", e);
                    return;
                }
                Log.v("repliesLogging", "fireStoreReplies");

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    DocumentSnapshot replyDocument = dc.getDocument();
                    format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    switch (dc.getType()) {
                        case ADDED: {
                            view.onDataFound();
                            Reply reply = new Reply();
                            String userName = replyDocument.getString("userName");
                            String userEmail = replyDocument.getString("userEmail");
                            String content = replyDocument.getString("content");
                            String userImage = replyDocument.getString("userImage");
                            String date = format.format(replyDocument.get("date"));
                            String writerUid = replyDocument.getString("writerUid");
                            long votes = replyDocument.getLong("votes");
                            boolean posted = replyDocument.getBoolean("posted");
                            String replyId = replyDocument.getId();

                            reply.setVotes(votes);
                            reply.setPosted(posted);
                            reply.setUserEmail(userEmail);
                            reply.setReplyId(replyId);
                            reply.setWriterUid(writerUid);
                            reply.setUserName(userName);
                            reply.setContent(content);
                            reply.setUserImage(userImage);
                            reply.setDate(date);
                            if (!existsInRepliesList(replyId)) {
                                repliesList.add(0, reply);
                                view.notifyAdapter();
                            }
                            break;
                        }
                        case MODIFIED:
                            String content = replyDocument.getString("content");
                            String date = format.format(replyDocument.get("date"));
                            long votes = replyDocument.getLong("votes");
                            boolean posted = replyDocument.getBoolean("posted");

                            for (int i = 0; i < repliesList.size(); i++) {
                                if (repliesList.get(i).getReplyId().equals(replyDocument.getId())) {
                                    repliesList.get(i).setPosted(posted);
                                    repliesList.get(i).setDate(date);
                                    repliesList.get(i).setContent(content);
                                    repliesList.get(i).setVotes(votes);
                                    view.notifyAdapter();
                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            startAsynkTask();//TODO : think about removeing the child from the list only                            break;
                    }
                }
                if (repliesList.size() == 0) {
                    Log.v("repliesLogging", "list size is 0");
                    view.onNoReplyFound();
                }
            }
        }));
    }

    void clearList() {
        if (!repliesList.isEmpty()) {
            repliesList.clear();
        }
    }

    public void addReply(String replyText) {
        Date today = new Date();
        final DateClass dateClass = new DateClass();
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        dateClass.setDate(today);

        Map<String, Object> map = new HashMap<>();
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode //TODO : check this
        String currentUserName = pref.getString("currentUserName", "غير معروف");
        String localCurrentUserImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.v("sharedPreference", " current userName is : " + currentUserName);

        map.put("content", replyText.trim());
        map.put("date", dateClass.getDate());
        map.put("downVotedUsers", "users: ");
        map.put("notified", view.getCommentWriterUid() + "false");
        map.put("commentId", view.getCommentId());
        map.put("commentWriterUid", view.getCommentWriterUid());
        map.put("writerUid", localCurrentUserUid);
        map.put("upVotedUsers", "users: ");
        map.put("userName", currentUserName);
        map.put("userEmail", localCurrentUserEmail);
        map.put("userImage", localCurrentUserImage);
        map.put("votes", 0);
        map.put("posted", false);

        final DocumentReference currenReplyRef = fireStoreReplies.document();
        currenReplyRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currenReplyRef.update("posted", true);
                currenReplyRef.update("date", dateClass.getDate());
                view.showToast("تم إضافة الرد بنجاح");
                view.notifyAdapter();
            }
        });
    }

    private boolean existsInRepliesList(String replyId) {
        for (Reply reply : repliesList) {
            if (reply.getReplyId().equals(replyId)) {
                return true;
            }
        }
        return false;
    }

    public interface View {

        void retryConnection();

        void startRecyclerAdapter(ArrayList repliesList);

        void onNoInternetConnection();

        void onDataFound();

        void hideProgressBar();

        void notifyAdapter();

        void onNoReplyFound();

        void showToast(String value);

        String getCommentId();

        String getCommentWriterUid();
    }
}
