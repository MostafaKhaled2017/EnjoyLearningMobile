package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.model.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;


public class BestStudentsInGeneralChallengeActivityPresenter {
    private User user;
    private View view;
    private ArrayList<User> bestStudentsList = new ArrayList();
    ValueEventListener usersListener;

    public BestStudentsInGeneralChallengeActivityPresenter(View view) {
        //this.user = new User();
        this.view = view;
    }

    private void getBestStudents() {
        fireStoreUsers.orderBy("lastGeneralChallengeScore", Query.Direction.DESCENDING).endAt(1).limit(25).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (!bestStudentsList.isEmpty())
                        bestStudentsList.clear();

                    view.startRecyclerAdapter(bestStudentsList);
                    
                    for(DocumentSnapshot documentSnapshot: task.getResult()){
                        if(documentSnapshot.exists()){
                            user = new User();
                            boolean admin = false;
                            int generalChallengePoints = -1000;
                            String name =  documentSnapshot.getString("userName");
                            if(documentSnapshot.getLong("lastGeneralChallengeScore") != null)
                                generalChallengePoints = Integer.parseInt(documentSnapshot.getLong("lastGeneralChallengeScore").toString());
                            String imageUrl =  documentSnapshot.getString("userImage");
                            String userType =  documentSnapshot.getString("userType");
                            if (documentSnapshot.getBoolean("adminStudent") != null)
                                admin = documentSnapshot.getBoolean("adminStudent");

                            if (userType.equals("طالب") && generalChallengePoints != -1000) {
                                user.setAdmin(admin);
                                user.setName(name);
                                user.setPoints(generalChallengePoints);
                                user.setImageUrl(imageUrl);
                                bestStudentsList.add(user);
                            } 
                        }
                    }


                    //Adding position to List
                    int position = 1, previousPoints = -1;
                    for (int i = 0; i < bestStudentsList.size(); i++) {
                        User user = bestStudentsList.get(i);
                        if (previousPoints != -1 && previousPoints > user.getPoints()) {
                            position++;
                        }
                        bestStudentsList.get(i).setPosition(position);
                        previousPoints = user.getPoints();
                    }

                    if (bestStudentsList.size() == 0) {
                        view.showNoStudentsText();
                    } else {
                        view.hideNoStudentsText();
                        view.notifyAdapter();
                    }

                    view.hideProgressBar();
                    view.hideSwipeRefreshLayout();
                    
                }
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

        void hideSwipeRefreshLayout();

        void notifyAdapter();

        void startRecyclerAdapter(ArrayList list);

        void handleNoInternetConnection();

        void retryConnection();

        void showNoStudentsText();

        void hideNoStudentsText();
    }
}
