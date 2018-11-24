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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
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

import static com.mk.playAndLearn.utils.Firebase.fireStorePosts;

public class HomeFragmentPresenter {
    Post post;
    View view;
    Context context;
    ArrayList<Post> postsList = new ArrayList();
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

    ChildEventListener postsEventListener;

    public HomeFragmentPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void startAsynkTask(final String currentSubject) {
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
                    getPosts(currentSubject);
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void addPost(String postText, String subject) {
        Date today = new Date();
        final DateClass dateClass = new DateClass();
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        dateClass.setDate(today);

        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode //TODO : check this
        String currentUserName = pref.getString("currentUserName", "غير معروف");
        String localCurrentUserImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.v("sharedPreference", " current userName is : " + currentUserName);

        Map<String, Object> map = new HashMap<>();
        map.put("content", postText.trim());
        map.put("writerName", currentUserName);
        map.put("subject", subject);
        map.put("writerUid", localCurrentUserUid);
        map.put("image", localCurrentUserImage);
        map.put("email", localCurrentUserEmail);
        map.put("upVotedUsers", "users: ");
        map.put("downVotedUsers", "users: ");
        map.put("posted", false);
        map.put("votes", 0);
        map.put("date", dateClass.getDate());

       /* final String postId = postsReference.push().getKey();
        final DatabaseReference currentPostRef = postsReference.child(postId);*/

        final DocumentReference currentPostRef = fireStorePosts.document();
        final String postId = currentPostRef.getId();//TODO : check this


        currentPostRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //TO ensure that the child exists
                currentPostRef.update("posted", true);
                currentPostRef.update("date", dateClass.getDate());
                format.setTimeZone(TimeZone.getTimeZone("GMT+2"));


                for (int i = 0; i < postsList.size(); i++) {
                    if (postsList.get(i).getId().equals(postId)) {
                        postsList.get(i).setPosted(true);
                        postsList.get(i).setDate(format.format(dateClass.getDate()));
                        view.notifyAdapter();
                        break;
                    }
                }

                Log.v("Logging", "current posts reference is : " + currentPostRef);
                view.showToast("تم إضافة المنشور بنجاح");
                view.notifyAdapter();
            }
        });

    }

    public void getPosts(final String currentSubject) {
        view.startRecyclerAdapter(postsList);
        view.showProgressBar();
        view.hideNoPostsText();
        if (!postsList.isEmpty()) {//TODO : check if we need to create new objects instead of using clear
            postsList.clear();
            view.notifyAdapter();
        }

        EventListener postsSnapshotListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG, listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    DocumentSnapshot postDocument = dc.getDocument();
                    switch (dc.getType()) {
                        case ADDED:
                            getPostData(postDocument);
                            break;
                        case MODIFIED:
                            Log.d("TAG", "Modified city: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            startAsynkTask(currentSubject);//TODO : think about removeing the child from the list only                            break;
                    }
                }

                if (postsList.size() == 0) {
                    view.onNoPostsExists();
                }
                else {
                    view.onDataFound();
                }

            }
        };

        if(currentSubject.equals("كل المواد"))
            fireStorePosts.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(postsSnapshotListener);
        else
            fireStorePosts.whereEqualTo("subject", currentSubject).orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(postsSnapshotListener);
    }

    private boolean existsInPostsList(String postId) {
        for (Post p : postsList) {
            if (p.getId().equals(postId)) {
                return true;
            }
        }
        return false;
    }

    void getPostData(DocumentSnapshot postDocument) {
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String postDate;

        post = new Post();
        String postContent = postDocument.getString("content");
        postDate = format.format(postDocument.get("date"));
        String postWriter = postDocument.getString("writerName");
        String postWriterEmail = postDocument.getString("email");
        String postImage = postDocument.getString("image");
        String postId = postDocument.getId();
        long votes = postDocument.getLong("votes");
        String writerUid = postDocument.getString("writerUid");
        boolean posted = postDocument.getBoolean("posted");

        post.setPosted(posted);
        if (writerUid != null)
            post.setWriterUid(writerUid);
        if (postWriterEmail != null)
            post.setEmail(postWriterEmail);
        if (postContent != null)
            post.setContent(postContent);
        if (postDate != null)
            post.setDate(postDate);
        if (postWriter != null)
            post.setWriter(postWriter);
        if (postImage != null)
            post.setImage(postImage);
        if (postId != null)
            post.setId(postId);

        post.setVotes(votes);

        if (!existsInPostsList(postId)) {
            postsList.add(post);
            view.notifyAdapter();
        }
    }

    public interface View {
        void retryConnection();

        void startRecyclerAdapter(ArrayList postsList);

        void onNoInternetConnection();

        void showToast(String value);

        void notifyAdapter();

        void onDataFound();

        void hideProgressBar();

        void hideNoPostsText();

        void showProgressBar();

        void onNoPostsExists();

    }
}
