package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mk.enjoylearning.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.mk.playAndLearn.service.NotificationsService.adjustSubject;
import static com.mk.playAndLearn.utils.Firebase.fireStoreChallenges;
import static com.mk.playAndLearn.utils.Firebase.usersReference;
import static com.mk.playAndLearn.utils.Strings.completedChallengeText;
import static com.mk.playAndLearn.utils.Strings.refusedChallengeText;
import static com.mk.playAndLearn.utils.Strings.uncompletedChallengeText;

public class AdminChallengesAndUsersMonitoring extends AppCompatActivity {

    TextView allChallengesNumber, todayChallengesNumberTv, yesterdayChallengesNumberTv;

    ListView todayAdminChallengesListView;
    int studentsCount = 0, allChallengesCount = 0, todayChallengesCount = 0, yesterdayChallengesCount = 0
    ,todayCompletedChallengesCount = 0, yesterdayCompletedChallengesCount = 0, allUsersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_challenges_and_users_monitoring);
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

        allChallengesNumber = findViewById(R.id.allChallengesNumberTv);
        todayChallengesNumberTv = findViewById(R.id.todayChallengesNumberTv);
        yesterdayChallengesNumberTv = findViewById(R.id.yesterdayChallengesNumberTv);
        todayAdminChallengesListView = findViewById(R.id.todayAdminChallengesListView);

        fireStoreChallenges.orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE,-1);
                Date today = new Date();
                SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                formatDate.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String todayDate = formatDate.format(today);
                String yesterdayDate = formatDate.format(calendar.getTime());

                SimpleDateFormat fullDateFormatter = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH);
                fullDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT+2"));


                ArrayList<String> todayChallengesList = new ArrayList<>();
                String challengeTime = "غير موجود";
                for (DocumentSnapshot documentSnapshot : documentSnapshots.getDocuments()) {
                    String challengeDate = formatDate.format(documentSnapshot.get("date"));
                    String fullDate = fullDateFormatter.format(documentSnapshot.get("date"));
                    String challengeState = documentSnapshot.getString("state");
                    long player1Score = (long) documentSnapshot.getLong("player1score");
                    long player2Score = (long) documentSnapshot.getLong("player2score");

                    String player1Name = documentSnapshot.getString("player1Name");
                    String subject = documentSnapshot.getString("subject");
                    String player2Name = documentSnapshot.getString("player2Name");

                    allChallengesCount++;

                    if (challengeDate.equals(todayDate)) {
                        todayChallengesCount++;

                        if(challengeState.equals(completedChallengeText) || challengeState.equals(refusedChallengeText))
                            todayCompletedChallengesCount ++;

                        todayChallengesList.add(" - " + player1Name + "(" + player1Score + ")"
                                + " ضد " + player2Name + "(" + player2Score + ")"
                                + " الساعة " + fullDate.substring(10) + " في مادة " + adjustSubject(subject)+ " (" + challengeState + ")" + "\n\n");
                    }
                    if(challengeDate.equals(yesterdayDate)){
                        yesterdayChallengesCount ++;

                        if(challengeState.equals(completedChallengeText))
                            yesterdayCompletedChallengesCount ++;

                    }
                }

                allChallengesNumber.append(allChallengesCount + "");
                todayChallengesNumberTv.append(todayChallengesCount + " (" + todayCompletedChallengesCount + ")");
                yesterdayChallengesNumberTv.append(yesterdayChallengesCount + " (" + yesterdayCompletedChallengesCount + ")");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminChallengesAndUsersMonitoring.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, todayChallengesList);
                todayAdminChallengesListView.setAdapter(adapter);

                Toast.makeText(AdminChallengesAndUsersMonitoring.this, "انتهى حساب التحديات", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
