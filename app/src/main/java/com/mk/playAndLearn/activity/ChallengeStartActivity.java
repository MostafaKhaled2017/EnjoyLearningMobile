package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.squareup.picasso.Picasso;

import butterknife.OnClick;

public class ChallengeStartActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseDatabase database;


    String firstPlayerName, firstPlayerEmail, firstPlayerImage, firstPlayerUid;
    String secondPlayerName, secondPlayerEmail, secondPlayerImage, secondPlayerUid;
    int firstPlayerPoints, secondPlayerPoints;

    ImageView player1Image, player2Image;
    TextView player1Name, player1Points, player2Name, player2Points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("ابدأ التحدي");//TODO : change this

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        player1Name = findViewById(R.id.firstPlayerName);
        player1Image = findViewById(R.id.firstPlayerImage);
        player1Points = findViewById(R.id.firstPlayerPoints);
        player2Name = findViewById(R.id.secondPlayerName);
        player2Image = findViewById(R.id.secondPlayerImage);
        player2Points = findViewById(R.id.secondPlayerPoints);

        final DatabaseReference usersRefrence = database.getReference("users");
        usersRefrence.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstPlayerPoints = Integer.parseInt(dataSnapshot.child("points").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        firstPlayerName = currentUser.getDisplayName();
        firstPlayerEmail = currentUser.getEmail();
        firstPlayerUid = currentUser.getUid();
        firstPlayerImage = currentUser.getPhotoUrl().toString();

        Intent intent = getIntent();
        if(intent != null){
            secondPlayerName = intent.getStringExtra("name");
            secondPlayerEmail = intent.getStringExtra("email");
            secondPlayerImage = intent.getStringExtra("image");
            secondPlayerPoints = intent.getIntExtra("points", -1);
            secondPlayerUid = intent.getStringExtra("uid");
        }

        player1Name.setText(firstPlayerName);
        Picasso.with(this).load(firstPlayerImage).into(player1Image);
        player1Points.setText(firstPlayerPoints +"");

        player2Name.setText(secondPlayerName);
        Picasso.with(this).load(secondPlayerImage).into(player2Image);
        player2Points.setText(secondPlayerPoints + "");
    }

   // @OnClick(R.id.) TODO
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
