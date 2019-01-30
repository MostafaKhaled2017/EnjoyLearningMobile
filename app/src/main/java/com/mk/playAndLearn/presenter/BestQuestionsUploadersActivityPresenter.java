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
import static com.mk.playAndLearn.utils.Strings.adminEmail;


public class BestQuestionsUploadersActivityPresenter {
    private User user;
    private View view;
    private ArrayList<User> bestQuestionsUploadersList = new ArrayList();
    ValueEventListener usersListener;

    public BestQuestionsUploadersActivityPresenter(View view) {
        //this.user = new User();
        this.view = view;
    }

    private void getBestStudents() {
        usersReference.orderByChild("acceptedQuestions")
                .startAt(1)
                .limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersListener = this;
                if (!bestQuestionsUploadersList.isEmpty())
                    bestQuestionsUploadersList.clear();

                view.startRecyclerAdapter(bestQuestionsUploadersList);

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    user = new User();
                    boolean admin = false;
                    int acceptedQuestions = -1000;
                    String name = (String) dataSnapshot1.child("userName").getValue();
                    if(dataSnapshot1.child("acceptedQuestions").getValue() != null)
                         acceptedQuestions = Integer.parseInt(dataSnapshot1.child("acceptedQuestions").getValue().toString());
                    String imageUrl = (String) dataSnapshot1.child("userImage").getValue();
                    String userType = (String) dataSnapshot1.child("userType").getValue();
                    String email = (String) dataSnapshot1.child("userEmail").getValue();
                    if (dataSnapshot1.child("admin").getValue() != null)
                        admin = (boolean) dataSnapshot1.child("admin").getValue();

                    if (userType.equals("طالب") && acceptedQuestions != -1000
                           && email != null) {
                        user.setAdmin(admin);
                        user.setName(name);
                        user.setEmail(email);
                        user.setAcceptedQuestions(acceptedQuestions);
                        user.setImageUrl(imageUrl);
                        bestQuestionsUploadersList.add(0, user);
                    }
                }

                //Adding position to List
                int position = 1, previousAcceptedQuestions = -1;
                for (int i = 0; i < bestQuestionsUploadersList.size(); i++) {
                    User user = bestQuestionsUploadersList.get(i);
                    if (previousAcceptedQuestions != -1 && previousAcceptedQuestions > user.getAcceptedQuestions()) {
                        position++;
                    }
                    bestQuestionsUploadersList.get(i).setPosition(position);
                    previousAcceptedQuestions = user.getAcceptedQuestions();
                }

                if (bestQuestionsUploadersList.size() == 0) {
                    view.showNoStudentsText();
                } else {
                    view.hideNoStudentsText();
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

        void showNoStudentsText();

        void hideNoStudentsText();
    }
}
