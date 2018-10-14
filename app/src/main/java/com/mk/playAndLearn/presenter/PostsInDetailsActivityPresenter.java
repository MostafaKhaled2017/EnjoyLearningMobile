package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Comment;

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
import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserImage;
import static com.mk.playAndLearn.utils.Strings.currentUserName;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class PostsInDetailsActivityPresenter {
    View view;
    ArrayList commentsList = new ArrayList();
    ChildEventListener commentsListener;

    public PostsInDetailsActivityPresenter(View view){
        this.view = view;
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
                comment.setUserName(userName);
                comment.setContent(content);
                comment.setUserImage(userImage);
                comment.setDate(date);
                commentsList.add(0, comment);
                view.notifyAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                startAsynkTask();
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

    void clearList(){
        if(!commentsList.isEmpty()) {
            commentsList.clear();
        }
    }

    public void addComment(String commentText){
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String date = format.format(today);

        Map<String, Object> map = new HashMap<>();
        map.put("userName", currentUserName);
        map.put("userEmail", currentUserEmail);
        map.put("userImage", currentUserImage);
        map.put("votes", 0);
        map.put("date", date);
        map.put("postId", view.getPostId());
        map.put("userUid", currentUserUid);
        map.put("content", commentText);
        commentsReference.push().setValue(map);
    }

    public void removeListeners(){
        commentsReference.removeEventListener(commentsListener);
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
    }
}
