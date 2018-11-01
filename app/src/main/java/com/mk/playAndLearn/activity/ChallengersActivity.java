package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import static com.mk.playAndLearn.utils.Firebase.usersReference;

public class ChallengersActivity extends AppCompatActivity {
    ArrayList list = new ArrayList();
    StudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    String subject;
    TextView noInternetConnectionText;

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
        if(intent != null){
            subject = intent.getStringExtra("subject");
        }
        recyclerView = findViewById(R.id.challengersRecyclerView);
        progressBar = findViewById(R.id.challengersProgressBar);
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
                progressBar.setVisibility(View.VISIBLE);
                startAsynkTask();
            }
        });

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
                    noInternetConnectionText.setVisibility(View.VISIBLE);
                }
            }
        };

        asyncTask.execute();
    }

    public void getStudents(){
        usersReference.orderByChild("userName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO : think about the conditions here
                if(list.isEmpty()) {

                    String localCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        User user = new User();
                        boolean admin = false;
                        String name = dataSnapshot1.child("userName").getValue().toString();
                        String email = dataSnapshot1.child("userEmail").getValue().toString();
                        String uid = dataSnapshot1.getKey();
                        String points = dataSnapshot1.child("points").getValue().toString();
                        String imageUrl = dataSnapshot1.child("userImage").getValue().toString();
                        String userType = dataSnapshot1.child("userType").getValue().toString();

                        if(dataSnapshot1.child("admin").getValue() != null)
                            admin = (boolean) dataSnapshot1.child("admin").getValue();
                        if (userType.equals("طالب") && !uid.equals(localCurrentUserUid)) {//TODO : think about allowing challenges against teachers and others and ask my friends about thier opinions in that
                            user.setAdmin(admin);
                            user.setName(name);
                            user.setPoints(Integer.parseInt(points));
                            user.setImageUrl(imageUrl);
                            user.setEmail(email);
                            user.setUid(uid);
                            list.add(user);
                        }
                        if (progressBar.getVisibility() != View.GONE)
                            progressBar.setVisibility(View.GONE);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
