package com.mk.playAndLearn.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.BestStudentsAdapter;
import com.mk.playAndLearn.model.User;

import java.util.ArrayList;

public class BestStudentsActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList list = new ArrayList();
    BestStudentsAdapter recyclerAdapter;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    //TODO : see what happens when the data changed rapidly and if there is a problem handle it by make data fixed and change it by refreshing
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_students);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        recyclerView = findViewById(R.id.bestStudentsRecyclerView);
        progressBar = findViewById(R.id.bestStudentsProgressBar);
        recyclerAdapter = new BestStudentsAdapter(list, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
            myRef.orderByChild("points").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!list.isEmpty())
                        list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        User user = new User();
                        String name = dataSnapshot1.child("userName").getValue().toString();
                        String points = dataSnapshot1.child("points").getValue().toString();
                        String imageUrl = dataSnapshot1.child("userImage").getValue().toString();
                        user.setName(name);
                        user.setPoints(Integer.parseInt(points));
                        user.setImageUrl(imageUrl);
                        list.add(0, user);

                        if (progressBar.getVisibility() != View.GONE)
                            progressBar.setVisibility(View.GONE);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
