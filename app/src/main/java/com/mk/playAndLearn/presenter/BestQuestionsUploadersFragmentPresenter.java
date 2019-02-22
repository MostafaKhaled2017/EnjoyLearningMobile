package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

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



public class BestQuestionsUploadersFragmentPresenter {
    private User user;
    private View view;
    private ArrayList<User> bestQuestionsUploadersList = new ArrayList();
    private Context context;

    public BestQuestionsUploadersFragmentPresenter(View view, Context context) {
        //this.user = new User();
        this.view = view;
        this.context = context;
    }

    private void getBestStudents() {
        fireStoreUsers.orderBy("acceptedQuestions", Query.Direction.DESCENDING)
                .limit(20)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (!bestQuestionsUploadersList.isEmpty())
                        bestQuestionsUploadersList.clear();

                    view.startRecyclerAdapter(bestQuestionsUploadersList);
                    for(DocumentSnapshot document:task.getResult()){
                        user = new User();
                        boolean admin = false;
                        int acceptedQuestions = -1000;
                        String name = (String) document.getString("userName");
                        String uid = (String) document.getId();
                        if(document.getLong("acceptedQuestions") != null)
                            acceptedQuestions = Integer.parseInt(document.getLong("acceptedQuestions").toString());
                        String imageUrl = (String) document.getString("userImage");
                        String userType = (String) document.getString("userType");
                        String email = (String) document.getString("userEmail");
                        if (document.getBoolean("admin") != null)
                            admin = (boolean) document.getBoolean("admin");

                        if (userType.equals("طالب") && acceptedQuestions != -1000
                                && email != null) {
                            user.setAdmin(admin);
                            user.setName(name);
                            user.setEmail(email);
                            user.setAcceptedQuestions(acceptedQuestions);
                            user.setImageUrl(imageUrl);
                            user.setUid(uid);
                            bestQuestionsUploadersList.add(user);
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
                else {
                    Toast.makeText(context, "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت و أعد المحاولة", Toast.LENGTH_SHORT).show();
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
