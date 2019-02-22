package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
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


public class BestStudentsFragmentPresenter {
    private User user;
    private View view;
    private ArrayList<User> bestStudentsList = new ArrayList();
    private Context context;

    public BestStudentsFragmentPresenter(View view, Context context) {
        //this.user = new User();
        this.view = view;
        this.context = context;
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

    private void getBestStudents() {
        fireStoreUsers.orderBy("points", Query.Direction.DESCENDING).limit(20).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int points = -1000;

                    if (!bestStudentsList.isEmpty())
                        bestStudentsList.clear();

                    view.startRecyclerAdapter(bestStudentsList);

                    for (DocumentSnapshot document : task.getResult()) {
                        user = new User();
                        boolean admin = false;
                        Log.v("Logging", "the key is : " + document.getId());
                        String name = (String) document.getString("userName");
                        if (document.getLong("points") != null)
                            points = Integer.parseInt(document.getLong("points").toString());
                        String imageUrl = document.getString("userImage");
                        String userType = (String) document.getString("userType");
                        String uid = (String) document.getId();

                        if (document.getBoolean("admin") != null)
                            admin = (boolean) document.getBoolean("admin");
                        if (userType.equals("طالب") && points != -1000) {
                            user.setAdmin(admin);
                            user.setName(name);
                            user.setPoints(points);
                            user.setImageUrl(imageUrl);
                            user.setUid(uid);
                            bestStudentsList.add(user);
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

                    view.hideProgressBar();
                    view.notifyAdapter();
                    view.hideSwipeRefreshLayout();
                } else {
                    Toast.makeText(context, "فشل تحميل البيانات من فضلك تأكد من الاتصال بالانترنت و أعد المحاولة", Toast.LENGTH_SHORT).show();
                }

                view.hideProgressBar();
            }
        });
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
