package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.mk.playAndLearn.utils.Firebase.challengesReference;
import static com.mk.playAndLearn.utils.Firebase.usersReference;

public class ChallengesAndUsersMonitoring extends AppCompatActivity {

    TextView studentsNumberTv, allChallengesNumber, todayChallengesNumberTv, yesterdayChallengesNumberTv;

    ListView todayAdminChallengesListView;
    int studentsCount = 0, allChallengesCount = 0, todayChallengesCount = 0, yesterdayChallengesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges_and_users_monitoring);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("بيانات التحديات والمستخدمين");

        studentsNumberTv = findViewById(R.id.studentsNumberTv);
        allChallengesNumber = findViewById(R.id.allChallengesNumberTv);
        todayChallengesNumberTv = findViewById(R.id.todayChallengesNumberTv);
        yesterdayChallengesNumberTv = findViewById(R.id.yesterdayChallengesNumberTv);
        todayAdminChallengesListView = findViewById(R.id.todayAdminChallengesListView);

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String userType = dataSnapshot1.child("userType").getValue().toString();
                    if (userType.equals("طالب")) {
                        studentsCount++;
                    }
                }
                studentsNumberTv.append(studentsCount + "");
                Toast.makeText(ChallengesAndUsersMonitoring.this, "انتهى عد الطلاب", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        challengesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE,-1);
                Date today = new Date();
                SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                formatDate.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String todayDate = formatDate.format(today);
                String yesterdayDate = formatDate.format(calendar.getTime());//TODO : check the value of this
                ArrayList<String> todayChallengesList = new ArrayList<>();
                String challengeTime = "غير موجود";
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String challengeDate = dataSnapshot1.child("date").getValue().toString();
                    if(dataSnapshot1.child("time").getValue() != null)
                        challengeTime = dataSnapshot1.child("time").getValue().toString();
                    String challengeSubject = dataSnapshot1.child("subject").getValue().toString();
                    String challengeState = dataSnapshot1.child("state").getValue().toString();
                    String challengeId = dataSnapshot1.getKey();
                    ArrayList challengeQuestionsList = (ArrayList) dataSnapshot1.child("questionsList").getValue();
                    long player1Score = (long) dataSnapshot1.child("player1score").getValue();
                    long player2Score = (long) dataSnapshot1.child("player2score").getValue();

                    String player1Name = dataSnapshot1.child("player1Name").getValue().toString();
                    String player1Image = dataSnapshot1.child("player1Image").getValue().toString();
                    String player1Uid = dataSnapshot1.child("player1Uid").getValue().toString();
                    String player2Name = dataSnapshot1.child("player2Name").getValue().toString();
                    String player2Image = dataSnapshot1.child("player2Image").getValue().toString();
                    String player2Uid = dataSnapshot1.child("player2Uid").getValue().toString();

                    allChallengesCount++;

                    if (challengeDate.equals(todayDate)) {
                        todayChallengesCount++;
                        todayChallengesList.add(0, " - " + player1Name + "(" + player1Score + ")"
                                + " ضد " + player2Name + "(" + player2Score + ")"
                                + "الساعة : " + challengeTime + " (" + challengeState + ")" + "\n\n");
                    }
                    if(challengeDate.equals(yesterdayDate)){
                        yesterdayChallengesCount ++;
                    }
                }

                allChallengesNumber.append(allChallengesCount + "");
                todayChallengesNumberTv.append(todayChallengesCount + "");
                yesterdayChallengesNumberTv.append(yesterdayChallengesCount + "");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ChallengesAndUsersMonitoring.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, todayChallengesList);
                todayAdminChallengesListView.setAdapter(adapter);

                Toast.makeText(ChallengesAndUsersMonitoring.this, "انتهى حساب التحديات", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
