package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.model.Lesson;
import com.mk.playAndLearn.model.Post;

import org.w3c.dom.Document;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;

public class LearnFragmentPresenter {
    private Lesson lesson;
    private View view;

    final String TAG = "LearnFragmentPresenter";

    ArrayList<Lesson> lessonsList = new ArrayList();
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
                    if (!lessonsList.isEmpty()) {
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

        //TODO : order lessons by position
        fireStoreLessons.document(currentSubject).collection(currentSubject).whereEqualTo("reviewed", true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    view.onDataFound();

                    lesson = new Lesson();
                    String title = document.getString("title");
                    String content = document.getString("content");
                    String position = document.getString("position");
                    String lessonId = document.getId();
                    lesson.setLessonId(lessonId);
                    lesson.setTitle(title);
                    lesson.setContent(content);
                    lesson.setPosition(position);
                    if (!existsInLessonsList(lessonId)) {
                        lessonsList.add(lesson);
                        view.notifyAdapter();
                    }
                }
                view.hideProgressBar();
                if (lessonsList.size() == 0) {
                    view.showNoLessonsTextView();
                }
                }
        });
    }

    private boolean existsInLessonsList(String lessonId) {
        for (Lesson lesson : lessonsList) {
            if (lesson.getLessonId().equals(lessonId)) {
                return true;
            }
        }
        return false;
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
