package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.model.Question;
import com.mk.playAndLearn.utils.DateClass;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mk.playAndLearn.utils.Firebase.fireStoreComplaintsQuestions;

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
            }
        }

        if (correct) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.question_result_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reportQuestion:
                reported = true;
                showDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(this)
                .setTitle("تقديم شكوى")
                .setMessage("هل أنت متأكد أنك تريد تقديم شكوى فى هذا السؤال؟")
                .setCancelable(false)
                .setPositiveButton("لا", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        navigate();
                    }
                })
                .setNegativeButton("نعم", null)
                .create();

        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Date today = new Date();
                final DateClass dateClass = new DateClass();
                dateClass.setDate(today);

                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String localCurrentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        SharedPreferences pref = getSharedPreferences("MyPref", 0);
                        String grade = pref.getString("grade", "غير معروف");
                        Map<String, Object> map = new HashMap<>();
                        map.put("ComplainantEmail", localCurrentEmail);
                        map.put("complaintResolved", false);
                        map.put("subject", subject);
                        map.put("grade", grade);
                        map.put("date", dateClass.getDate());
                        map.put("questionId", ((ArrayList<Question>)list).get(questionNo).getQuestionId());
                        fireStoreComplaintsQuestions.add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                Toast.makeText(QuestionResultActivity.this, "تم إستقبال شكوتك وسيتم مراجعة السؤال", Toast.LENGTH_SHORT).show();
                                navigate();
                            }
                        });
                    }
                });
            }
        });

        alertDialogBuilderUserInput.show();
    }
}
