package com.mk.playAndLearn.presenter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
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

import static com.mk.playAndLearn.utils.Firebase.fireStoreComments;
import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;
import static com.mk.playAndLearn.utils.Firebase.fireStorePosts;

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

    public void getLessons(final String currentSubject) {
        view.startRecyclerAdapter(lessonsList);
        view.onLoadingData();
        if (!lessonsList.isEmpty()) {
            lessonsList.clear();
            view.notifyAdapter();
        }

        EventListener lessonsSnapshotListener = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG, listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    DocumentSnapshot lessonDocument = dc.getDocument();
                    switch (dc.getType()) {
                        case ADDED:
                            getLessonData(lessonDocument);
                            break;
                        case MODIFIED:
                            Log.d("TAG", "Modified city: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            startAsynkTask(currentSubject);//TODO : think about removeing the child from the list only                            break;
                    }
                }

                if (lessonsList.size() == 0) {
                    view.showNoLessonsTextView();
                    view.hideProgressBar();
                } else {
                    view.onDataFound();
                }
            }
        };

        if(currentSubject.equals("كل المواد"))
            fireStoreLessons.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(lessonsSnapshotListener);
        else
            fireStoreLessons.whereEqualTo("subject", currentSubject).orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(lessonsSnapshotListener);
    }

    private boolean existsInLessonsList(String lessonId) {
        for (Lesson lesson : lessonsList) {
            if (lesson.getLessonId().equals(lessonId)) {
                return true;
            }
        }
        return false;
    }

    private void getLessonData(DocumentSnapshot lessonDocument) {
        lesson = new Lesson();
        String title = lessonDocument.getString("title");
        String content = lessonDocument.getString("content");
        String subject = lessonDocument.getString("subject");
        String writerName = lessonDocument.getString("writerName");
        String writerEmail = lessonDocument.getString("writerEmail");
        String writerUid = lessonDocument.getString("writerUid");
        String position = lessonDocument.getString("position");
        String lessonId = lessonDocument.getId();

        lesson.setLessonId(lessonId);
        lesson.setTitle(title);
        lesson.setContent(content);
        lesson.setSubject(subject);
        lesson.setPosition(position);
        lesson.setWriterName(writerName);
        lesson.setWriterEmail(writerEmail);
        lesson.setWriterUid(writerUid);

        if (!existsInLessonsList(lessonId)) {
            lessonsList.add(lesson);
            view.notifyAdapter();
        }
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
