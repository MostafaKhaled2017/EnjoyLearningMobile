package com.mk.playAndLearn.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.CommentsAdapter;
import com.mk.playAndLearn.model.Comment;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.mk.playAndLearn.activity.MainActivity.deleteCache;

public class PostInDetailsActivity extends AppCompatActivity {
    String content, name, date, image;
    TextView contentTv, nameTv, dateTv;
    ImageView imageView;
    ArrayList list = new ArrayList();

    DatabaseReference myRef;
    FirebaseDatabase database;
    FirebaseAuth auth;

    String userName = "", userImage = "", userEmail = "", postId = "";
    SharedPreferences sharedPreferences;

    CommentsAdapter recyclerAdapter;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noCommentsText, noInternetConnectionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_in_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //TODO : think about editing this drawable but I think it is good
        final Drawable upArrow = getResources().getDrawable(R.drawable.back_arrow);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        deleteCache(this);

        contentTv = findViewById(R.id.postContentInDetails);
        nameTv = findViewById(R.id.postUserNameInDetails);
        dateTv = findViewById(R.id.postDateInDetails);
        noCommentsText = findViewById(R.id.noCommentsText);
        imageView = findViewById(R.id.postImageInDetails);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("comments");
        auth = FirebaseAuth.getInstance();

        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noInternetConnectionText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startAsynkTask();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            content = intent.getStringExtra("content");
            date = intent.getStringExtra("date");
            name = intent.getStringExtra("name");
            image = intent.getStringExtra("image");
            postId = intent.getStringExtra("id");

            contentTv.setText(content);
            dateTv.setText(date);
            nameTv.setText(name);
            Picasso.with(this).load(image).into(imageView);
        }

        sharedPreferences = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            if (sharedPreferences.contains("userName")) {
                userName = sharedPreferences.getString("userName", "");
            }
            if (sharedPreferences.contains("userImage")) {
                userImage = sharedPreferences.getString("userImage", "");
            }
            if (sharedPreferences.contains("userEmail")) {
                userEmail = sharedPreferences.getString("userEmail", "");
            }
        }

        recyclerView = findViewById(R.id.commentsRecyclerView);
        progressBar = findViewById(R.id.commentsProgressBar);
        recyclerAdapter = new CommentsAdapter(list, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();

        FloatingActionButton fab = findViewById(R.id.fab);
        Drawable myFabSrc = getResources().getDrawable(android.R.drawable.ic_input_add);
        //copy it in a new one
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        //set the color filter, you can use also Mode.SRC_ATOP
        willBeWhite.mutate().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        //set it to your fab button initialized before
        fab.setImageDrawable(willBeWhite);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentsDialog();
            }
        });

        startAsynkTask();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void showCommentsDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("إضافة تعليق");

        alertDialogBuilderUserInput.setCancelable(true);
        alertDialogBuilderUserInput.setPositiveButton("إلغاء", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialogBuilderUserInput.setNegativeButton("إضافة",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        Date today = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());//TODO : check that the date changes at 12 p.m exactly
                        String date = format.format(today);

                        Map<String, Object> map = new HashMap<>();
                        map.put("userName", userName);
                        map.put("userEmail", userEmail);
                        map.put("userImage", userImage);
                        map.put("votes", 0);
                        map.put("date", date);
                        map.put("postId", postId);
                        map.put("userUid", auth.getCurrentUser().getUid());
                        map.put("content", inputComment.getText().toString());
                        myRef.push().setValue(map);
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

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
                    getComments();
                } else {
                    progressBar.setVisibility(View.GONE);
                    noInternetConnectionText.setVisibility(View.VISIBLE);
                }
            }
        };

        asyncTask.execute();
    }

    public void getComments() {
        myRef.orderByChild("postId").equalTo(postId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Lesson value = dataSnapshot.getValue(Lesson.class);
                noCommentsText.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                noInternetConnectionText.setVisibility(View.GONE);
                Comment comment = new Comment();
                String userName = dataSnapshot.child("userName").getValue().toString();
                String content = dataSnapshot.child("content").getValue().toString();
                String userImage = dataSnapshot.child("userImage").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                comment.setUserName(userName);
                comment.setContent(content);
                comment.setUserImage(userImage);
                comment.setDate(date);
                list.add(0, comment);
                if (progressBar.getVisibility() != View.GONE)
                    progressBar.setVisibility(View.GONE);
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getActivity(), "فشل تحميل البينات من فضلك تأكد من الاتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                Log.v("Logging", "database error : " + databaseError);
                progressBar.setVisibility(View.GONE);
            }
        });
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (list.size() == 0) {
                    progressBar.setVisibility(View.GONE);
                    noCommentsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        deleteCache(this);
    }
}
