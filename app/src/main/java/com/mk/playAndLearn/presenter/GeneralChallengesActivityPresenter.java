package com.mk.playAndLearn.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.activity.QuestionActivity;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.AdManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.generalChallengeDocument;
import static com.mk.playAndLearn.utils.Strings.adminEmail;

public class GeneralChallengesActivityPresenter {
    View view;
    Context context;

    ValueEventListener generalChallengeListener;
    ArrayList list = new ArrayList<>();

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    public GeneralChallengesActivityPresenter(View view) {
        this.view = view;
        this.context = ((Context) view);
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
                    loadChallengeState();
                } else {
                    view.onNoInternetConnection();
                }
            }
        };

        asyncTask.execute();
    }

    public void loadChallengeState() {
        generalChallengeDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("Logging", "documentSnapShot is : " + documentSnapshot.getData().toString());
                String challengeText = documentSnapshot.getString("text");
                boolean activeNow = documentSnapshot.getBoolean("activeNow");

                if (activeNow || currentUser.getEmail().equals(adminEmail)) {
                    view.showButtonGroup();
                    view.hideChallengeText();
                } else {
                    view.hideButtonGroup();
                }

                if (currentUser.getEmail().equals(adminEmail)) {
                    if (activeNow) {
                        Toast.makeText(context, "التحدى نشط الان", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "التحدى غير نشط الان", Toast.LENGTH_SHORT).show();
                    }
                }

                view.setChallengeText(challengeText);
                view.hideProgressBar();
            }
        });
    }

    public void loadQuestions(final String schoolType) {
        if (!list.isEmpty())
            list.clear();

        String usedCollection = "";
        if (schoolType.equals("arabic"))
            usedCollection = "arabicQuestions";
        if (schoolType.equals("languages"))
            usedCollection = "languageQuestions";

        generalChallengeDocument.collection(usedCollection).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                    Question question = new Question();
                    String questionText = document.getString("alQuestion");
                    String answer1 = document.getString("answer1");
                    String answer2 = document.getString("answer2");
                    String answer3 = document.getString("answer3");
                    String answer4 = document.getString("answer4");
                    String correctAnswer = document.getString("correctAnswer");
                    String writerName = document.getString("writerName");
                    boolean reviewed = document.getBoolean("reviewed");

                    question.setAnswer1(answer1);
                    question.setAnswer2(answer2);
                    question.setAnswer3(answer3);
                    question.setAnswer4(answer4);
                    question.setCorrectAnswer(correctAnswer);
                    question.setWriterName(writerName);
                    question.setAlQuestion(questionText);
                    question.setReviewed(reviewed);
                    question.setQuestionId(document.getId());

                    list.add(0, question);
                }

                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putParcelableArrayListExtra("questionList", list);
                intent.putExtra("questionNo", 0);
                intent.putExtra("score", 0);
                intent.putExtra("isGeneralChallenge", true);
                context.startActivity(intent);
                view.finishActivity();
                view.hideHorizontalProgressBar();
            }
        });
    }


    public void prepareAd() {
        AdManager adManager = AdManager.getInstance();
        adManager.createInterstitialAd(context);
    }


    public interface View {
        void hideProgressBar();

        void showHorizontalProgressBar();

        void hideHorizontalProgressBar();

        void setChallengeText(String text);

        void hideChallengeText();

        void onNoInternetConnection();

        void retryConnection();

        void hideButtonGroup();

        void showButtonGroup();

        void finishActivity();
    }
}
