package com.mk.playAndLearn.presenter;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.model.User;
import com.mk.playAndLearn.utils.Firebase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.fireStoreUsers;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedImage;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedPoints;

public class ChallengersFragmentPresenter {
    private Lesson lesson;
    private View view;
    private Context context;

    final String TAG = "ChallengersFragmentPresenter";

    ArrayList<User> studentsList = new ArrayList();
    ChildEventListener lessonsListener;

    public ChallengersFragmentPresenter(View view, Context context) {
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
                    if (!studentsList.isEmpty()) {
                        studentsList.clear();
                        view.notifyAdapter();
                    }
                    getNearestChallengers();
                } else {
                    view.handleNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void getNearestChallengers() {
        view.startRecyclerAdapter(studentsList);

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fireStoreUsers.document(currentUserUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot currentUserSnapshot) {
                //To get the highest user in the required query
                fireStoreUsers.orderBy("points").startAt(currentUserSnapshot).limit(4).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot lastUser = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            fireStoreUsers.orderBy("points", Query.Direction.DESCENDING).startAt(lastUser).limit(7).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                            addUserData(documentSnapshot, false);
                                        }
                                    }

                                    if (studentsList.size() == 0) {
                                        view.showNoStudentsText();
                                    } else {
                                        view.hideNoStudentsText();
                                        view.notifyAdapter();
                                    }

                                    view.hideProgressBar();
                                    view.hideSwipeRefreshLayout();
                                }
                            });
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(((Fragment) view).getActivity(), "حدثت مشكلة أثناء تحميل البيانات برجاء إعادة المحاولة لاحقا", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //the boolean is to reverse the order of students
    void addUserData(DocumentSnapshot document, boolean reverse) {
        User user = new User();
        boolean admin = false;
        int points = -1000;
        String name = (String) document.getString("userName");
        String uid = (String) document.getId();
        if (document.getLong("points") != null)
            points = Integer.parseInt(document.getLong("points").toString());
        String imageUrl = (String) document.getString("userImage");
        String userType = (String) document.getString("userType");
        String email = (String) document.getString("userEmail");
        if (document.getBoolean("admin") != null)
            admin = (boolean) document.getBoolean("admin");

        if (userType.equals("طالب") && points != -1000
                && email != null) {
            user.setAdmin(admin);
            user.setName(name);
            user.setEmail(email);
            user.setPoints(points);
            user.setImageUrl(imageUrl);
            user.setUid(uid);

            if (reverse) {
                studentsList.add(0, user);
            } else {
                studentsList.add(user);
            }
        }
    }

    public void getUserData(){
        String name = getSavedName(context);
        String imageUrl = getSavedImage(context);
        long points = getSavedPoints(context);

        view.setUserData(name, imageUrl, points);
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

        void setUserData(String name, String imageUrl, long points);

    }
}
