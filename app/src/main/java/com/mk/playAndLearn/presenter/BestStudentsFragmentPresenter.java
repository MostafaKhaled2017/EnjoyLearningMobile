package com.mk.playAndLearn.presenter;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class BestStudentsFragmentPresenter {
    private User user;
    private View view;
    private ArrayList<User> usersList = new ArrayList();
    private Context context;

    private final String TAG = "BestStudentsFragment";

    public BestStudentsFragmentPresenter(View view, Context context) {
        //this.user = new User();
        this.view = view;
        this.context = context;
    }

    public void startAsynkTask(final String currentOption) {
        //TODO : search for a solution to this error
        usersList.clear();
        view.showProgressBar();

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
                    getBestStudents(currentOption);
                } else {
                    view.handleNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    private void getBestStudents(String currentOption) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentOption.equals("قائمة المتصدرين")) {
            view.startRecyclerAdapter(usersList, TAG);
            fireStoreUsers.orderBy("points", Query.Direction.DESCENDING).limit(20).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    getStudentsData(task);
                }
            });
        } else if (currentOption.equals("أقرب المنافسين")) {
            view.startRecyclerAdapter(usersList, "LastChallengers");

            fireStoreUsers.document(currentUserUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot currentUserSnapshot) {
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

                                        if (usersList.size() == 0) {
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
    }

    private void getStudentsData(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {

            if (!usersList.isEmpty())
                usersList.clear();

            for (DocumentSnapshot document : task.getResult()) {
                addUserData(document, false);
            }
            //Adding position to List
            int position = 1, previousPoints = -1;
            for (int i = 0; i < usersList.size(); i++) {
                User user = usersList.get(i);
                if (previousPoints != -1 && previousPoints > user.getPoints()) {
                    position++;
                }
                usersList.get(i).setPosition(position);
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
                usersList.add(0, user);
            } else {
                usersList.add(user);
            }
        }
    }

    public interface View {

        void hideProgressBar();

        void hideSwipeRefreshLayout();

        void notifyAdapter();

        void startRecyclerAdapter(ArrayList list, String Tag);

        void handleNoInternetConnection();

        void retryConnection();

        void showNoStudentsText();

        void showProgressBar();

        void hideNoStudentsText();
    }
}
