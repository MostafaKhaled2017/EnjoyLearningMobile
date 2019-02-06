package com.mk.playAndLearn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mk.enjoylearning.R;
import com.mk.playAndLearn.adapters.RepliesAdapter;
import com.mk.playAndLearn.presenter.PostsInDetailsActivityPresenter;
import com.mk.playAndLearn.presenter.RepliesButtonActivityPresenter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.mk.playAndLearn.activity.MainActivity.deleteCache;


public class RepliesActivity extends AppCompatActivity implements RepliesButtonActivityPresenter.View {
    String content, name, date, image, commentId = "", commentWriterUid = "";
    RepliesButtonActivityPresenter presenter;

    RepliesAdapter recyclerAdapter;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    TextView noRepliesText, noInternetConnectionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies_button);
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
        
        presenter = new RepliesButtonActivityPresenter(RepliesActivity.this, this);
        
        noRepliesText = findViewById(R.id.noRepliesText);

        noInternetConnectionText = findViewById(R.id.noInternetConnectionText);
        noInternetConnectionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnection();
            }
        });

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            commentId = intent.getStringExtra("id");
            commentWriterUid = intent.getStringExtra("commentWriterUid");

        }

        recyclerView = findViewById(R.id.repliesRecyclerView);
        progressBar = findViewById(R.id.repliesProgressBar);

        FloatingActionButton fab = findViewById(R.id.fab);
//        Drawable myFabSrc = getResources().getDrawable(android.R.drawable.ic_input_add);
//        //copy it in a new one
//        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
//        //set the color filter, you can use also Mode.SRC_ATOP
//        willBeWhite.mutate().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
//        //set it to your fab button initialized before
//        fab.setImageDrawable(willBeWhite);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRepliesDialog();
            }
        });

        presenter.startAsynkTask();
    }

    public void showRepliesDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);//TODO : check this
        View view = layoutInflaterAndroid.inflate(R.layout.dialog, null);

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

        final EditText inputReply = view.findViewById(R.id.dialog_value);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("إضافة تعليق");



        alertDialogBuilderUserInput.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = alertDialogBuilderUserInput.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String repliesText = inputReply.getText().toString();
                        if (TextUtils.isEmpty(repliesText.trim())) {
                            inputReply.setError("لا يمكنك ترك هذا الحقل فارغا");
                        }
                        else {
                            presenter.addReply(repliesText);
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
        deleteCache(this);
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
        recyclerAdapter = new RepliesAdapter(list, this, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
    }



    @Override
    public void onNoInternetConnection() {
        progressBar.setVisibility(View.GONE);
        noInternetConnectionText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDataFound() {
        noRepliesText.setVisibility(View.GONE);
        hideProgressBar();
        noInternetConnectionText.setVisibility(View.GONE);
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() != View.GONE)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void notifyAdapter() {
        recyclerView.removeAllViews();
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNoReplyFound() {
        progressBar.setVisibility(View.GONE);
        noRepliesText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showToast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getCommentId() {
        return commentId;
    }

    @Override
    public String getCommentWriterUid() {
        return commentWriterUid;
    }
}
