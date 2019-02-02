package com.mk.playAndLearn.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.playAndLearn.activity.ChallengeStartActivity;
import com.mk.playAndLearn.activity.QuestionActivity;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.AdManager;

import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.fireStoreQuestions;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.sharedPreference.getGradeFromSharedPreferences;
import static com.mk.playAndLearn.utils.sharedPreference.getNameFromSharedPreferences;

public class ChallengeStartActivityPresenter {
    View view;
    Context context;

    Intent i;

    ArrayList list = new ArrayList<>();

    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    String subject, challengeId;
    int firstPlayerPoints = -1, currentChallenger = 1;
    long secondPlayerPoints = -1;

    String challengeQuestionsIds, grade;
    String playerAnswersBooleansList = "", playerAnswersList = "", correctAnswersList = "";

    public ChallengeStartActivityPresenter(View view) {
        this.view = view;
        this.context = (Context) view;
    }

    public void prepareAd() {
        AdManager adManager = AdManager.getInstance();
        adManager.createAd(context);
    }

    public void getCurrentPlayerData() {
        final String player1Name = getNameFromSharedPreferences(context);
        final String player1Image = currentUser.getPhotoUrl().toString();
        usersReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int player1Points = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
                view.showCurrentPlayerData(player1Name, player1Image, player1Points);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getOpponentData(Intent intent) {
        int currentChallenger = -1, opponentPoints = -1;
        String opponentName = "", opponentImage = "", opponentUid = "";

        if (intent.getExtras() != null) {
            currentChallenger = intent.getIntExtra("currentChallenger", 1);
            opponentUid = intent.getStringExtra("uid");

            if (currentChallenger == 1) {
                opponentName = intent.getStringExtra("name");
                opponentImage = intent.getStringExtra("image");
                opponentPoints = intent.getIntExtra("points", -1);
            }
        }

        if (currentChallenger == 1) {
            view.showOpponentData(opponentName, opponentImage, opponentPoints);
        } else if (currentChallenger == 2) {
            usersReference.child(opponentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String opponentName = (String) dataSnapshot.child("userName").getValue();
                    String opponentImage = (String) dataSnapshot.child("userImage").getValue();
                    int opponentPoints = -1;

                    if (dataSnapshot.child("points").getValue() != null)
                        opponentPoints = (int) dataSnapshot.child("points").getValue();

                    view.showOpponentData(opponentName, opponentImage, opponentPoints);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void setQuestionsListAndNavigate(Intent intent){

        if (intent.getExtras() != null) {
            currentChallenger = intent.getIntExtra("currentChallenger", 1);
            subject = intent.getStringExtra("subject");
            secondPlayerUid = intent.getStringExtra("uid");

            if (currentChallenger == 2) {
                challengeId = intent.getStringExtra("challengeId");
                challengeQuestionsIds = intent.getStringExtra("questionsList");
                grade = intent.getStringExtra("grade");
            } else {
                secondPlayerName = intent.getStringExtra("name");
                secondPlayerEmail = intent.getStringExtra("email");
                secondPlayerImage = intent.getStringExtra("image");
                secondPlayerPoints = intent.getIntExtra("points", -1);
            }
        }

        i = new Intent(context, QuestionActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

        i.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
        i.putExtra("currentPlayerAnswers", playerAnswersList);
        i.putExtra("correctAnswers", correctAnswersList);
        i.putExtra("questionNo", 0);
        i.putExtra("score", 0);
        i.putExtra("subject", subject);
        i.putExtra("currentChallenger", currentChallenger);
        i.putExtra("isGeneralChallenge", false);
        i.putParcelableArrayListExtra("questionList", list);


        if (currentChallenger == 1) {
            i.putExtra("player2Name", secondPlayerName);
            i.putExtra("player2Email", secondPlayerEmail);
            i.putExtra("player2Image", secondPlayerImage);
            i.putExtra("player2Points", secondPlayerPoints);
            i.putExtra("player2Uid", secondPlayerUid);
        } else {
            i.putExtra("challengeId", challengeId);
        }

        if (currentChallenger == 1) {
            view.showProgressBar();
            clearList();
            loadQuestionsForChallenger1();
        } else if (currentChallenger == 2) {
            view.showProgressBar();
            clearList();
            setQuestionListFromIdsForPlayer2();
        }
    }

    void clearList(){
        //TO clear the listBefore start filling data by recursion
        if (!list.isEmpty())
            list.clear();
    }



    void setQuestionListFromIdsForPlayer2(){
        //setQuestionsList
        String[] questionsIds = challengeQuestionsIds.split(" ");
        final int listSize = questionsIds.length;
        for (String questionId : questionsIds) {
            fireStoreQuestions.document(grade).collection(subject).document(questionId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    addQuestionData(documentSnapshot);
                    if (list.size() == listSize) {
                        navigate();
                    }
                }
            });
        }
    }


    void loadQuestionsForChallenger1() {
        String randomId = fireStoreQuestions.document().getId();

        fireStoreQuestions.document(getGradeFromSharedPreferences(context)).collection(subject)
                .whereEqualTo("reviewed", true)
                .whereGreaterThan(FieldPath.documentId(), randomId)
                .limit(1)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                    addQuestionData(document);
                    if (list.size() < 5) {
                        loadQuestionsForChallenger1();
                    } else {
                        navigate();
                    }
                }
            }
        });
    }



    public void addQuestionData(DocumentSnapshot document) {
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

        if (list.size() < 5 && !existsInList(questionText))
            list.add(question);
    }

    boolean existsInList(String questionText) {
        ArrayList<Question> localList = list;
        for (Question q : localList) {
            if (q.getAlQuestion().equals(questionText)) {
                return true;
            }
        }
        return false;
    }

    public void navigate(){
        context.startActivity(i);
        ((Activity)context).finish();
    }

    public interface View {
        void showCurrentPlayerData(String currentPlayerName, String currentPlayerImage, int currentPlayerPoints);

        void showOpponentData(String opponentName, String opponentImage, int opponentPoints);

        void showProgressBar();
    }
}
