package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Comment;
import com.mk.playAndLearn.model.Post;

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

import static com.mk.playAndLearn.utils.Firebase.commentsReference;
import static com.mk.playAndLearn.utils.Firebase.postsReference;
import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserImage;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class PostsInDetailsActivityPresenter {
    View view;
    Context context;
    ArrayList<Comment> commentsList = new ArrayList();
    ChildEventListener commentsListener;
    String date;

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
        commentsListener = commentsReference.orderByChild("postId").equalTo(view.getPostId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Lesson value = dataSnapshot.getValue(Lesson.class);
                view.onDataFound();
                Comment comment = new Comment();
                String userName = dataSnapshot.child("userName").getValue().toString();
                String content = dataSnapshot.child("content").getValue().toString();
                String userImage = dataSnapshot.child("userImage").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                String writerUid = dataSnapshot.child("writerUid").getValue().toString();
                long votes = (long) dataSnapshot.child("votes").getValue();
                boolean posted = (boolean) dataSnapshot.child("posted").getValue();
                String commentId = dataSnapshot.getKey();
                comment.setVotes(votes);
                comment.setPosted(posted);
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
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String content = dataSnapshot.child("content").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                long votes = (long) dataSnapshot.child("votes").getValue();
                boolean posted = (boolean) dataSnapshot.child("posted").getValue();

                for (int i = 0; i < commentsList.size(); i++) {
                    if (commentsList.get(i).getCommentId().equals(dataSnapshot.getKey())) {
                        commentsList.get(i).setPosted(posted);
                        commentsList.get(i).setDate(date);
                        commentsList.get(i).setContent(content);
                        commentsList.get(i).setVotes(votes);
                        view.notifyAdapter();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                startAsynkTask();//TODO : it 's better to change that to more efficient way
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getActivity(), "فشل تحميل البينات من فضلك تأكد من الاتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "database error : " + databaseError);
                view.hideProgressBar();
            }
        });
        commentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (commentsList.size() == 0) {
                    view.onNoCommentsFound();
                }

                commentsReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void clearList() {
        if (!commentsList.isEmpty()) {
            commentsList.clear();
        }
    }

    public void addComment(String commentText) {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        date = format.format(today);

        Map<String, Object> map = new HashMap<>();
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode //TODO : check this
        String currentUserName = pref.getString("currentUserName", "غير معروف");
        Log.v("sharedPreference", " current userName is : " + currentUserName);

        map.put("userName", currentUserName);
        map.put("userEmail", currentUserEmail);
        map.put("userImage", currentUserImage);
        map.put("votes", 0);
        map.put("date", date);
        map.put("posted", false);
        map.put("upVotedUsers", "users: ");
        map.put("downVotedUsers", "users: ");
        map.put("postId", view.getPostId());
        map.put("writerUid", currentUserUid);
        map.put("content", commentText.trim());
        final String commentId = commentsReference.push().getKey();
        final DatabaseReference currentCommentRef = commentsReference.child(commentId);
        currentCommentRef.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currentCommentRef.child("posted").setValue(true);
                currentCommentRef.child("date").setValue(date);
                view.showToast("تم إضافة التعليق بنجاح");
                view.notifyAdapter();
            }
        });
    }

    public void removeListeners() {
        if (commentsListener != null)
            commentsReference.removeEventListener(commentsListener);
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
