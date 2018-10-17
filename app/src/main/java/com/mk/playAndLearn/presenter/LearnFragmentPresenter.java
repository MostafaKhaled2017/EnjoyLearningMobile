package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.playAndLearn.model.Lesson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.lessonsReference;

public class LearnFragmentPresenter {
    private Lesson lesson;
    private View view;

    ArrayList lessonsList = new ArrayList();
    ChildEventListener lessonsListener;

    public LearnFragmentPresenter(View view) {
        this.view = view;
    }

    public void startAsynkTask(final String currentSubject) {
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
                    if(!lessonsList.isEmpty()){
                        lessonsList.clear();
                        view.notifyAdapter();
                    }
                    getLessons(currentSubject);
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void getLessons(String currentSubject) {
        view.startRecyclerAdapter(lessonsList);
        if (!lessonsList.isEmpty()) {
            lessonsList.clear();
            view.notifyAdapter();
        }
        view.onLoadingData();
        lessonsListener = lessonsReference.orderByChild("subject").equalTo(currentSubject).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                view.onDataFound();

                 Lesson value = dataSnapshot.getValue(Lesson.class);
                lesson = new Lesson();
                boolean reviewed = (boolean) dataSnapshot.child("reviewed").getValue();
                if (reviewed) {
                    String title = value.getTitle();
                    String content = value.getContent();
                    String arabicPosition = value.getArabicPosition();
                    lesson.setTitle(title);
                    lesson.setContent(content);
                    lesson.setArabicPosition(arabicPosition);
                    lessonsList.add(lesson);
                }
                view.notifyAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getActivity(), "فشل تحميل البينات من فضلك تأكد من الاتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "database error : " + databaseError);
                view.hideProgressBar();
            }
        });

        lessonsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                view.hideProgressBar();
                if (lessonsList.size() == 0) {
                    view.showNoLessonsTextView();
                }

                lessonsReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeListeners(){
        if(lessonsListener != null)
            lessonsReference.removeEventListener(lessonsListener);
    }

    public interface View {
        void retryConnection();

        void hideNoLessonsTextView();

        void showNoLessonsTextView();

        void onNoInternetConnection();

        void notifyAdapter();

        void onLoadingData();

        void onDataFound();

        void hideProgressBar();

        void startRecyclerAdapter(ArrayList list);

    }
}
