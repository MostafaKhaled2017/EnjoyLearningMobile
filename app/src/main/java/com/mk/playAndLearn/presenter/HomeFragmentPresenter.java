package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.model.Post;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.postsReference;
import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserImage;
import static com.mk.playAndLearn.utils.Strings.currentUserName;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class HomeFragmentPresenter {
    Post post;
    View view;
    ArrayList postsList = new ArrayList();
    ChildEventListener postsEventListener;

    public HomeFragmentPresenter(View view) {
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
                    getPosts();
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void addPost(String postText) {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String date = format.format(today);
        Log.v("Logging2", date);
        if (view.validateInput(postText)) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", postText);
            map.put("date", date);
            map.put("writerName", currentUserName);
            map.put("writerUid", currentUserUid);
            map.put("image", currentUserImage);
            map.put("email", currentUserEmail);
            map.put("votes", 0);
            postsReference.push().setValue(map);
            view.onPostAddedSuccessfully();
        }
    }

    public void getPosts() {
        view.startRecyclerAdapter(postsList);
        if (!postsList.isEmpty()) {//TODO : check if we need to create new objects instead of using clear
            postsList.clear();
            view.notifyAdapter();
        }
        postsEventListener =  postsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                view.onDataFound();
                post = new Post();
                String postContent = dataSnapshot.child("content").getValue().toString();
                String postDate = dataSnapshot.child("date").getValue().toString();//TODO : solve the date problem
                String postWriter = dataSnapshot.child("writerName").getValue().toString();
                String postImage = dataSnapshot.child("image").getValue().toString();
                String postId = dataSnapshot.getKey();
                post.setContent(postContent);
                post.setDate(postDate);
                post.setWriter(postWriter);
                post.setImage(postImage);
                post.setId(postId);
                postsList.add(0, post);
                view.notifyAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                startAsynkTask();//TODO : think about removeing the child from the list only
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getActivity(), "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت", Toast.LENGTH_SHORT).show();
                view.hideProgressBar();
                Log.v("Logging", "error loading data : " + databaseError);
            }
        });

        postsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (postsList.size() == 0) {
                    view.onNoPostsExists();
                }
                postsReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeListeners(){
        postsReference.removeEventListener(postsEventListener);
    }
    public interface View {
        void retryConnection();

        void startRecyclerAdapter(ArrayList postsList);

        void onNoInternetConnection();

        boolean validateInput(String postText);

        void onPostAddedSuccessfully();

        void notifyAdapter();

        void onDataFound();

        void hideProgressBar();

        void onNoPostsExists();
    }
}
