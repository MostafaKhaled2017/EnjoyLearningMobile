package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;

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
    private ArrayList bestStudentsList = new ArrayList();

    public BestStudentsActivityPresenter(View view) {
        //this.user = new User();
        this.view = view;
    }

    private void getBestStudents() {
        usersReference.orderByChild("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!bestStudentsList.isEmpty())
                    bestStudentsList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    user = new User();
                    String name = dataSnapshot1.child("userName").getValue().toString();
                    String points = dataSnapshot1.child("points").getValue().toString();
                    String imageUrl = dataSnapshot1.child("userImage").getValue().toString();
                    String userType = dataSnapshot1.child("userType").getValue().toString();
                    if (userType.equals("طالب")) {
                        user.setName(name);
                        user.setPoints(Integer.parseInt(points));
                        user.setImageUrl(imageUrl);
                        bestStudentsList.add(0, user);
                    }
                    view.startRecyclerAdapter(bestStudentsList);
                    view.hideProgressBar();
                    view.notifyAdapter();
                }
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


    public interface View {

        void hideProgressBar();

        void notifyAdapter();

        void startRecyclerAdapter(ArrayList list);

        void handleNoInternetConnection();

        void retryConnection();
    }
}