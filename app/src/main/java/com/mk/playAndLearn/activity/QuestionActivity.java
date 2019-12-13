package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;

public class QuestionActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "", correctAnswersList = "";
    TextView tvQuestion;
    RadioGroup rg1;
    LinearLayout checkBoxGroup;
    Button nextButton;
    String selection, correctAnswer;
    RadioButton r1, r2, r3, r4;
    CheckBox c1, c2, c3, c4;
    TextView r1Txt, r2Txt, r3Txt, r4Txt, c1Txt, c2Txt, c3Txt, c4Txt;
    Intent i;
    int questionNo, score, currentChallenger;
    boolean isGeneralChallenge = true;
    CountDownTimer timer;
    String subject, challengeId;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    String[] correctAnswers;
    int secondPlayerPoints;
    ProgressBar timerProgressBar;
    FirebaseUser currentUser;

    RadioButton radioButtonone, radiontwo, radiothree, radiofour;
    RelativeLayout r1View, r2View, r3View, r4View, c1View, c2View, c3View, c4View;

    //TODO : change the xml tags to support
    //TODO : handle what happens when internet connection problem occurs in a challenge
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        radioButtonone = findViewById(R.id.radio1);
        radiontwo = findViewById(R.id.radio2);
        radiothree = findViewById(R.id.radio3);
        radiofour = findViewById(R.id.radio4);
       /* final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);*/
        i = new Intent(QuestionActivity.this, QuestionResultActivity.class);

        rg1 = findViewById(R.id.radioGroup);
        nextButton = findViewById(R.id.skipQuestionButton);
        tvQuestion = findViewById(R.id.questionText);
        r1 = findViewById(R.id.radio1);
        r2 = findViewById(R.id.radio2);
        r3 = findViewById(R.id.radio3);
        r4 = findViewById(R.id.radio4);
        c1 = findViewById(R.id.checkBox1);
        c2 = findViewById(R.id.checkBox2);
        c3 = findViewById(R.id.checkBox3);
        c4 = findViewById(R.id.checkBox4);
        r1Txt = findViewById(R.id.radio1Text);
        r2Txt = findViewById(R.id.radio2Text);
        r3Txt = findViewById(R.id.radio3Text);
        r4Txt = findViewById(R.id.radio4Text);
        c1Txt = findViewById(R.id.checkBox1Text);
        c2Txt = findViewById(R.id.checkBox2Text);
        c3Txt = findViewById(R.id.checkBox3Text);
        c4Txt = findViewById(R.id.checkBox4Text);
        r1View = findViewById(R.id.r1View);
        r2View = findViewById(R.id.r2View);
        r3View = findViewById(R.id.r3View);
        r4View = findViewById(R.id.r4View);
        c1View = findViewById(R.id.c1View);
        c2View = findViewById(R.id.c2View);
        c3View = findViewById(R.id.c3View);
        c4View = findViewById(R.id.c4View);
        checkBoxGroup = findViewById(R.id.checkBoxGroup);
        timerProgressBar = findViewById(R.id.timerProgressbar);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            list = intent.getParcelableArrayListExtra("questionList");
            questionNo = intent.getIntExtra("questionNo", -1);
            score = intent.getIntExtra("score", -1);
            isGeneralChallenge = intent.getBooleanExtra("isGeneralChallenge", false);

            if (!isGeneralChallenge) {
                currentChallenger = intent.getIntExtra("currentChallenger", -1);
                subject = intent.getStringExtra("subject");
                playerAnswersBooleansList = intent.getStringExtra("currentPlayerAnswersBooleans");
                playerAnswersList = intent.getStringExtra("currentPlayerAnswers");
                correctAnswersList = intent.getStringExtra("correctAnswers");

                if (currentChallenger == 1) {
                    secondPlayerName = intent.getStringExtra("player2Name");
                    secondPlayerEmail = intent.getStringExtra("player2Email");
                    secondPlayerImage = intent.getStringExtra("player2Image");
                    secondPlayerUid = intent.getStringExtra("player2Uid");
                    secondPlayerPoints = intent.getIntExtra("player2Points", -1);
                } else {
                    challengeId = intent.getStringExtra("challengeId");
                    secondPlayerName = intent.getStringExtra("player2Name");
                    secondPlayerImage = intent.getStringExtra("player2Image");
                }
            }
        }

        hideRadioGroup();
        hideCheckBoxesGroup();

        r1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r1.setChecked(true);
                r2.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(false);
            }
        });

        r2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r1.setChecked(false);
                r2.setChecked(true);
                r3.setChecked(false);
                r4.setChecked(false);            }
        });

        r3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(true);
                r4.setChecked(false);
            }
        });

        r4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r1.setChecked(false);
                r2.setChecked(false);
                r3.setChecked(false);
                r4.setChecked(true);
            }
        });

        c1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(c1.isChecked()){
                    c1.setChecked(false);
                } else {
                    c1.setChecked(true);
                }
            }
        });

        c2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(c2.isChecked()){
                    c2.setChecked(false);
                } else {
                    c2.setChecked(true);
                }
            }
        });

        c3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(c3.isChecked()){
                    c3.setChecked(false);
                } else {
                    c3.setChecked(true);
                }
            }
        });

        c4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(c4.isChecked()){
                    c4.setChecked(false);
                } else {
                    c4.setChecked(true);
                }
            }
        });

        Question question = (Question) list.get(questionNo);
        correctAnswer = question.getCorrectAnswer();

        tvQuestion.setText(question.getAlQuestion().trim());

        correctAnswers = correctAnswer.split(",");

        //store answers in array
        ArrayList<String> answers = new ArrayList<>();
        answers.add(question.getAnswer1());
        answers.add(question.getAnswer2());

        //To shuffle Answers but we have disabled this feature
        // Collections.shuffle(answers);

        if (question.getAnswer3().length() > 0)
            answers.add(question.getAnswer3());

        if (question.getAnswer4().length() > 0)
            answers.add(question.getAnswer4());//because some question there last option is things like "all above"

        if (correctAnswers.length == 1) {
            showRadioGroup();
            hideCheckBoxesGroup();
            setAnswersData(r1Txt, r2Txt, r3Txt, r4Txt, answers);
        } else if (correctAnswers.length > 1) {
            showCheckBoxesGroup();
            hideRadioGroup();
            setAnswersData(c1Txt, c2Txt, c3Txt, c4Txt, answers);
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate();
            }
        });

        final int millisInFuture, countDownInterval, totalSeconds;

        if (isGeneralChallenge) {
            millisInFuture = 300000;//5 minutes
            countDownInterval = 3000;//3 seconds
            totalSeconds = millisInFuture / 1000;
        } else {
            millisInFuture = 21000;
            countDownInterval = 100;
            totalSeconds = millisInFuture / 1000;
        }

        // TODO : solve timer running in the end problem
        timer = new CountDownTimer(millisInFuture, countDownInterval) {

            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) millisUntilFinished / 1000;
                int percentage = (int) (((double) secondsLeft / (double) totalSeconds) * 100.0);
                //Toast.makeText(QuestionActivity.this, "percentage is : " + percentage, Toast.LENGTH_SHORT).show();
                timerProgressBar.setProgress(percentage);
            }

            public void onFinish() {
                timerProgressBar.setProgress(0);
                navigate();
            }

        };
        timer.start();
    }

    void hideRadioGroup() {
        rg1.setVisibility(View.GONE);
    }

    void showRadioGroup() {
        rg1.setVisibility(View.VISIBLE);
    }

    void hideCheckBoxesGroup() {
        checkBoxGroup.setVisibility(View.GONE);
    }

    void showCheckBoxesGroup() {
        checkBoxGroup.setVisibility(View.VISIBLE);
    }

    void setAnswersData(TextView v1, TextView v2, TextView v3, TextView v4, ArrayList<String> answers) {
        Log.v("answersLogging", "size is : " + answers.size());

        v1.setText(answers.get(0).trim());
        v2.setText(answers.get(1).trim());
        if (answers.size() > 2) {
            v3.setText(answers.get(2).trim());
        } else {
            v3.setVisibility(View.GONE);
            r3.setVisibility(View.GONE);
            c3.setVisibility(View.GONE);
        }
        if (answers.size() > 3) {
            v4.setText(answers.get(3).trim());
        } else {
            v4.setVisibility(View.GONE);
            r4.setVisibility(View.GONE);
            c4.setVisibility(View.GONE);
        }
    }

   /* void setAnswersData(TextView v1, TextView v2, TextView v3, TextView v4, ArrayList<String> answers) {
        Log.v("answersLogging","size is : " + answers.size());

        v1.setText(answers.get(0).trim());
        v2.setText(answers.get(1).trim());
        if (answers.size() > 2) {
            v3.setText(answers.get(2).trim());
        } else {
            v3.setVisibility(View.GONE);
        }
        if (answers.size() > 3) {
            v4.setText(answers.get(3).trim());
        } else {
            v4.setVisibility(View.GONE);
        }
    }*/

    @Override
    public void onBackPressed() {

    }

    public void navigate() {
        if (r1.isChecked()) {
            selection = r1Txt.getText().toString();
        } else if (r2.isChecked()) {
            selection = r2Txt.getText().toString();
        } else if (r3.isChecked()) {
            selection = r3Txt.getText().toString();
        } else if (r4.isChecked()) {
            selection = r4Txt.getText().toString();
        }

        String[] userAnswers = getUserAnswers(correctAnswers.length).split(",");

        if (correctAnswers.length == 1) {
            if (selection != null && selection.trim().equals(correctAnswer.trim())) {
                i.putExtra("answer", true);
            } else {
                i.putExtra("answer", false);
            }
            Log.v("newQuestionLog", "selected : " + selection + " , correct : " + correctAnswer);
        } else if (correctAnswers.length > 1) {

            Arrays.sort(userAnswers);
            Arrays.sort(correctAnswers);

            if (Arrays.equals(userAnswers, correctAnswers)) {
                i.putExtra("answer", true);
            } else {
                i.putExtra("answer", false);
            }
            Log.v("newQuestionLog", "selected : " + userAnswers + " , correct : " + correctAnswer);

        }


        if (questionNo < list.size()) {//TODO : check this condition
            playerAnswersList += getUserAnswers(correctAnswers.length).trim() + "/";
            correctAnswersList += correctAnswer.trim() + "/";
        } else {
            playerAnswersList += getUserAnswers(correctAnswers.length).trim();
            correctAnswersList += correctAnswer.trim();
        }

        i.putExtra("questionNo", questionNo);
        i.putExtra("score", score);
        i.putParcelableArrayListExtra("questionList", list);
        i.putExtra("isGeneralChallenge", isGeneralChallenge);

        i.putExtra("player2Name", secondPlayerName);
        i.putExtra("player2Image", secondPlayerImage);

        if (!isGeneralChallenge) {
            i.putExtra("isGeneralChallenge", false);
            i.putExtra("subject", subject);
            i.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
            i.putExtra("currentPlayerAnswers", playerAnswersList);
            i.putExtra("correctAnswers", correctAnswersList);
            i.putExtra("currentChallenger", currentChallenger);
            if (currentChallenger == 1) {
                i.putExtra("player2Email", secondPlayerEmail);
                i.putExtra("player2Uid", secondPlayerUid);
                i.putExtra("player2Points", secondPlayerPoints);
            } else if (currentChallenger == 2) {
                i.putExtra("challengeId", challengeId);
            }
        } else {
            i.putExtra("isGeneralChallenge", true);
        }
        timer.cancel();
        startActivity(i);
        finish();
    }

    private String getUserAnswers(int length) {
        String answers = "";
        if (length == 1) {
            if (r1.isChecked()) {
                answers += addAnswer((String) r1Txt.getText(), answers.length());
            }
            if (r2.isChecked()) {
                answers += addAnswer((String) r2Txt.getText(), answers.length());
            }
            if (r3.isChecked()) {
                answers += addAnswer((String) r3Txt.getText(), answers.length());
            }
            if (r4.isChecked()) {
                answers += addAnswer((String) r4Txt.getText(), answers.length());
            }
        } else if (length > 1) {
            if (c1.isChecked()) {
                answers += addAnswer((String) c1Txt.getText(), answers.length());
            }
            if (c2.isChecked()) {
                answers += addAnswer((String) c2Txt.getText(), answers.length());
            }
            if (c3.isChecked()) {
                answers += addAnswer((String) c3Txt.getText(), answers.length());
            }
            if (c4.isChecked()) {
                answers += addAnswer((String) c4Txt.getText(), answers.length());
            }
        }

        return answers;
    }


    public String addAnswer(String answer, int length) {
        if (length == 0) {
            return answer;
        } else {
            return "," + answer;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //timer.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isGeneralChallenge) {
          //  timer.cancel();
        }
    }

    public void finishch(View view) {
        finish();
    }


    public void radioone(View view) {


        switch (view.getId()) {


            case R.id.radio1:
                radioButtonone.setChecked(true);

                radiontwo.setChecked(false);
                radiothree.setChecked(false);

                radiofour.setChecked(false);

                break;


            case R.id.radio2:
                radioButtonone.setChecked(false);

                radiontwo.setChecked(true);
                radiothree.setChecked(false);

                radiofour.setChecked(false);

                break;

            case R.id.radio3:
                radioButtonone.setChecked(false);

                radiontwo.setChecked(false);
                radiothree.setChecked(true);

                radiofour.setChecked(false);

                break;

            case R.id.radio4:
                radioButtonone.setChecked(false);

                radiontwo.setChecked(false);
                radiothree.setChecked(false);

                radiofour.setChecked(true);

                break;


        }


    }
}
