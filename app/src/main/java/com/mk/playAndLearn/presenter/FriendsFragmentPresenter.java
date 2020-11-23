package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.playAndLearn.activity.AddQuestionActivity;
import com.mk.playAndLearn.model.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;

public class FriendsFragmentPresenter {
    View view;
    Context context;
    ArrayList<User> list = new ArrayList<>();
    String[] friendsArray;

    public FriendsFragmentPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void startAsynkTask(final String subject) {
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
                    getStudents(subject);
                } else {
                    if(!list.isEmpty()){
                        list.clear();
                    }
                    view.handleNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void getStudents(final String subject) {
        if (!list.isEmpty()) {
            list.clear();
        }
        view.showProgressBar();
        view.startRecyclerAdapter(list);

        final OnCompleteListener usersListener = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String uid = "";
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                    User user = new User();
                    String points = "";
                    boolean admin = false, online = false;
                    String name = (String) document.getString("userName");
                    String email = (String) document.getString("userEmail");
                    String grade = (String) document.getString("grade");
                    uid = document.getId();
                    if (document.getLong("points") != null)
                        points = document.getLong("points").toString();
                    if (document.getBoolean("admin") != null)
                        admin = (boolean) document.getBoolean("admin");
                    String imageUrl = (String) document.getString("userImage");
                    String userType = (String) document.getString("userType");
                    String userSchoolType = (String) document.getString("userSchoolType");

                    int pointsInt;
                    try {
                        pointsInt = Integer.parseInt(points);
                    } catch (Exception ex) {
                        Log.v("pointsException", "exception is : " + ex);
                        pointsInt = 0;
                    }

                    String subjectSchoolType = AddQuestionActivity.getSchoolType(subject);

                    if (userType != null && grade!= null && userType.equals("طالب")
                            && !uid.equals(localCurrentUserUid)
                            && name != null && grade.equals(grade)) {//TODO : think about allowing challenges against teachers and others and ask my friends about thier opinions in that

                        user.setAdmin(admin);
                        user.setOnline(online);
                        user.setName(name);
                        user.setPoints(pointsInt);
                        user.setImageUrl(imageUrl);
                        user.setEmail(email);
                        user.setUid(uid);


                        if (userSchoolType != null && subjectSchoolType.equals("both")
                                || (subjectSchoolType.equals("languages") && userSchoolType.equals("لغات"))
                                || (subjectSchoolType.equals("arabic") && userSchoolType.equals("عربى"))) {
                            if(!existsInList(user, list) && grade.equals(getSavedGrade(context))) {
                                list.add(user);
                            }
                        }
                    }

                    if (uid.equals(friendsArray[friendsArray.length - 1])) {
                        view.notifyAdapter();
                        view.hideProgressBar();
                        view.hideSwipeRefreshLayout();
                        view.hideNoInternetConnectionText();
                        if(list.size() == 0){
                            view.showNoFriendsTv();
                        }
                    }
                }
                } else {
                    Log.v("TAG", "failed");
                    view.hideProgressBar();
                    view.hideSwipeRefreshLayout();
                    view.hideNoInternetConnectionText();
                }
            }
        };

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fireStoreUsers.document(currentUserUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String friendsString = documentSnapshot.getString("friends");
                        friendsArray = friendsString.split(" ");

                        if (friendsArray.length == 1) {
                            //handle no friends exists
                            view.hideNoInternetConnectionText();
                            view.hideSwipeRefreshLayout();
                            view.showNoFriendsTv();
                            view.hideProgressBar();
                        } else {
                            view.hideNoFriendsTv();

                            //starting from 1 to skip the default word at the begging
                            for (int i = 1; i < friendsArray.length; i++) {
                                String id = friendsArray[i];
                                fireStoreUsers.document(id).get().addOnCompleteListener(usersListener);
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "فشل عرض الإصدقاء برجاء إعادة المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean existsInList(User user, ArrayList<User> list) {
        for(User user2: list){
            if(user.getUid().equals(user2.getUid())){
                return true;
            }
        }
        return false;
    }

    public interface View {
        void handleNoInternetConnection();

        void hideSwipeRefreshLayout();

        void startRecyclerAdapter(ArrayList list);

        void notifyAdapter();

        void hideProgressBar();

        void showProgressBar();

        void hideNoInternetConnectionText();

        void showNoFriendsTv();

        void hideNoFriendsTv();
    }
}
