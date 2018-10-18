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
import com.mk.playAndLearn.presenter.PostsInDetailsActivityPresenter;
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
import static com.mk.playAndLearn.utils.Firebase.commentsReference;
import static com.mk.playAndLearn.utils.Strings.currentUserEmail;
import static com.mk.playAndLearn.utils.Strings.currentUserImage;
import static com.mk.playAndLearn.utils.Strings.currentUserName;
import static com.mk.playAndLearn.utils.Strings.currentUserUid;

public class PostInDetailsActivity extends AppCompatActivity implements PostsInDetailsActivityPresenter.View{
    String content, name, date, image;
    TextView contentTv, nameTv, dateTv;
    ImageView imageView;
    PostsInDetailsActivityPresenter presenter;
    
    String postId = "";

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

        presenter = new PostsInDetailsActivityPresenter(this);

        contentTv = findViewById(R.id.postContentInDetails);
        nameTv = findViewById(R.id.postUserNameInDetails);
        dateTv = findViewById(R.id.postDateInDetails);
        noCommentsText = findViewById(R.id.noCommentsText);
        imageView = findViewById(R.id.postImageInDetails);

        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
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

        recyclerView = findViewById(R.id.commentsRecyclerView);
        progressBar = findViewById(R.id.commentsProgressBar);

        FloatingActionButton fab = findViewById(R.id.fab);
        Drawable myFabSrc = getResources().getDrawable(android.R.drawable.ic_input_add);
        //copy it in a new one
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
        //set the color filter, you can use also Mode.SRC_ATOP
        willBeWhite.mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        //set it to your fab button initialized before
        fab.setImageDrawable(willBeWhite);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentsDialog();
            }
        });

        presenter.startAsynkTask();
    }

    public void showCommentsDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);//TODO : check this
        android.view.View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

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
                        String commentText = inputComment.getText().toString();
                        presenter.addComment(commentText);
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        deleteCache(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.removeListeners();
    }

    @Override
    public void retryConnection() {
        noInternetConnectionText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        presenter.startAsynkTask();
    }

    @Override
    public void startRecyclerAdapter(ArrayList list) {
        recyclerAdapter = new CommentsAdapter(list, this, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onNoInternetConnection() {
        progressBar.setVisibility(android.view.View.GONE);
        noInternetConnectionText.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void onDataFound() {
        noCommentsText.setVisibility(android.view.View.GONE);
       hideProgressBar();
        noInternetConnectionText.setVisibility(android.view.View.GONE);
    }

    @Override
    public String getPostId() {
        return postId;
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != android.view.View.GONE)
            progressBar.setVisibility(android.view.View.GONE);
    }

    @Override
    public void notifyAdapter() {
        recyclerView.removeAllViews();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNoCommentsFound() {
        progressBar.setVisibility(android.view.View.GONE);
        noCommentsText.setVisibility(android.view.View.VISIBLE);
    }
}
