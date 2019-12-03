package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.CommentsAdapter;
import com.mk.playAndLearn.presenter.PostsInDetailsActivityPresenter;

import java.util.ArrayList;


public class PostInDetailsActivity extends AppCompatActivity implements PostsInDetailsActivityPresenter.View {
    String content, name, date, image, postId = "", postWriterUid = "";
    PostsInDetailsActivityPresenter presenter;

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
       /* final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = toolbar.findViewById(R.id.title);
        toolbarTitle.setText("التعليقات");*/

        presenter = new PostsInDetailsActivityPresenter(this, this);

        noCommentsText = findViewById(R.id.noCommentsText);

        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            content = intent.getStringExtra("content");
            date = intent.getStringExtra("date");
            name = intent.getStringExtra("name");
            image = intent.getStringExtra("image");
            postId = intent.getStringExtra("id");
            postWriterUid = intent.getStringExtra("postWriterUid");

        }

        recyclerView = findViewById(R.id.commentsRecyclerView);
        progressBar = findViewById(R.id.commentsProgressBar);

        FloatingActionButton fab = findViewById(R.id.fab);
//        Drawable myFabSrc = getResources().getDrawable(android.R.drawable.ic_input_add);
//        //copy it in a new one
//        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
//        //set the color filter, you can use also Mode.SRC_ATOP
//        willBeWhite.mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
//        //set it to your fab googleBtnIv initialized before
//        fab.setImageDrawable(willBeWhite);
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

        final AlertDialog alertDialogBuilderUserInput = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("إلغاء", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("إضافة", null)
                .create();

        final EditText inputComment = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("إضافة تعليق");



        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String commentText = inputComment.getText().toString();
                        if (TextUtils.isEmpty(commentText.trim())) {
                            inputComment.setError("لا يمكنك ترك هذا الحقل فارغا");
                        }
                        else {
                            presenter.addComment(commentText);
                            alertDialogBuilderUserInput.dismiss();
                        }
                    }
                });
            }
        });

        alertDialogBuilderUserInput.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        recyclerView.setNestedScrollingEnabled(false);
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
    public String getPostWriterUid() {
        return postWriterUid;
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

    @Override
    public void showToast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    public void finishch(View view) {

        finish();
    }
}
