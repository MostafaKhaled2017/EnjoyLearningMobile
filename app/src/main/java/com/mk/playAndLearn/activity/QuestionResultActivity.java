package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.DateClass;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mk.playAndLearn.utils.Firebase.fireStoreComplaintsQuestions;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedGrade;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedImage;
import static com.mk.playAndLearn.utils.sharedPreference.getSavedName;

public class QuestionResultActivity extends AppCompatActivity {
    TextView resultText;
    boolean correct;
    ArrayList list = new ArrayList();
    String playerAnswersBooleansList = "", playerAnswersList = "", correctAnswersList = "";
    int questionNo, score;
    String subject, challengeId;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int secondPlayerPoints, currentChallenger, sleepTime = 1000;
    boolean isGeneralChallenge, reported = false;

    ImageView player1ImageIv, player2Image;
    TextView player1NameTv, player1PointsTv, player2Name, player2Points, textComplaintTv;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_question_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);

        resultText = findViewById(R.id.resultText);

        player1NameTv = findViewById(R.id.firstPlayerName);
        player1ImageIv = findViewById(R.id.firstPlayerImage);
        player2Name = findViewById(R.id.secondPlayerName);
     //   player2Image = findViewById(R.id.secondPlayerImage);

        textComplaintTv = findViewById(R.id.textcomplaint);

        textComplaintTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            correct = intent.getBooleanExtra("answer", false);
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
                } else if (currentChallenger == 2) {
                    challengeId = intent.getStringExtra("challengeId");
                }

                player1NameTv.setText(getSavedName(this));
                Picasso.with(this).load(getSavedImage(this)).placeholder(R.drawable.picasso_placeholder).into(player1ImageIv);

                player2Name.setText(secondPlayerName);
                Picasso.with(this).load(secondPlayerImage).placeholder(R.drawable.picasso_placeholder).into(player2Image);

            }
        }

        if (correct){
            resultText.setText("إجابة صحيحة");
            resultText.setTextColor(Color.GREEN);
            playerAnswersBooleansList += true + " ";//TODO
            score++;
        } else {
            resultText.setText("إجابة خاطئة");
            resultText.setTextColor(Color.RED);
            playerAnswersBooleansList += false + " ";
        }

        i = new Intent(this, QuestionActivity.class);


        if (questionNo + 1 < list.size()) {
            Thread timer = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if(!reported) {
                            navigate();
                        }
                    }
                }
            };
            timer.start();
        } else {
            Thread timer = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent1 = new Intent(QuestionResultActivity.this, ChallengeResultActivity.class);
                        intent1.putExtra("score", score);
                        intent1.putExtra("isGeneralChallenge", isGeneralChallenge);

                        if (!isGeneralChallenge) {
                            intent1.putExtra("currentChallenger", currentChallenger);
                            intent1.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
                            intent1.putExtra("currentPlayerAnswers", playerAnswersList);
                            intent1.putExtra("correctAnswers", correctAnswersList);
                            intent1.putExtra("subject", subject);

                            if (currentChallenger == 1) {
                                intent1.putExtra("player2Name", secondPlayerName);
                                intent1.putExtra("player2Email", secondPlayerEmail);
                                intent1.putExtra("player2Image", secondPlayerImage);
                                intent1.putExtra("player2Uid", secondPlayerUid);
                                intent1.putExtra("player2Points", secondPlayerPoints);
                                intent1.putParcelableArrayListExtra("questionsList", list);
                            } else if (currentChallenger == 2) {
                                intent1.putExtra("challengeId", challengeId);
                            }
                        }
                        startActivity(intent1);
                        finish();
                    }
                }
            };
            timer.start();
        }
    }

    void navigate(){
        i.putExtra("questionList", list);
        i.putExtra("questionNo", questionNo + 1);
        i.putExtra("score", score);
        i.putExtra("isGeneralChallenge", isGeneralChallenge);

        if (!isGeneralChallenge) {
            i.putExtra("subject", subject);
            i.putExtra("currentPlayerAnswersBooleans", playerAnswersBooleansList);
            i.putExtra("currentPlayerAnswers", playerAnswersList);
            i.putExtra("correctAnswers", correctAnswersList);
            i.putExtra("currentChallenger", currentChallenger);

            if (currentChallenger == 1) {
                i.putExtra("player2Name", secondPlayerName);
                i.putExtra("player2Email", secondPlayerEmail);
                i.putExtra("player2Image", secondPlayerImage);
                i.putExtra("player2Uid", secondPlayerUid);
                i.putExtra("player2Points", secondPlayerPoints);
            } else if (currentChallenger == 2) {
                i.putExtra("challengeId", challengeId);
            }
        }

        startActivity(i);
        finish();
    }
    void showDialog(){
        reported = true;

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);//TODO : check this
        final android.view.View view = layoutInflaterAndroid.inflate(R.layout.alertdialog, null);

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Date today = new Date();
                final DateClass dateClass = new DateClass();
                dateClass.setDate(today);

                Button Confirmbutton = view.findViewById(R.id.yes);
                Confirmbutton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String localCurrentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        SharedPreferences pref = getSharedPreferences("MyPref", 0);
                        String questionId = ((ArrayList<Question>)list).get(questionNo).getQuestionId();
                        String grade = getSavedGrade(QuestionResultActivity.this);
                        Map<String, Object> map = new HashMap<>();
                        map.put("ComplainantEmail", localCurrentEmail);
                        map.put("complaintResolved", false);
                        map.put("subject", subject);
                        map.put("grade", grade);
                        map.put("date", dateClass.getDate());
                        map.put("questionId", questionId);
                        fireStoreComplaintsQuestions.document(questionId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(QuestionResultActivity.this, "تم إستقبال شكوتك وسيتم مراجعة السؤال", Toast.LENGTH_SHORT).show();
                                    navigate();
                                } else {
                                    Toast.makeText(QuestionResultActivity.this, "لم يتم إرسال الشكوى", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        Button dismissBtn = view.findViewById(R.id.no);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogBuilderUserInput.dismiss();
                navigate();
            }
        });

        alertDialogBuilderUserInput.show();
    }
}
