package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.model.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.usersReference;


public class BestStudentsActivityPresenter {
    private User user;
    private View view;
    private ArrayList<User> bestStudentsList = new ArrayList();
    ValueEventListener usersListener;

    public BestStudentsActivityPresenter(View view) {
        //this.user = new User();
        this.view = view;
    }

    private void getBestStudents() {
        usersReference.orderByChild("points").limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int points = -1000;

                usersListener = this;
                if (!bestStudentsList.isEmpty())
                    bestStudentsList.clear();

                view.startRecyclerAdapter(bestStudentsList);

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    user = new User();
                    boolean admin = false;
                    Log.v("Logging", "the key is : " + dataSnapshot1.getKey());
                    String name = (String) dataSnapshot1.child("userName").getValue();
                    if(dataSnapshot1.child("points").getValue() != null)
                        points = Integer.parseInt(dataSnapshot1.child("points").getValue().toString());
                    String imageUrl = (String) dataSnapshot1.child("userImage").getValue();
                    String userType = (String) dataSnapshot1.child("userType").getValue();

                    if(dataSnapshot1.child("adminStudent").getValue() != null)
                         admin = (boolean) dataSnapshot1.child("adminStudent").getValue();
                    if (userType.equals("طالب") && points != -1000) {
                        user.setAdmin(admin);
                        user.setName(name);
                        user.setPoints(points);
                        user.setImageUrl(imageUrl);
                        bestStudentsList.add(0, user);
                    }

                }

                //Adding position to List
                int position = 1, previousPoints = -1;
                for (int i = 0; i < bestStudentsList.size(); i++) {
                    User user = bestStudentsList.get(i);
                    if (previousPoints != -1 && previousPoints > user.getPoints()){
                        position ++;
                    }
                    bestStudentsList.get(i).setPosition(position);
                    previousPoints = user.getPoints();
                }

                view.hideProgressBar();
                view.notifyAdapter();
                view.hideSwipeRefreshLayout();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                view.hideProgressBar();
            }
        });
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
                    getBestStudents();
                } else {
                    view.handleNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void removeListeners() {
        if (usersListener != null)
            usersReference.removeEventListener(usersListener);
    }

    public interface View {

        void hideProgressBar();

        void hideSwipeRefreshLayout();

        void notifyAdapter();

        void startRecyclerAdapter(ArrayList list);

        void handleNoInternetConnection();

        void retryConnection();
    }
}
