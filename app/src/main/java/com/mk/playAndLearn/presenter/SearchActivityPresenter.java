package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.model.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;

public class SearchActivityPresenter {
    View view;
    Context context;
    ArrayList<User> list = new ArrayList<>();

    public SearchActivityPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void startAsynkTask(final String userName) {
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
                    getStudents(userName);
                } else {
                    view.handleNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void getStudents(final String userName) {
        if (!list.isEmpty()) {
            list.clear();
        }

        OnCompleteListener usersListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.v("searchLogging", "onCompleteListener");
                view.startRecyclerAdapter(list);
                if (task.isSuccessful()) {
                    Log.v("searchLogging", "task is successful , size is : " + task.getResult().size());
                    for (DocumentSnapshot document : task.getResult()) {
                        User user = new User();
                        String points = "";
                        boolean admin = false, online = false;
                        String name = (String) document.getString("userName");
                        String email = (String) document.getString("userEmail");
                        String uid = document.getId();
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


                        if (userType != null && userType.equals("طالب")
                                && !uid.equals(localCurrentUserUid)
                                && name != null) {//TODO : think about allowing challenges against teachers and others and ask my friends about thier opinions in that

                            user.setAdmin(admin);
                            user.setOnline(online);
                            user.setName(name);
                            user.setPoints(pointsInt);
                            user.setImageUrl(imageUrl);
                            user.setEmail(email);
                            user.setUid(uid);

                            list.add(user);
                        }
                    }
                } else {
                    Log.v("TAG", "failed");
                }

                view.notifyAdapter();
                view.hideProgressBar();
                view.hideSwipeRefreshLayout();

                if (list.size() == 0) {
                    view.showNoResultsTv();
                    view.hideNoInternetConnectionText();
                } else {
                    view.hideNoResultsTv();
                    view.hideNoInternetConnectionText();
                }
            }
        };

        fireStoreUsers.whereEqualTo("userName", userName)
                .whereEqualTo("grade", getSavedGrade(context))
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(usersListener);
    }

    public interface View {
        void handleNoInternetConnection();

        void hideSwipeRefreshLayout();

        void startRecyclerAdapter(ArrayList list);

        void notifyAdapter();

        void hideProgressBar();

        void hideNoInternetConnectionText();

        void showNoResultsTv();

        void hideNoResultsTv();
    }
}
