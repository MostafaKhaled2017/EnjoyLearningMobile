package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Comment;
import com.mk.playAndLearn.model.Post;
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

import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;

public class PostsInDetailsActivityPresenter {
    View view;
    Context context;
    ArrayList<Comment> commentsList = new ArrayList();
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

    public PostsInDetailsActivityPresenter(View view, Context context) {
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
                    getComments();
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }


    public void getComments() {
        clearList();
        view.startRecyclerAdapter(commentsList);
        fireStoreComments.whereEqualTo("postId", view.getPostId()).orderBy("date", Query.Direction.ASCENDING).addSnapshotListener((new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG, listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    DocumentSnapshot postDocument = dc.getDocument();
                    format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    switch (dc.getType()) {
                        case ADDED: {
                            view.onDataFound();
                            Comment comment = new Comment();
                            String userName = postDocument.getString("userName");
                            String userEmail = postDocument.getString("userEmail");
                            String content = postDocument.getString("content");
                            String userImage = postDocument.getString("userImage");
                            String date = format.format(postDocument.get("date"));
                            String writerUid = postDocument.getString("writerUid");
                            long votes = postDocument.getLong("votes");
                            boolean posted = postDocument.getBoolean("posted");
                            String commentId = postDocument.getId();

                            comment.setVotes(votes);
                            comment.setPosted(posted);
                            comment.setUserEmail(userEmail);
                            comment.setCommentId(commentId);
                            comment.setWriterUid(writerUid);
                            comment.setUserName(userName);
                            comment.setContent(content);
                            comment.setUserImage(userImage);
                            comment.setDate(date);
                            if (!existsInCommentsList(commentId)) {
                                commentsList.add(0, comment);
                                view.notifyAdapter();
                            }
                            break;
                        }
                        case MODIFIED:
                            String content = postDocument.getString("content");
                            String date = format.format(postDocument.get("date"));
                            long votes = postDocument.getLong("votes");
                            boolean posted = postDocument.getBoolean("posted");

                            for (int i = 0; i < commentsList.size(); i++) {
                                if (commentsList.get(i).getCommentId().equals(postDocument.getId())) {
                                    commentsList.get(i).setPosted(posted);
                                    commentsList.get(i).setDate(date);
                                    commentsList.get(i).setContent(content);
                                    commentsList.get(i).setVotes(votes);
                                    view.notifyAdapter();
                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            startAsynkTask();//TODO : think about removeing the child from the list only                            break;
                    }
                }
                if (commentsList.size() == 0) {
                    view.onNoCommentsFound();
                }
            }
        }));
    }

    void clearList() {
        if (!commentsList.isEmpty()) {
            commentsList.clear();
        }
    }

    public void addComment(String commentText) {
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

        map.put("userName", currentUserName);
        map.put("userEmail", localCurrentUserEmail);
        map.put("userImage", localCurrentUserImage);
        map.put("votes", 0);
        map.put("date", dateClass.getDate());
        map.put("posted", false);
        map.put("notified", view.getPostWriterUid() + "false");
        map.put("upVotedUsers", "users: ");
        map.put("downVotedUsers", "users: ");
        map.put("postId", view.getPostId());
        map.put("postWriterUid", view.getPostWriterUid());
        map.put("writerUid", localCurrentUserUid);
        map.put("content", commentText.trim());
        final DocumentReference currentCommentRef = fireStoreComments.document();
        final String commentId = currentCommentRef.getId();
        currentCommentRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currentCommentRef.update("posted", true);
                currentCommentRef.update("date", dateClass.getDate());
                view.showToast("تم إضافة التعليق بنجاح");
                view.notifyAdapter();
            }
        });
    }

    private boolean existsInCommentsList(String commentId) {
        for (Comment comment : commentsList) {
            if (comment.getCommentId().equals(commentId)) {
                return true;
            }
        }
        return false;
    }

    public interface View {
        String getPostId();

        String getPostWriterUid();

        void retryConnection();

        void startRecyclerAdapter(ArrayList commentsList);

        void onNoInternetConnection();

        void onDataFound();

        void hideProgressBar();

        void notifyAdapter();

        void onNoCommentsFound();

        void showToast(String value);
    }
}
