package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.AbsListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.activity.MainActivity;
import com.mk.playAndLearn.model.Lesson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.fireStoreLessons;

public class LessonsFragmentPresenter {
    Lesson lesson;
    View view;
    Context context;
    DocumentSnapshot lastVisible;
    int lastPosition;
    Map<String, Object> map;
    ArrayList<Lesson> lessonsList = new ArrayList();
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);

    SharedPreferences pref;
    String currentUserName;
    String grade;
    int limit = 5;
    Query firstQuery;
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;

    public LessonsFragmentPresenter(View view, Context context) {
        this.view = view;
        this.context = context;
        pref = context.getSharedPreferences("MyPref", 0); // 0 - for private mode //TODO : check thisg
        currentUserName = pref.getString("currentUserName", "غير معروف");
        grade = pref.getString("grade", "غير معروف");

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
                    getLessons(currentSubject);
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void getLessons(final String currentSubject) {
        lessonsList = new ArrayList<>();
        view.startRecyclerAdapter(lessonsList);
        view.showProgressBar();
        view.hideNoLessonsText();
        view.notifyAdapter();

        OnCompleteListener lessonsListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.v("lessonsLogging", "task is successful");
                    for (DocumentSnapshot document : task.getResult()) {
                        getLessonData(document);
                    }

                    ((MainActivity) context).updateLastOnlineDateAndShowRewardsPage();

                    if (lessonsList.size() == 0) {
                        view.onNoLessonsExists();
                    } else {
                        view.onDataFound();
                    }

                    if (task.getResult().size() > 0) {
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        lastPosition = task.getResult().size() - 1;
                    }


                    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true;
                            }
                        }

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = linearLayoutManager.getChildCount();
                            int totalItemCount = linearLayoutManager.getItemCount();

                            if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                isScrolling = false;

                                OnCompleteListener secondQueryCompleteListener = new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                        if (t.isSuccessful()) {
                                            for (DocumentSnapshot d : t.getResult()) {
                                                getLessonData(d);
                                            }
                                            view.notifyAdapter();

                                            if (t.getResult().size() > 0) {
                                                lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                                lastPosition += 5;
                                            }

                                            Log.v("LoggingPosition", "last position is : " + lastPosition);

                                            if (t.getResult().size() < limit) {
                                                isLastItemReached = true;
                                            }
                                        }
                                    }
                                };


                                if (currentSubject.equals("كل المواد")) {
                                    Query nextQuery = fireStoreLessons.whereEqualTo("grade", grade).orderBy("date", Query.Direction.ASCENDING)
                                            .startAfter(lastVisible).limit(limit);
                                    nextQuery.get().addOnCompleteListener(secondQueryCompleteListener);
                                } else {
                                    Query nextQuery = fireStoreLessons.whereEqualTo("subject", currentSubject).whereEqualTo("grade", grade)
                                            .orderBy("date", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
                                    nextQuery.get().addOnCompleteListener(secondQueryCompleteListener);
                                }
                            }
                        }
                    };
                    view.setOnScrollListener(onScrollListener);


                } else {
                    Log.v("TAG", "failed");
                    Log.v("lessonsLogging", "task failed , "
                            + "exception is : " + task.getException().toString());
                }
            }
        };

        if (currentSubject.equals("كل المواد")) {
            firstQuery = fireStoreLessons.whereEqualTo("grade", grade).orderBy("date", Query.Direction.DESCENDING).limit(limit);
            firstQuery.get().addOnCompleteListener(lessonsListener);
        } else {
            firstQuery = fireStoreLessons.whereEqualTo("subject", currentSubject).whereEqualTo("grade", grade)
                    .orderBy("date", Query.Direction.DESCENDING).limit(limit);
            firstQuery.get().addOnCompleteListener(lessonsListener);
        }
    }

    private boolean existsInLessonsList(String lessonId) {
        for (Lesson lesson : lessonsList) {
            if (lesson.getId().equals(lessonId)) {
                return true;
            }
        }
        return false;
    }

    void getLessonData(DocumentSnapshot lessonDocument) {
        Log.v("lessonsLogging", "get LessonData is called");

        format.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String lessonDate;

        lesson = new Lesson();
        String lessonContent = lessonDocument.getString("content");
        String lessonTitle = lessonDocument.getString("title");
        lessonDate = format.format(lessonDocument.get("date"));
        String lessonWriter = lessonDocument.getString("writerName");
        String lessonWriterEmail = lessonDocument.getString("writerEmail");
        String lessonId = lessonDocument.getId();
        String writerUid = lessonDocument.getString("writerUid");

        if (writerUid != null)
            lesson.setWriterUid(writerUid);
        if (lessonWriterEmail != null)
            lesson.setWriterEmail(lessonWriterEmail);
        if (lessonContent != null)
            lesson.setContent(lessonContent);
        if (lessonDate != null)
            lesson.setDate(lessonDate);
        if (lessonWriter != null)
            lesson.setWriterName(lessonWriter);
        if (lessonTitle != null)
            lesson.setTitle(lessonTitle);
        if (lessonId != null)
            lesson.setId(lessonId);


        if (!existsInLessonsList(lessonId)) {
            lessonsList.add(lesson);
            view.notifyAdapter();
        }
    }

    public interface View {
        void retryConnection();

        void startRecyclerAdapter(ArrayList lessonsList);

        void onNoInternetConnection();

        void showToast(String value);

        void notifyAdapter();

        void onDataFound();

        void hideProgressBar();

        void hideNoLessonsText();

        void showProgressBar();

        void onNoLessonsExists();

        void setOnScrollListener(RecyclerView.OnScrollListener onScrollListener);

    }
}
