package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.AbsListView;

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
import com.mk.playAndLearn.activity.MainActivity;
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
    DocumentSnapshot lastVisible;
    int lastPosition;
    Map<String, Object> map;
    ArrayList<Post> postsList = new ArrayList();
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

    SharedPreferences pref;
    String currentUserName ;
    String grade;
    int limit = 5;
    Query firstQuery;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;


    ChildEventListener postsEventListener;

    public HomeFragmentPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
        pref = context.getSharedPreferences("MyPref", 0); // 0 - for private mode //TODO : check thisg
        currentUserName = pref.getString("currentUserName", "غير معروف");
        grade = pref.getString("grade", "غير معروف");

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

    public int getPostsListSize(){
        return postsList.size();
    }

    public void addPost(String postText, String subject) {
        Date today = new Date();
        final DateClass dateClass = new DateClass();
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        dateClass.setDate(today);

        String localCurrentUserImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
        String localCurrentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.v("sharedPreference", " current userName is : " + currentUserName);


        map = new HashMap<>();
        map.put("content", postText.trim());
        map.put("date", dateClass.getDate());
        map.put("downVotedUsers", "users: ");
        map.put("grade", grade);
        map.put("subject", subject);
        map.put("term", 2);//Todo : make the user control this by entering it
        map.put("upVotedUsers", "users: ");
        map.put("votes", (long)0);
        map.put("writerUid", localCurrentUserUid);
        map.put("writerName", currentUserName);
        map.put("image", localCurrentUserImage);
        map.put("email", localCurrentUserEmail);
        map.put("posted", false);

       /* final String postId = postsReference.push().getKey();
        final DatabaseReference currentPostRef = postsReference.child(postId);*/

        final String postId = fireStorePosts.document().getId();
        final DocumentReference currentPostRef = fireStorePosts.document(postId);

        //Add the newly added post to the list
        getPostDataFromMap(map, postId);

        currentPostRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //TO ensure that the child exists

                Map<String, Object> updates = new HashMap<>();

                updates.put("posted", true);
                updates.put("date", dateClass.getDate());

                currentPostRef.update(updates);

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
                view.hideNoPostsText();
            }
        });
    }

    public void getPosts(final String currentSubject) {
        postsList = new ArrayList<>();
        view.startRecyclerAdapter(postsList);
        view.showProgressBar();
        view.hideNoPostsText();
        view.notifyAdapter();

        OnCompleteListener postsListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        getPostData(document);
                    }

                    ((MainActivity)context).updateLastOnlineDateAndShowRewardsPage();

                    if (postsList.size() == 0) {
                        view.onNoPostsExists();
                    } else {
                        view.onDataFound();
                    }

                    if(task.getResult().size() > 0 ) {
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        lastPosition = task.getResult().size() - 1;
                    }


                    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true;
                            }
                        }

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = linearLayoutManager.getChildCount();
                            int totalItemCount = linearLayoutManager.getItemCount();

                            if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                isScrolling = false;

                                OnCompleteListener secondQueryCompleteListener = new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                        if (t.isSuccessful()) {
                                            for (DocumentSnapshot d : t.getResult()) {
                                                getPostData(d);
                                            }
                                            view.notifyAdapter();

                                            if(t.getResult().size() > 0) {
                                                lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                                lastPosition += 5;
                                            }

                                            Log.v("LoggingPosition", "last position is : " + lastPosition);

                                            if (t.getResult().size() < limit) {
                                                isLastItemReached = true;
                                            }
                                        }
                                    }
                                };


                                if (currentSubject.equals("كل المواد")) {
                                    Query nextQuery = fireStorePosts.whereEqualTo("grade", grade).orderBy("date", Query.Direction.DESCENDING)
                                            .startAfter(lastVisible).limit(limit);
                                    nextQuery.get().addOnCompleteListener(secondQueryCompleteListener);
                                }
                                else {
                                    Query nextQuery = fireStorePosts.whereEqualTo("subject", currentSubject).whereEqualTo("grade", grade)
                                            .orderBy("date", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
                                    nextQuery.get().addOnCompleteListener(secondQueryCompleteListener);
                                }
                            }
                        }
                    };
                    view.setOnScrollListener(onScrollListener);


                } else {
                    Log.v("TAG", "failed");
                }
            }
        };

        if (currentSubject.equals("كل المواد")) {
            firstQuery = fireStorePosts.whereEqualTo("grade", grade).orderBy("date", Query.Direction.DESCENDING).limit(limit);
            firstQuery.get().addOnCompleteListener(postsListener);
        } else {
            firstQuery = fireStorePosts.whereEqualTo("subject", currentSubject).whereEqualTo("grade", grade)
                    .orderBy("date", Query.Direction.DESCENDING).limit(limit);
            firstQuery.get().addOnCompleteListener(postsListener);
        }
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
        String grade = postDocument.getString("grade");
        String postId = postDocument.getId();
        long votes = postDocument.getLong("votes");
        String writerUid = postDocument.getString("writerUid");
        boolean posted = postDocument.getBoolean("posted");

       // Log.v("postsLogging","content is : " + postContent + " , grade is : " + grade);

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

void getPostDataFromMap(Map<String, Object> map, String id) {
        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String postDate;

        post = new Post();
        String postContent = (String) map.get("content");
        postDate = format.format(map.get("date"));
        String postWriter = (String) map.get("writerName");
        String postWriterEmail = (String) map.get("email");
        String postImage = (String) map.get("image");
        String postId = id;
        long votes = (long) map.get("votes");
        String writerUid = (String) map.get("writerUid");
        boolean posted = (boolean) map.get("posted");

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
            postsList.add(0, post); //To be added at the begging
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

        void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener);

    }
}
