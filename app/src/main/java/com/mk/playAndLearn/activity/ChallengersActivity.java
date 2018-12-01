package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.StudentsAdapter;
import com.mk.playAndLearn.model.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.mk.playAndLearn.utils.Firebase.currentUser;
import static com.mk.playAndLearn.utils.Firebase.lastActiveUsersReference;
import static com.mk.playAndLearn.utils.Firebase.usersReference;

public class ChallengersActivity extends AppCompatActivity {
    ArrayList<User> list = new ArrayList();
    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    String subject, userId;
    TextView noInternetConnectionText, noStudentsTv;
    SwipeRefreshLayout swipeRefreshLayout;

    private final String TAG = "ChallengersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challengers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        if (intent != null) {
            subject = intent.getStringExtra("subject");
        }
        recyclerView = findViewById(R.id.challengersRecyclerView);
        progressBar = findViewById(R.id.challengersProgressBar);
        swipeRefreshLayout = findViewById(R.id.challengersSwipeRefreshLayout);
        noStudentsTv = findViewById(R.id.noActiveStudentsTv);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startAsynkTask();
            }
        });

        recyclerAdapter = new StudentsAdapter(list, this, TAG, subject);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();

        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                noStudentsTv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startAsynkTask();
            }
        });
        noInternetConnectionText.setText("لا يوجد اتصال بالانترنت");

        startAsynkTask();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
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
                    getStudents();
                } else {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    noInternetConnectionText.setVisibility(View.VISIBLE);
                }
            }
        };

        asyncTask.execute();
    }

    public void getStudents() {
        if (!list.isEmpty()) {
            list.clear();
        }
        usersReference.orderByChild("lastChallengeDate").limitToLast(15).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    User user = new User();
                    String points = "";
                    boolean admin = false, online = false;
                    String name = (String) dataSnapshot1.child("userName").getValue();
                    String email = (String) dataSnapshot1.child("userEmail").getValue();
                    String uid = dataSnapshot1.getKey();
                    if (dataSnapshot1.child("points").getValue() != null)
                        points = dataSnapshot1.child("points").getValue().toString();
                    String imageUrl = (String) dataSnapshot1.child("userImage").getValue();
                    String userType = (String) dataSnapshot1.child("userType").getValue();

                    int pointsInt;
                    try {
                        pointsInt = Integer.parseInt(points);
                    } catch (Exception ex) {
                        Log.v("pointsException", "exception is : " + ex);
                        pointsInt = 0;
                    }
                    if (userType.equals("طالب") && !uid.equals(localCurrentUserUid)
                            && dataSnapshot1.child("lastChallengeDate").getValue() != null && name != null) {//TODO : think about allowing challenges against teachers and others and ask my friends about thier opinions in that
                        user.setAdmin(admin);
                        user.setOnline(online);
                        user.setName(name);
                        user.setPoints(pointsInt);
                        user.setImageUrl(imageUrl);
                        user.setEmail(email);
                        user.setUid(uid);
                        list.add(0, user);
                    }
                }

                recyclerAdapter.notifyDataSetChanged();

                if (progressBar.getVisibility() != View.GONE)
                    progressBar.setVisibility(View.GONE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                if (list.size() == 0) {
                    noStudentsTv.setVisibility(View.VISIBLE);
                    noInternetConnectionText.setVisibility(View.GONE);
                } else {
                    noStudentsTv.setVisibility(View.INVISIBLE);
                    noInternetConnectionText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

